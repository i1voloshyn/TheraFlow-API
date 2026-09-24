# TheraFlow TODO

## Account

### Registration
- [x] Create Therapist account
- [x] Validate registration data
- [x] Hash password before persistence
- [x] Prevent duplicate email registration
- [x] Send email verification
  - → [Email Verification](#email-verification)
- [x] Verify email address
  - → [Email Verification](#email-verification)
- [ ] Restrict protected features for unverified accounts
  - Registered but unverified therapist → can log in
  - Unverified therapist → cannot create/manage patients


### Authentication

#### Login
- [x] Authenticate credentials
- [ ] Generate authentication tokens
  - → [Authentication Token Flow](#authentication-token-flow)

#### Logout
- [ ] Define token invalidation strategy
- [ ] Invalidate refresh token
  - → [Refresh Token Flow](#refresh-token-flow)

#### Refresh Access Token
- [ ] Validate refresh token
  - → [Refresh Token](#refresh-token)
- [ ] Rotate refresh token
  - → [Refresh Token](#refresh-token)
- [ ] Generate new access token
  - → [Authentication Token Flow](#authentication-token-flow)


### Password

#### Change Password
- [ ] Verify current password
  - → [Password Flow](#password-flow)
- [ ] Hash new password
  - → [Password Flow](#password-flow)
- [ ] Update password


#### Forgot Password
- [ ] Request password reset
  - → [Password Reset Flow](#password-reset-flow)
- [ ] Send password reset email
  - → [Email Verification](#email-verification)
  

#### Reset Password
- [ ] Validate reset token
  - → [Password Reset](#password-reset)
- [ ] Set new password
  - → [Password Flow](#password-flow)


---

# Reusable Flows

## Authentication Token Flow

Used by:
- Registration
- Login
- Refresh Access Token


### Access Token
- [x] Generate access token
- [x] Validate access token
- [x] Extract account identity from access token

### Refresh Token
- [ ] Generate refresh token
- [ ] Hash refresh token
- [ ] Persist refresh token
- [ ] Find refresh token
- [ ] Validate refresh token
- [ ] Revoke refresh token
- [ ] Rotate refresh token


### Email Verification
- [x] Generate verification token
- [x] Send verification email
- [x] Validate verification token
- [x] Mark account as verified

### Password
- [ ] Change password
- [ ] Forgot password
- [ ] Reset password

### Password Reset
- [ ] Generate reset token
- [ ] Send reset email
- [ ] Validate reset token
- [ ] Invalidate reset token
