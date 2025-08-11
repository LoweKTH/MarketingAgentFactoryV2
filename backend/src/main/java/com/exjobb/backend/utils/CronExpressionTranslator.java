
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

        if (parts.length == 6) {
            String seconds = parts[0];
            String minute = parts[1];
            String hour = parts[2];
            String dayOfMonth = parts[3];
            String month = parts[4];
            String dayOfWeek = parts[5];


            if (seconds.startsWith("*/") && "*".equals(minute) && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int interval = Integer.parseInt(seconds.substring(2));
                    return String.format("Every %d seconds", interval);
                } catch (NumberFormatException e) {

                }
            }

            if ("0".equals(seconds) && minute.startsWith("*/") && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int interval = Integer.parseInt(minute.substring(2));
                    return String.format("Every %d minutes", interval);
                } catch (NumberFormatException e) {

                }
            }

            if ("0".equals(seconds) && "0".equals(minute) && hour.startsWith("*/") && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int interval = Integer.parseInt(hour.substring(2));
                    return String.format("Every %d hours", interval);
                } catch (NumberFormatException e) {

                }
            }
            if ("0".equals(seconds) && "0".equals(minute) && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                return "Every hour";
            }
            if ("0".equals(seconds) && "*".equals(minute) && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                return "Every minute";
            }

            if ("0".equals(seconds) && "0".equals(minute) && ! "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int hourNum = Integer.parseInt(hour);
                    if (hourNum >= 0 && hourNum <= 23) {
                        LocalTime time = LocalTime.of(hourNum, 0);
                        return "Daily at " + time.format(DateTimeFormatter.ofPattern("h a"));
                    }
                } catch (NumberFormatException e) {
                }
            }
            if ("0".equals(seconds) && "0".equals(minute) && ! "*".equals(hour) && dayOfMonth.startsWith("*/") && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int hourNum = Integer.parseInt(hour);
                    int dayInterval = Integer.parseInt(dayOfMonth.substring(2));
                    if (hourNum >= 0 && hourNum <= 23) {
                        LocalTime time = LocalTime.of(hourNum, 0);
                        return String.format("Every %d days at %s", dayInterval, time.format(DateTimeFormatter.ofPattern("h a")));
                    }
                } catch (NumberFormatException e) {

                }
            }
            if ("0".equals(seconds) && "0".equals(minute) && ! "*".equals(hour) && ! "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int hourNum = Integer.parseInt(hour);
                    int domNum = Integer.parseInt(dayOfMonth);
                    if (hourNum >= 0 && hourNum <= 23 && domNum >= 1 && domNum <= 31) {
                        LocalTime time = LocalTime.of(hourNum, 0);
                        return String.format("On day %d of the month at %s", domNum, time.format(DateTimeFormatter.ofPattern("h a")));
                    }
                } catch (NumberFormatException e) {
                }
            }


        } else if (parts.length == 5) {
            String minute = parts[0];
            String hour = parts[1];
            String dayOfMonth = parts[2];
            String month = parts[3];
            String dayOfWeek = parts[4];

            if (minute.startsWith("*/") && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int interval = Integer.parseInt(minute.substring(2));
                    return String.format("Every %d minutes", interval);
                } catch (NumberFormatException e) {

                }
            }
            if ("0".equals(minute) && hour.startsWith("*/") && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int interval = Integer.parseInt(hour.substring(2));
                    return String.format("Every %d hours", interval);
                } catch (NumberFormatException e) {

                }
            }
            if ("0".equals(minute) && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                return "Every hour";
            }
            if ("*".equals(minute) && "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                return "Every minute";
            }

            if ("0".equals(minute) && ! "*".equals(hour) && "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int hourNum = Integer.parseInt(hour);
                    if (hourNum >= 0 && hourNum <= 23) {
                        LocalTime time = LocalTime.of(hourNum, 0);
                        return "Daily at " + time.format(DateTimeFormatter.ofPattern("h a"));
                    }
                } catch (NumberFormatException e) {
                }
            }
            if ("0".equals(minute) && ! "*".equals(hour) && dayOfMonth.startsWith("*/") && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int hourNum = Integer.parseInt(hour);
                    int dayInterval = Integer.parseInt(dayOfMonth.substring(2));
                    if (hourNum >= 0 && hourNum <= 23) {
                        LocalTime time = LocalTime.of(hourNum, 0);
                        return String.format("Every %d days at %s", dayInterval, time.format(DateTimeFormatter.ofPattern("h a")));
                    }
                } catch (NumberFormatException e) {

                }
            }
            if ("0".equals(minute) && ! "*".equals(hour) && ! "*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
                try {
                    int hourNum = Integer.parseInt(hour);
                    int domNum = Integer.parseInt(dayOfMonth);
                    if (hourNum >= 0 && hourNum <= 23 && domNum >= 1 && domNum <= 31) {
                        LocalTime time = LocalTime.of(hourNum, 0);
                        return String.format("On day %d of the month at %s", domNum, time.format(DateTimeFormatter.ofPattern("h a")));
                    }
                } catch (NumberFormatException e) {
                }
            }
        }

        return "Cron: " + cronExpression;
    }
}