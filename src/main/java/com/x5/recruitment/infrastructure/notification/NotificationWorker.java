package com.x5.recruitment.infrastructure.notification;

import com.x5.recruitment.domain.model.Notification;
import com.x5.recruitment.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Background worker for processing notification outbox.
 * Implements the outbox pattern for reliable async notifications.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWorker {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    private static final int MAX_ATTEMPTS = 3;

    /**
     * Process unsent notifications every 30 seconds.
     */
    @Scheduled(fixedDelay = 30000) // 30 seconds
    @Transactional
    public void processNotifications() {
        List<Notification> pending = notificationRepository
            .findBySentFalseAndAttemptsLessThan(MAX_ATTEMPTS);

        if (pending.isEmpty()) {
            return;
        }

        log.info("Processing {} pending notifications", pending.size());

        for (Notification notification : pending) {
            try {
                emailService.sendEmail(
                    notification.getCandidate().getEmail(),
                    notification.getSubject(),
                    notification.getBody()
                );

                notification.markAsSent();
                notificationRepository.save(notification);
                
                log.debug("Sent notification {} to {}", 
                    notification.getId(), notification.getCandidate().getEmail());
                    
            } catch (Exception e) {
                log.error("Failed to send notification {}: {}", 
                    notification.getId(), e.getMessage());
                
                notification.recordError(e.getMessage());
                notificationRepository.save(notification);
            }
        }

        log.info("Processed {} notifications", pending.size());
    }
}
