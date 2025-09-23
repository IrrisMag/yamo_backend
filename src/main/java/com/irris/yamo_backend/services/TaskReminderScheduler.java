package com.irris.yamo_backend.services;

import com.irris.yamo_backend.entities.Task;
import com.irris.yamo_backend.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class TaskReminderScheduler {
    private static final Logger log = LoggerFactory.getLogger(TaskReminderScheduler.class);

    private final TaskRepository taskRepository;
    private final WhatsAppNotificationService waService;

    // runs every minute
    @Scheduled(cron = "0 * * * * *")
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime inOneHour = now.plusHours(1);
        List<Task> upcoming = taskRepository.findByScheduledAtBetween(now, inOneHour);
        for (Task t : upcoming) {
            if (t.getStatus() != Task.TaskStatus.SCHEDULED) continue;
            int remindBefore = t.getRemindBeforeMinutes() == null ? 30 : t.getRemindBeforeMinutes();
            LocalDateTime reminderTime = t.getScheduledAt().minusMinutes(remindBefore);
            if (reminderTime.isBefore(now.plusMinutes(1)) && reminderTime.isAfter(now.minusMinutes(1))) {
                notifyParticipants(t);
            }
        }
    }

    private void notifyParticipants(Task t) {
        String msg = String.format("Rappel de %s à %s: %s. Adresse: %s",
                t.getType(), t.getScheduledAt(), t.getTitle(), t.getAddressLine());
        if (t.getParticipantsPhones() != null) {
            for (String p : t.getParticipantsPhones()) {
                try {
                    waService.sendTextMessage(p, msg);
                } catch (Exception ex) {
                    log.warn("Failed to send reminder to {}: {}", p, ex.getMessage());
                }
            }
        }
        if (t.getAssigneeLivreur() != null && t.getAssigneeLivreur().getPhone() != null) {
            try {
                waService.sendTextMessage(t.getAssigneeLivreur().getPhone(), msg);
            } catch (Exception ex) {
                log.warn("Failed to send reminder to livreur {}: {}", t.getAssigneeLivreur().getId(), ex.getMessage());
            }
        }
    }
}
