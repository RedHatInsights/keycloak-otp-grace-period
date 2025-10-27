package com.redhat.keycloak;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class GracePeriodOTPAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "grace-period-otp-authenticator";

    @Override
    public String getDisplayType() {
        return "OTP Grace Period Enforcer";
    }

    @Override
    public String getReferenceCategory() {
        return "otp";
    }

    @Override
    public boolean isConfigurable() {
        return false; // Set to true if you want configurable grace period
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[]{
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Enforces OTP configuration after a 24-hour grace period from account creation";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of();
        // If you want configurable grace period:
        // return List.of(
        //     new ProviderConfigProperty(
        //         "grace.period.hours",
        //         "Grace Period (hours)",
        //         "Number of hours before OTP is required",
        //         ProviderConfigProperty.STRING_TYPE,
        //         "24"
        //     )
        // );
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new GracePeriodOTPAuthenticator();
    }

    @Override
    public void init(Config.Scope config) {
        // Nothing to initialize
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Nothing to do
    }

    @Override
    public void close() {
        // Nothing to close
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
