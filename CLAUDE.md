# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Keycloak custom authenticator extension that enforces WebAuthn (hardware token) setup with a configurable grace period. The authenticator allows users a configurable grace period (default: 24 hours) from account creation to register a WebAuthn security key (e.g., YubiKey) before requiring them to set up MFA.

## Build Commands

Build the project:
```bash
mvn clean package
```

The build produces `target/otp-grace-period-authenticator.jar` which can be deployed to Keycloak.

## Architecture

### Core Components

**GracePeriodOTPAuthenticator** (`src/main/java/com/redhat/keycloak/GracePeriodOTPAuthenticator.java`)
- Main authenticator logic implementing Keycloak's `Authenticator` interface
- Grace period: Configurable via admin UI (default: 24 hours, defined by `DEFAULT_GRACE_PERIOD_HOURS` constant)
- Credential type: WebAuthn only (hardware tokens like YubiKey)
- Behavior:
  - If user has WebAuthn configured: Allow access
  - If user created within grace period without WebAuthn: Allow access with warning (sets auth note with hours/minutes remaining)
  - If user created beyond grace period without WebAuthn: Add `webauthn-register` required action, redirect to security key registration

**GracePeriodOTPAuthenticatorFactory** (`src/main/java/com/redhat/keycloak/GracePeriodOTPAuthenticatorFactory.java`)
- Factory for creating authenticator instances (Keycloak SPI pattern)
- Provider ID: `grace-period-otp-authenticator`
- Display name: "WebAuthn Grace Period Enforcer"
- Reference category: `webauthn`
- Fully configurable via admin UI (grace period in hours)
- Creates new instance per request (thread-safe, no singleton pattern)

### Keycloak SPI Integration

The authenticator is registered via Java ServiceLoader:
- `src/main/resources/META-INF/services/org.keycloak.authentication.AuthenticatorFactory` contains the factory class name
- This allows Keycloak to discover and load the authenticator at runtime

### Dependencies

- Keycloak version: 26.0.0
- Java: 17
- All Keycloak dependencies are `provided` scope (supplied by Keycloak runtime)
- Required Keycloak modules: core, server-spi, server-spi-private, services

## Deployment

Deploy the JAR to Keycloak's providers directory (exact location varies by Keycloak version and deployment method), then configure in Authentication flows via the Keycloak admin console.
