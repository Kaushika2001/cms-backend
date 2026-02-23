package com.epic.cms.config;

import com.epic.cms.util.LoggerUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Aspect for auditing encryption and decryption operations.
 * Logs all sensitive operations for security monitoring and compliance.
 * 
 * IMPORTANT: This logs WHO performed encryption/decryption operations,
 * but NEVER logs the actual sensitive data (card numbers, keys, etc.)
 */
@Aspect
@Component
public class EncryptionAuditAspect {
    
    /**
     * Audit decryption operations (sensitive - someone is accessing plaintext).
     * This is logged every time encrypted data is decrypted.
     */
    @Before("execution(* com.epic.cms.util.EncryptionUtil.decrypt(..))")
    public void auditDecryption(JoinPoint joinPoint) {
        String user = getCurrentUser();
        String timestamp = LocalDateTime.now().toString();
        
        LoggerUtil.info(EncryptionAuditAspect.class, 
            "[AUDIT] Decryption operation performed by user: {} at {}", 
            user, timestamp
        );
    }
    
    /**
     * Audit encryption operations.
     * This is logged every time data is encrypted.
     */
    @Before("execution(* com.epic.cms.util.EncryptionUtil.encrypt(..))")
    public void auditEncryption(JoinPoint joinPoint) {
        String user = getCurrentUser();
        String timestamp = LocalDateTime.now().toString();
        
        LoggerUtil.info(EncryptionAuditAspect.class, 
            "[AUDIT] Encryption operation performed by user: {} at {}", 
            user, timestamp
        );
    }
    
    /**
     * Audit card creation with encrypted data.
     * Logs when a new card is created and stored with encrypted card number.
     */
    @AfterReturning(
        pointcut = "execution(* com.epic.cms.service.impl.CardServiceImpl.createCard(..))",
        returning = "result"
    )
    public void auditCardCreation(JoinPoint joinPoint, Object result) {
        String user = getCurrentUser();
        String timestamp = LocalDateTime.now().toString();
        
        LoggerUtil.info(EncryptionAuditAspect.class, 
            "[AUDIT] Card created with encrypted storage by user: {} at {}", 
            user, timestamp
        );
    }
    
    /**
     * Audit encrypted payload reception from frontend.
     * Logs when backend receives and decrypts a payload from frontend.
     */
    @Before("execution(* com.epic.cms.controller.CardController.createCardEncrypted(..))")
    public void auditEncryptedPayloadReception(JoinPoint joinPoint) {
        String user = getCurrentUser();
        String timestamp = LocalDateTime.now().toString();
        String clientIp = getClientIp();
        
        LoggerUtil.info(EncryptionAuditAspect.class, 
            "[AUDIT] Encrypted payload received from client IP: {} by user: {} at {}", 
            clientIp, user, timestamp
        );
    }
    
    /**
     * Audit card data retrieval operations (when decryption happens for viewing).
     */
    @Before("execution(* com.epic.cms.service.impl.CardServiceImpl.getCardByCardNumber(..))")
    public void auditCardRetrieval(JoinPoint joinPoint) {
        String user = getCurrentUser();
        String timestamp = LocalDateTime.now().toString();
        
        LoggerUtil.info(EncryptionAuditAspect.class, 
            "[AUDIT] Card data retrieved (will be decrypted) by user: {} at {}", 
            user, timestamp
        );
    }
    
    /**
     * Audit bulk card retrieval operations.
     */
    @Before("execution(* com.epic.cms.service.impl.CardServiceImpl.getAllCards())")
    public void auditBulkCardRetrieval(JoinPoint joinPoint) {
        String user = getCurrentUser();
        String timestamp = LocalDateTime.now().toString();
        
        LoggerUtil.warn(EncryptionAuditAspect.class, 
            "[AUDIT] BULK card retrieval (multiple decryptions) by user: {} at {}", 
            user, timestamp
        );
    }
    
    /**
     * Get the current authenticated user from Spring Security context.
     * 
     * @return Username or "SYSTEM" if no authentication context exists
     */
    private String getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
            return "SYSTEM";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }
    
    /**
     * Get client IP address (placeholder - implement based on your setup).
     * 
     * @return Client IP address or "N/A"
     */
    private String getClientIp() {
        // TODO: Implement IP extraction from HTTP request if needed
        // This would require injecting HttpServletRequest
        return "N/A";
    }
}
