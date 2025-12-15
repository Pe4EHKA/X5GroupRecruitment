# Security Considerations for Production Deployment

## Overview

This document outlines security considerations and required changes for production deployment of the X5 Tech Recruitment System MVP.

## ⚠️ DEV-Only Implementations (MUST CHANGE for Production)

### 1. Authentication & Credentials Storage

**Current Implementation (DEV):**
- HTTP Basic Authentication with hardcoded test users
- Username/password stored in localStorage
- Simple role-based access without proper token validation

**Required for Production:**
```
✅ Implement OAuth2/OIDC (e.g., Keycloak)
✅ Use JWT tokens with short expiration
✅ Store tokens in HTTP-only secure cookies (NOT localStorage)
✅ Implement token refresh mechanism
✅ Add proper session management
✅ Implement logout endpoint that invalidates tokens
```

**Files to Update:**
- `apps/frontend/src/providers/AuthProvider.tsx` - Replace localStorage with secure cookies
- `apps/frontend/src/lib/api.ts` - Update to use JWT tokens
- `apps/backend/src/main/java/com/x5/recruitment/infrastructure/security/SecurityConfig.java` - Replace Basic Auth with JWT

### 2. Database Credentials

**Current Implementation (DEV):**
```properties
# apps/backend/src/main/resources/application.properties
spring.datasource.password=recruitment123  # HARDCODED
```

**Required for Production:**
```
✅ Use environment variables for all credentials
✅ Use secret management (e.g., HashiCorp Vault, AWS Secrets Manager)
✅ Never commit credentials to repository
✅ Rotate credentials regularly
```

**Example Production Config:**
```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
```

### 3. CSRF Protection

**Current Implementation (DEV):**
```java
// SecurityConfig.java
.csrf(csrf -> csrf.disable()) // Disabled for stateless API
```

**Required for Production:**
```
✅ Enable CSRF protection for browser-based requests
✅ Use CSRF tokens for state-changing operations
✅ Configure CORS to only allow production domains
```

**Example Production Config:**
```java
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    .ignoringRequestMatchers("/api/public/**")
)
```

### 4. CORS Configuration

**Current Implementation (DEV):**
```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000", 
    "http://localhost:8080"
));
```

**Required for Production:**
```
✅ Replace with production domain(s) only
✅ Remove localhost origins
✅ Use specific methods, not wildcard
✅ Limit allowed headers
```

**Example Production Config:**
```java
configuration.setAllowedOrigins(Arrays.asList(
    "https://recruitment.x5.ru"
));
configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE"));
configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
```

### 5. Email Service

**Current Implementation (DEV):**
```java
// EmailService.java - Stub implementation
log.info("Would send email to: {}", to);
```

**Required for Production:**
```
✅ Implement real SMTP integration
✅ Use email service provider (SendGrid, AWS SES, etc.)
✅ Configure SPF, DKIM, DMARC records
✅ Implement email templates
✅ Add email sending queues and retry logic
✅ Monitor email delivery rates
```

## 🔒 Additional Security Requirements

### 6. HTTPS/TLS

**Required:**
```
✅ Enable HTTPS for all connections
✅ Use valid SSL/TLS certificates (Let's Encrypt, commercial CA)
✅ Configure HSTS headers
✅ Disable HTTP (redirect to HTTPS)
✅ Use TLS 1.2 or higher
```

**Backend Configuration:**
```properties
server.ssl.enabled=true
server.ssl.key-store=${SSL_KEYSTORE_PATH}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
```

### 7. Security Headers

**Required:**
```
✅ Content-Security-Policy
✅ X-Frame-Options: DENY
✅ X-Content-Type-Options: nosniff
✅ X-XSS-Protection: 1; mode=block
✅ Strict-Transport-Security
✅ Referrer-Policy
```

**Implementation:**
```java
http.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp
        .policyDirectives("default-src 'self'")
    )
    .frameOptions(frame -> frame.deny())
    .xssProtection(xss -> xss.block(true))
    .httpStrictTransportSecurity(hsts -> hsts
        .maxAgeInSeconds(31536000)
        .includeSubDomains(true)
    )
);
```

### 8. Rate Limiting

**Required:**
```
✅ Implement rate limiting for all endpoints
✅ Stricter limits for authentication endpoints
✅ Per-IP and per-user limits
✅ Return 429 Too Many Requests
```

**Recommended Libraries:**
- Bucket4j for Java
- express-rate-limit for Node.js (if using proxy)

### 9. Input Validation

**Current:**
- Basic validation with Bean Validation annotations

**Enhanced for Production:**
```
✅ Strict input sanitization
✅ Whitelist allowed characters
✅ File upload validation (type, size, content)
✅ SQL injection prevention (already handled by JPA)
✅ XSS prevention in frontend
```

### 10. Logging & Monitoring

**Required:**
```
✅ Centralized logging (ELK stack, CloudWatch, etc.)
✅ Security event monitoring
✅ Failed login attempt tracking
✅ Anomaly detection
✅ Audit trail for all data changes
✅ PII data masking in logs
```

