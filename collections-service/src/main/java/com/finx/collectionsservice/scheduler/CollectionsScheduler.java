package com.finx.collectionsservice.scheduler;

import com.finx.collectionsservice.service.OTSService;
import com.finx.collectionsservice.service.PTPService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Scheduled jobs for Collections Service
 *
 * Cron Expression Format: second minute hour day-of-month month day-of-week
 * Examples:
 * - "0 0 6 * * *" = Every day at 6:00 AM
 * - "0 0 0 * * *" = Every day at midnight
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CollectionsScheduler {

    private final OTSService otsService;
    private final PTPService ptpService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Process expired OTS requests
     * Runs daily at 6:00 AM IST
     *
     * This job finds all OTS requests where:
     * - Status is APPROVED or PENDING_APPROVAL
     * - Payment deadline has passed
     * And marks them as EXPIRED
     */
    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Kolkata")
    public void processExpiredOTS() {
        log.info("=== OTS Expiry Job Started at {} ===", LocalDateTime.now().format(formatter));

        try {
            Integer expiredCount = otsService.processExpiredOTS();
            log.info("OTS Expiry Job completed. Expired {} OTS requests", expiredCount);
        } catch (Exception e) {
            log.error("OTS Expiry Job failed: {}", e.getMessage(), e);
        }

        log.info("=== OTS Expiry Job Ended at {} ===", LocalDateTime.now().format(formatter));
    }

    /**
     * Process overdue PTP commitments
     * Runs daily at 7:00 AM IST
     *
     * This job finds all PTP commitments where:
     * - Status is SCHEDULED
     * - PTP date has passed (overdue)
     * And marks them as BROKEN with reason "Auto-marked: Payment not received by
     * due date"
     */
    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Kolkata")
    public void processOverduePTPs() {
        log.info("=== PTP Overdue Job Started at {} ===", LocalDateTime.now().format(formatter));

        try {
            Integer brokenCount = ptpService.processOverduePTPs();
            log.info("PTP Overdue Job completed. Marked {} PTPs as broken", brokenCount);
        } catch (Exception e) {
            log.error("PTP Overdue Job failed: {}", e.getMessage(), e);
        }

        log.info("=== PTP Overdue Job Ended at {} ===", LocalDateTime.now().format(formatter));
    }

    /**
     * Send PTP reminder notifications
     * Runs daily at 9:00 AM IST
     *
     * This job finds all PTP commitments where:
     * - Status is SCHEDULED
     * - PTP date is tomorrow or today
     * - Reminder has not been sent yet
     * And sends reminder notifications
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    public void sendPTPReminders() {
        log.info("=== PTP Reminder Job Started at {} ===", LocalDateTime.now().format(formatter));

        try {
            Integer reminderCount = ptpService.sendPTPReminders();
            log.info("PTP Reminder Job completed. Sent {} reminders", reminderCount);
        } catch (Exception e) {
            log.error("PTP Reminder Job failed: {}", e.getMessage(), e);
        }

        log.info("=== PTP Reminder Job Ended at {} ===", LocalDateTime.now().format(formatter));
    }

    /**
     * Generate daily collections summary report
     * Runs daily at 11:00 PM IST
     */
    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Kolkata")
    public void generateDailySummary() {
        log.info("=== Daily Summary Job Started at {} ===", LocalDateTime.now().format(formatter));

        try {
            // TODO: Implement daily summary generation
            // This could generate reports, send email summaries, etc.
            log.info("Daily Summary Job completed");
        } catch (Exception e) {
            log.error("Daily Summary Job failed: {}", e.getMessage(), e);
        }

        log.info("=== Daily Summary Job Ended at {} ===", LocalDateTime.now().format(formatter));
    }
}
