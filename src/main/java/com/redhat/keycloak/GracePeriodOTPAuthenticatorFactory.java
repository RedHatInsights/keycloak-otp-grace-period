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
    public static final String CONFIG_GRACE_PERIOD_HOURS = "grace.period.hours";

    @Override
    public String getDisplayType() {
        return "WebAuthn Grace Period Enforcer";
    }

    @Override
    public String getReferenceCategory() {
        return "webauthn";
    }

    @Override
    public boolean isConfigurable() {
        return true;
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
        return "Enforces WebAuthn (hardware token) configuration after a configurable grace period from account creation";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty gracePeriodProperty = new ProviderConfigProperty();
        gracePeriodProperty.setType(ProviderConfigProperty.STRING_TYPE);
        gracePeriodProperty.setName(CONFIG_GRACE_PERIOD_HOURS);
        gracePeriodProperty.setLabel("Grace Period (hours)");
        gracePeriodProperty.setHelpText("Number of hours after account creation before WebAuthn is required. Default is 24 hours.");
        gracePeriodProperty.setDefaultValue("24");

        return List.of(gracePeriodProperty);
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