**Never Log:**
- Passwords (even hashed)
- Full credit card numbers
- Personal identification numbers
- Session tokens
- API keys

### 11. Database Security

**Required:**
```
✅ Use connection pooling with proper limits
✅ Encrypt sensitive data at rest
✅ Regular database backups
✅ Backup encryption
✅ Access control and least privilege
✅ SQL injection prevention (using parameterized queries)
✅ Database audit logging
```

### 12. Dependency Management

**Required:**
```
✅ Regular dependency updates
✅ Security vulnerability scanning (npm audit, OWASP Dependency Check)
✅ Remove unused dependencies
✅ Pin dependency versions
✅ Use private npm registry for sensitive packages
```

**Commands:**
```bash
# Backend
mvn dependency-check:check

# Frontend
npm audit
npm audit fix
```

### 13. Secrets Management

**Required:**
```
✅ Never commit secrets to Git
✅ Use .env files for local development (gitignored)
✅ Use secret management service for production
✅ Rotate secrets regularly
✅ Audit secret access
```

**Recommended Services:**
- AWS Secrets Manager
- HashiCorp Vault
- Azure Key Vault
- Google Secret Manager

### 14. File Upload Security

**Current:**
- Excel file upload in import endpoint

**Enhanced for Production:**
```
✅ Validate file types (magic bytes, not just extension)
✅ Limit file sizes
✅ Scan for viruses/malware
✅ Store files outside web root
✅ Generate random file names
✅ Set proper file permissions
```

**Example:**
```java
@PostMapping("/import")
public ResponseEntity<?> importFile(
    @RequestParam("file") MultipartFile file) {
    
    // Validate file type
    if (!file.getContentType().equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
        throw new IllegalArgumentException("Only XLSX files allowed");
    }
    
    // Validate file size (10MB max)
    if (file.getSize() > 10 * 1024 * 1024) {
        throw new IllegalArgumentException("File too large");
    }
    
    // Continue with processing...
}
```

## 📋 Production Deployment Checklist

### Pre-Deployment
- [ ] All hardcoded credentials removed
- [ ] Environment variables configured
- [ ] Secrets stored in secret management service
- [ ] CORS configured for production domain only
- [ ] CSRF protection enabled
- [ ] HTTPS/TLS configured
- [ ] Security headers configured
- [ ] Rate limiting implemented
- [ ] Logging and monitoring configured
- [ ] Database backups configured
- [ ] Dependencies updated and scanned
- [ ] Security review completed

### Authentication
- [ ] OAuth2/OIDC implemented
- [ ] JWT tokens with expiration
- [ ] Secure cookie storage
- [ ] Token refresh mechanism
- [ ] Session management
- [ ] Logout functionality

### Infrastructure
- [ ] Firewall rules configured
- [ ] Network segmentation
- [ ] Database access restricted
- [ ] Load balancer configured
- [ ] Auto-scaling configured
- [ ] DDoS protection enabled

### Monitoring
- [ ] Application monitoring (APM)
- [ ] Security monitoring (SIEM)
- [ ] Uptime monitoring
- [ ] Error tracking (Sentry, Rollbar)
- [ ] Performance monitoring
- [ ] Audit logging

### Compliance
- [ ] GDPR compliance (if applicable)
- [ ] Data retention policies
- [ ] Privacy policy updated
- [ ] Terms of service updated
- [ ] User data export functionality
- [ ] User data deletion functionality

## 🔍 Security Testing

### Before Production
```
✅ Penetration testing
✅ Vulnerability scanning
✅ OWASP Top 10 verification
✅ SQL injection testing
✅ XSS testing
✅ CSRF testing
✅ Authentication/authorization testing
✅ Session management testing
✅ API security testing
```

### Recommended Tools
- **OWASP ZAP** - Web application security scanner
- **Burp Suite** - Web vulnerability scanner
- **SonarQube** - Code quality and security
- **npm audit** - npm package vulnerabilities
- **OWASP Dependency Check** - Maven dependencies
- **Snyk** - Dependency vulnerability scanning

## 📞 Incident Response

### Required
```
✅ Security incident response plan
✅ Escalation procedures
✅ Communication templates
✅ Breach notification procedures
✅ Recovery procedures
✅ Post-incident review process
```

## 📚 References

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [OWASP ASVS](https://owasp.org/www-project-application-security-verification-standard/)
- [Spring Security Best Practices](https://docs.spring.io/spring-security/reference/index.html)
- [Next.js Security](https://nextjs.org/docs/app/building-your-application/configuring/security-headers)

## 🔄 Regular Maintenance

### Weekly
- Review security logs
- Check for failed login attempts
- Monitor error rates

### Monthly
- Update dependencies
- Security vulnerability scan
- Review access logs
- Backup verification

### Quarterly
- Security audit
- Penetration testing
- Review and update security policies
- Credential rotation

---

**Last Updated:** December 2025  
**Version:** 1.0  
**Status:** DEV - Requires Production Hardening
