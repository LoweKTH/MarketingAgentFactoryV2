// src/main/java/com/exjobb/backend/util/CronExpressionTranslator.java
package com.exjobb.backend.utils;

import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.time.LocalTime;

@Component
public class CronExpressionTranslator {

    public String translate(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            return "Unknown Schedule";
        }

        String[] parts = cronExpression.trim().split("\\s+");

        // Cron expressions typically have 5 or 6 parts (min hour dom month dow [year])
        // If your cron library supports 6 parts (with year), adjust this.
        // Standard cron (Unix, Spring's CronTrigger) is 5 or 6 fields.
        // Your example "0 0 */12 * * *" is 6 fields, implying a seconds field at the start.
        // Let's assume the cron string is "seconds minute hour dayOfMonth month dayOfWeek" (6 fields)
        // or "minute hour dayOfMonth month dayOfWeek" (5 fields).
        // The JSON provided "0 0 */12 * * *" looks like a 6-field cron.
        // Let's adjust based on this common interpretation where the first "0" is seconds.

        if (parts.length == 6) { // Assuming format: seconds minute hour dayOfMonth month dayOfWeek
            String seconds = parts[0];
            String minute = parts[1];
            String hour = parts[2];
            String dayOfMonth = parts[3];
            String month = parts[4];
            String dayOfWeek = parts[5];

            // Specific patterns for 6-field cron
            // "0 0 */12 * * *" -> Every 12 hours (0 seconds, 0 minutes, every 12th hour)
            if ("0".equals(seconds) && "0".equals(minute) && "*/12".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                return "Every 12 hours";
            }
            // "0 0 * * * *" -> Every hour (at 0 minutes and 0 seconds past the hour)
            if ("0".equals(seconds) && "0".equals(minute) && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                return "Every hour";
            }
            // "0 * * * * *" -> Every minute (at 0 seconds past the minute)
            if ("0".equals(seconds) && "*".equals(minute) && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                return "Every minute";
            }
            // "0 0 9 * * *" -> Daily at 9 AM (0 seconds, 0 minutes, 9th hour)
            if ("0".equals(seconds) && "0".equals(minute) && ! "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int hourNum = Integer.parseInt(hour);
                    LocalTime time = LocalTime.of(hourNum, 0);
                    return "Daily at " + time.format(DateTimeFormatter.ofPattern("h a"));
                } catch (NumberFormatException e) {
                    // Fallback to generic below
                }
            }
            // "0 */X * * * *" -> Every X minutes (at 0 seconds past the minute)
            if ("0".equals(seconds) && minute.startsWith("*/") && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int interval = Integer.parseInt(minute.substring(2));
                    return String.format("Every %d minutes", interval);
                } catch (NumberFormatException e) {
                    // Fallback
                }
            }


        } else if (parts.length == 5) { // Standard Unix/Linux cron format: minute hour dayOfMonth month dayOfWeek
            String minute = parts[0];
            String hour = parts[1];
            String dayOfMonth = parts[2];
            String month = parts[3];
            String dayOfWeek = parts[4];

            // Specific patterns for 5-field cron
            // This case won't match "0 0 */12 * * *" as it has 6 fields.
            // If you expect 5-field crons, add their specific rules here.
            // Example: "0 */12 * * *" -> Every 12 hours
            if ("0".equals(minute) && "*/12".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                return "Every 12 hours";
            }
            // "0 * * * *" -> Every hour (at 0 minutes past the hour)
            if ("0".equals(minute) && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                return "Every hour";
            }
            // "* * * * *" -> Every minute
            if ("*".equals(minute) && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                return "Every minute";
            }
            // "0 9 * * *" -> Daily at 9 AM
            if ("0".equals(minute) && ! "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int hourNum = Integer.parseInt(hour);
                    LocalTime time = LocalTime.of(hourNum, 0);
                    return "Daily at " + time.format(DateTimeFormatter.ofPattern("h a"));
                } catch (NumberFormatException e) {
                    // Fallback
                }
            }
            // "*/X * * * *" -> Every X minutes
            if (minute.startsWith("*/") && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int interval = Integer.parseInt(minute.substring(2));
                    return String.format("Every %d minutes", interval);
                } catch (NumberFormatException e) {
                    // Fallback
                }
            }
        }

        // Fallback: return the original cron expression
        return "Cron: " + cronExpression;
    }
}