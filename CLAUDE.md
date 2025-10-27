# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Keycloak custom authenticator extension that enforces OTP (One-Time Password) setup with a configurable grace period. The authenticator allows users a 24-hour grace period from account creation to configure OTP before blocking their access.

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
- Grace period: 24 hours (defined by `GRACE_PERIOD_HOURS` constant)
- Behavior:
  - If user has OTP configured: Allow access
  - If user created <24h ago without OTP: Allow access with warning (sets auth note with hours remaining)
  - If user created >24h ago without OTP: Block access, disable account, set `account_locked_reason` attribute

**GracePeriodOTPAuthenticatorFactory** (`src/main/java/com/redhat/keycloak/GracePeriodOTPAuthenticatorFactory.java`)
- Factory for creating authenticator instances (Keycloak SPI pattern)
- Provider ID: `grace-period-otp-authenticator`
- Display name: "OTP Grace Period Enforcer"
- Uses singleton pattern for authenticator instances
- Currently not configurable (hard-coded 24h period), but includes commented code showing how to make grace period configurable via admin UI

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
