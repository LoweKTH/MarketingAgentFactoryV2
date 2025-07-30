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

        if (parts.length == 6) { // Assuming format: seconds minute hour dayOfMonth month dayOfWeek
            String seconds = parts[0];
            String minute = parts[1];
            String hour = parts[2];
            String dayOfMonth = parts[3];
            String month = parts[4];
            String dayOfWeek = parts[5];

            // Specific patterns for 6-field cron

            // "*/X * * * * *" -> Every X seconds
            if (seconds.startsWith("*/") && "*".equals(minute) && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int interval = Integer.parseInt(seconds.substring(2));
                    return String.format("Every %d seconds", interval);
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
            // "0 0 */X * * *" -> Every X hours (at 0 minutes and 0 seconds past the hour)
            if ("0".equals(seconds) && "0".equals(minute) && hour.startsWith("*/") && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int interval = Integer.parseInt(hour.substring(2));
                    return String.format("Every %d hours", interval);
                } catch (NumberFormatException e) {
                    // Fallback
                }
            }
            // "0 0 * * * *" -> Every hour (at 0 minutes and 0 seconds past the hour)
            if ("0".equals(seconds) && "0".equals(minute) && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                return "Every hour";
            }
            // "0 * * * * *" -> Every minute (at 0 seconds past the minute)
            if ("0".equals(seconds) && "*".equals(minute) && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                return "Every minute";
            }

            // "0 0 H * * *" -> Daily at H AM/PM (0 seconds, 0 minutes, H hour)
            if ("0".equals(seconds) && "0".equals(minute) && ! "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int hourNum = Integer.parseInt(hour);
                    if (hourNum >= 0 && hourNum <= 23) {
                        LocalTime time = LocalTime.of(hourNum, 0);
                        return "Daily at " + time.format(DateTimeFormatter.ofPattern("h a"));
                    }
                } catch (NumberFormatException e) {
                    // Fallback to generic below
                }
            }
            // "0 0 H */D * *" -> Every D days at H AM/PM (0 seconds, 0 minutes, H hour, every Dth day)
            if ("0".equals(seconds) && "0".equals(minute) && ! "*".equals(hour) && dayOfMonth.startsWith("*/") && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int hourNum = Integer.parseInt(hour);
                    int dayInterval = Integer.parseInt(dayOfMonth.substring(2));
                    if (hourNum >= 0 && hourNum <= 23) {
                        LocalTime time = LocalTime.of(hourNum, 0);
                        return String.format("Every %d days at %s", dayInterval, time.format(DateTimeFormatter.ofPattern("h a")));
                    }
                } catch (NumberFormatException e) {
                    // Fallback
                }
            }
            // "0 0 H D * *" -> On specific day of month D at H AM/PM
            if ("0".equals(seconds) && "0".equals(minute) && ! "*".equals(hour) && ! "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int hourNum = Integer.parseInt(hour);
                    int domNum = Integer.parseInt(dayOfMonth);
                    if (hourNum >= 0 && hourNum <= 23 && domNum >= 1 && domNum <= 31) {
                        LocalTime time = LocalTime.of(hourNum, 0);
                        return String.format("On day %d of the month at %s", domNum, time.format(DateTimeFormatter.ofPattern("h a")));
                    }
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

            // "*/X * * * *" -> Every X minutes
            if (minute.startsWith("*/") && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int interval = Integer.parseInt(minute.substring(2));
                    return String.format("Every %d minutes", interval);
                } catch (NumberFormatException e) {
                    // Fallback
                }
            }
            // "0 */X * * *" -> Every X hours (at 0 minutes past the hour)
            if ("0".equals(minute) && hour.startsWith("*/") && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int interval = Integer.parseInt(hour.substring(2));
                    return String.format("Every %d hours", interval);
                } catch (NumberFormatException e) {
                    // Fallback
                }
            }
            // "0 * * * *" -> Every hour (at 0 minutes past the hour)
            if ("0".equals(minute) && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                return "Every hour";
            }
            // "* * * * *" -> Every minute
            if ("*".equals(minute) && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                return "Every minute";
            }

            // "0 H * * *" -> Daily at H AM/PM
            if ("0".equals(minute) && ! "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int hourNum = Integer.parseInt(hour);
                    if (hourNum >= 0 && hourNum <= 23) {
                        LocalTime time = LocalTime.of(hourNum, 0);
                        return "Daily at " + time.format(DateTimeFormatter.ofPattern("h a"));
                    }
                } catch (NumberFormatException e) {
                    // Fallback
                }
            }
            // "0 H */D * *" -> Every D days at H AM/PM
            if ("0".equals(minute) && ! "*".equals(hour) && dayOfMonth.startsWith("*/") && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int hourNum = Integer.parseInt(hour);
                    int dayInterval = Integer.parseInt(dayOfMonth.substring(2));
                    if (hourNum >= 0 && hourNum <= 23) {
                        LocalTime time = LocalTime.of(hourNum, 0);
                        return String.format("Every %d days at %s", dayInterval, time.format(DateTimeFormatter.ofPattern("h a")));
                    }
                } catch (NumberFormatException e) {
                    // Fallback
                }
            }
            // "0 H D * *" -> On specific day of month D at H AM/PM
            if ("0".equals(minute) && ! "*".equals(hour) && ! "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int hourNum = Integer.parseInt(hour);
                    int domNum = Integer.parseInt(dayOfMonth);
                    if (hourNum >= 0 && hourNum <= 23 && domNum >= 1 && domNum <= 31) {
                        LocalTime time = LocalTime.of(hourNum, 0);
                        return String.format("On day %d of the month at %s", domNum, time.format(DateTimeFormatter.ofPattern("h a")));
                    }
                } catch (NumberFormatException e) {
                    // Fallback
                }
            }
        }

        // Fallback: return the original cron expression
        return "Cron: " + cronExpression;
    }
}