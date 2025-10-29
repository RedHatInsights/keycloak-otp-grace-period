# Keycloak OTP Grace Period Authenticator

A custom Keycloak authenticator that enforces OTP (One-Time Password) configuration with a configurable grace period for new users.

## Overview

This authenticator allows organizations to enforce MFA/OTP requirements while giving new users a grace period to set up their authentication method. After the grace period expires, users without OTP configured will be locked out until they configure it.

## Features

- **Configurable grace period** - Set the grace period in hours via Keycloak admin UI (default: 24 hours)
- **Multi-factor authentication support** - Accepts both OTP and WebAuthn credentials
- **Required action enforcement** - Users are prompted to set up MFA after grace period expires
- **Grace period notifications** - Remaining time is tracked in the authentication session
- **Seamless integration** with Keycloak's authentication flows

## How It Works

When a user attempts to authenticate:

1. **User has MFA configured** (OTP or WebAuthn): Authentication proceeds normally
2. **User created within grace period without MFA**: Authentication succeeds with remaining time tracked
3. **User created beyond grace period without MFA**:
   - User is prompted to configure MFA via required action
   - Cannot complete login until MFA is configured

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
5. Click the **gear icon** (⚙️) next to the execution to configure:
   - **Grace Period (hours)**: Number of hours after account creation before MFA is required (default: 24)
6. Bind the flow to your desired authentication scenario (browser, direct grant, etc.)

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| Grace Period (hours) | Number | 24 | Number of hours after account creation before MFA is required. Must be a positive integer. |

## Requirements

- Keycloak 26.0.0 (or compatible version)
- Java 17 or higher
- Maven 3.6+

## Technical Details

- **Provider ID**: `grace-period-otp-authenticator`
- **Category**: OTP
- **Supported MFA types**: OTP (TOTP/HOTP), WebAuthn
- **Auth notes set**:
  - `grace_period_hours_remaining` - Hours remaining in grace period
  - `grace_period_minutes_remaining` - Minutes remaining when <1 hour left
- **Event details**: `grace_period_expired=true` when grace period has ended
- **Configuration key**: `grace.period.hours`

## License

This project does not currently specify a license.
