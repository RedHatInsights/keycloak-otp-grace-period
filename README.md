# Keycloak WebAuthn Grace Period Authenticator

A custom Keycloak authenticator that enforces WebAuthn (hardware token) configuration with a configurable grace period for new users.

## Overview

This authenticator allows organizations to enforce hardware-based MFA requirements (such as YubiKey) while giving new users a grace period to set up their security key. After the grace period expires, users without WebAuthn configured will be required to register a hardware token before completing login.

## Features

- **Configurable grace period** - Set the grace period in hours via Keycloak admin UI (default: 24 hours)
- **WebAuthn-only enforcement** - Requires hardware security keys (YubiKey, etc.), not software OTP apps
- **Required action enforcement** - Users are prompted to register their security key after grace period expires
- **Grace period notifications** - Remaining time is tracked in the authentication session
- **Seamless integration** with Keycloak's authentication flows

## How It Works

When a user attempts to authenticate:

1. **User has WebAuthn configured**: Authentication proceeds normally
2. **User created within grace period without WebAuthn**: Authentication succeeds with remaining time tracked
3. **User created beyond grace period without WebAuthn**:
   - User is prompted to register a hardware security key (e.g., YubiKey) via required action
   - Cannot complete login until WebAuthn is configured

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
3. Add the **"WebAuthn Grace Period Enforcer"** execution to your flow
4. Set the requirement to **REQUIRED**
5. Click the **gear icon** (⚙️) next to the execution to configure:
   - **Grace Period (hours)**: Number of hours after account creation before WebAuthn is required (default: 24)
6. Bind the flow to your desired authentication scenario (browser, direct grant, etc.)

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| Grace Period (hours) | Number | 24 | Number of hours after account creation before WebAuthn (hardware token) is required. Must be a positive integer. |

## Requirements

- Keycloak 26.0.0 (or compatible version)
- Java 17 or higher
- Maven 3.6+

## Technical Details

- **Provider ID**: `grace-period-otp-authenticator`
- **Display Name**: WebAuthn Grace Period Enforcer
- **Category**: WebAuthn
- **Supported credential type**: WebAuthn (hardware tokens only)
- **Required action**: `webauthn-register`
- **Auth notes set**:
  - `grace_period_hours_remaining` - Hours remaining in grace period
  - `grace_period_minutes_remaining` - Minutes remaining when <1 hour left
- **Event details**: `grace_period_expired=true` when grace period has ended
- **Configuration key**: `grace.period.hours`

## License

This project does not currently specify a license.
