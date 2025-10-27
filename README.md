# Keycloak OTP Grace Period Authenticator

A custom Keycloak authenticator that enforces OTP (One-Time Password) configuration with a configurable grace period for new users.

## Overview

This authenticator allows organizations to enforce MFA/OTP requirements while giving new users a grace period to set up their authentication method. After the grace period expires, users without OTP configured will be locked out until they configure it.

## Features

- **24-hour grace period** from account creation for OTP setup
- **Automatic account locking** when grace period expires without OTP configuration
- **Grace period notifications** - remaining hours are tracked in the authentication session
- **Seamless integration** with Keycloak's authentication flows

## How It Works

When a user attempts to authenticate:

1. **User has OTP configured**: Authentication proceeds normally
2. **User created <24 hours ago without OTP**: Authentication succeeds with a warning (grace period active)
3. **User created >24 hours ago without OTP**:
   - Authentication fails
   - User account is disabled
   - Account locked reason is set to `OTP_NOT_CONFIGURED`

## Building

Build the project with Maven:

```bash
mvn clean package
```

This produces `target/otp-grace-period-authenticator.jar`.

## Installation

1. Copy the built JAR to your Keycloak providers directory:
   - Keycloak 17+: `providers/` directory
   - Older versions: `standalone/deployments/` or `domain/deployments/`

2. Restart Keycloak or run the build command (for Quarkus-based Keycloak):
   ```bash
   kc.sh build
   ```

3. The authenticator will appear in the Keycloak admin console under Authentication > Flows

## Configuration

### Adding to an Authentication Flow

1. Navigate to **Authentication** > **Flows** in the Keycloak admin console
2. Create a new flow or copy an existing one
3. Add the **"OTP Grace Period Enforcer"** execution to your flow
4. Set the requirement to **REQUIRED**
5. Bind the flow to your desired authentication scenario (browser, direct grant, etc.)

## Requirements

- Keycloak 26.0.0 (or compatible version)
- Java 17 or higher
- Maven 3.6+

## Technical Details

- **Provider ID**: `grace-period-otp-authenticator`
- **Category**: OTP
- **User attribute set**: `account_locked_reason` (when locked)
- **Auth note set**: `grace_period_hours_remaining` (during grace period)

## License

This project does not currently specify a license.
