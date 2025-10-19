
# mfano-registration

Spring Boot 3.2 / Java 21 authentication project with:
- Email verification tokens
- BCrypt password hashing
- Four roles: ADMIN, DIRECTOR, HOI, USER
- Admin management panel (CRUD, toggle, reset)
- Resend verification + Forgot/Reset password (email tokens)
- Docker Compose with MySQL + MailDev + app
- Bootstrap + Thymeleaf UI

**Important security note:** I did **not** embed any real Gmail password or sensitive secret into this repository.
You should provide secrets using environment variables, Docker secrets, or CI/CD secret management.

## Quick start (Docker, recommended)

1. Copy `.env.example` to `.env` and fill in values (especially `SPRING_MAIL_PASSWORD` if using Gmail).
2. Build & run:

```bash
docker compose up --build
```

- App: http://localhost:8080  
- MailDev UI: http://localhost:1080

Default DB: `auth_demo`.


Changes made: CSRF tokens added to all forms, shared layout, Profile page, Audit logging for admin actions.
