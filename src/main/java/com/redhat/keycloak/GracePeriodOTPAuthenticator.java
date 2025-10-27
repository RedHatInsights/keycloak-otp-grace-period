package com.redhat.keycloak;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.OTPCredentialModel;

import java.util.concurrent.TimeUnit;

public class GracePeriodOTPAuthenticator implements Authenticator {

    private static final Logger logger = Logger.getLogger(GracePeriodOTPAuthenticator.class);
    private static final long GRACE_PERIOD_HOURS = 24;

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        // Defensive check - context should never be null
        if (context == null) {
            logger.error("Authentication context is null");
            return;
        }

        UserModel user = context.getUser();

        // Defensive check - should never be null since requiresUser() returns true
        if (user == null) {
            logger.warn("User is null in authentication context");
            context.failure(AuthenticationFlowError.UNKNOWN_USER);
            return;
        }

        // Check if user has OTP configured
        var credentialManager = user.credentialManager();
        if (credentialManager == null) {
            logger.warn("Credential manager is null for user: " + user.getUsername());
            context.attempted();
            return;
        }

        boolean hasOTP = credentialManager
                .getStoredCredentialsByTypeStream(OTPCredentialModel.TYPE)
                .findAny()
                .isPresent();

        if (hasOTP) {
            // User has OTP configured, allow access
            logger.debugf("User %s has OTP configured, authentication successful", user.getUsername());
            context.success();
            return;
        }

        // User doesn't have OTP - check grace period
        Long createdTimestamp = user.getCreatedTimestamp();

        if (createdTimestamp == null) {
            logger.warnf("User %s has null createdTimestamp, allowing authentication", user.getUsername());
            context.attempted();
            return;
        }

        long currentTime = System.currentTimeMillis();

        // Validate timestamp is not in the future or invalid
        if (createdTimestamp > currentTime) {
            logger.errorf("User %s has future createdTimestamp (%d > %d), possible clock skew or manipulation",
                         user.getUsername(), createdTimestamp, currentTime);
            context.failure(AuthenticationFlowError.INVALID_USER);
            return;
        }

        if (createdTimestamp <= 0) {
            logger.warnf("User %s has invalid createdTimestamp (%d), allowing authentication",
                        user.getUsername(), createdTimestamp);
            context.attempted();
            return;
        }

        long gracePeriodMs = TimeUnit.HOURS.toMillis(GRACE_PERIOD_HOURS);
        long accountAge = currentTime - createdTimestamp;

        if (accountAge < gracePeriodMs) {
            // Within grace period - allow access but warn user
            long remainingMs = gracePeriodMs - accountAge;
            long hoursRemaining = TimeUnit.MILLISECONDS.toHours(remainingMs);

            // If less than 1 hour, show minutes instead
            if (hoursRemaining == 0 && remainingMs > 0) {
                long minutesRemaining = TimeUnit.MILLISECONDS.toMinutes(remainingMs);
                context.getAuthenticationSession().setAuthNote(
                    "grace_period_minutes_remaining",
                    String.valueOf(minutesRemaining)
                );
                logger.debugf("User %s in grace period. %d minutes remaining",
                             user.getUsername(), minutesRemaining);
            } else {
                context.getAuthenticationSession().setAuthNote(
                    "grace_period_hours_remaining",
                    String.valueOf(hoursRemaining)
                );
                logger.debugf("User %s in grace period. %d hours remaining",
                             user.getUsername(), hoursRemaining);
            }

            context.success();
        } else {
            // Grace period expired - add required action instead of disabling account
            logger.warnf("Grace period expired for user %s (account age: %d hours). Adding CONFIGURE_TOTP required action",
                        user.getUsername(), TimeUnit.MILLISECONDS.toHours(accountAge));

            user.addRequiredAction(UserModel.RequiredAction.CONFIGURE_TOTP);
            user.setSingleAttribute("account_locked_reason", "OTP_NOT_CONFIGURED");

            // Set authentication error
            context.getEvent().error("otp_grace_period_expired");
            context.failure(AuthenticationFlowError.CREDENTIAL_SETUP_REQUIRED);
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // Not used for conditional authenticators
        context.attempted();
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // This authenticator is always "configured" - it just checks conditions
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // Optionally add OTP required action if grace period expired
        // user.addRequiredAction(UserModel.RequiredAction.CONFIGURE_TOTP);
    }

    @Override
    public void close() {
        // Nothing to close
    }
}
