# TheraFlow TODO

## Account

### Registration

- Implement create Therapist account ✅
- Validate registration data ✅
- Hash password before persistence ✅
- Prevent duplicate email registration ✅
- Send email verification after registration ✅
- Verify email address ✅
- Restrict protected features for unverified accounts:Registered but unverified therapist → can log in, but cannot
  create/manage patients.

### Authentication

- Login ✅
- Logout / token invalidation strategy ✅
- JWT authentication ✅
- Refresh access token
- Handle expired access token ✅
- Handle invalid/revoked refresh token

### Password

- Change password ✅
- Forgot password
- Reset password using email link/token
- Require current password when changing password
- Invalidate existing sessions/tokens after password reset

### Email

- Resend verification email
- Verification token expiration
- Prevent reuse of verification token
- Handle already verified account

### Account lifecycle

- Get current account (`/me`)
- Update account information
- Deactivate account
- Reactivate account (if applicable)
- Permanently delete account (if required)