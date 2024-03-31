package com.example.readerapp.utils;

import android.util.Log;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class HelperFunctions {

    public static String timestampToDate(String timestamp, String datePattern) {
        long timestampNumber = Long.parseLong(timestamp);
        Instant instant = Instant.ofEpochSecond(timestampNumber);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePattern);
        return dateTime.format(formatter);
    }

    public static String adjustByteSizeString(String bytesString) {
        double bytes = Double.parseDouble(bytesString);
        if (bytes < 1024) {
            return  roundToOneDecimal(bytes) + " B";
        }

        double kilobytes = bytes / 1024;
        if (kilobytes < 1024) {
            return roundToOneDecimal(kilobytes) + " KB";
        }

        double megabytes = kilobytes / 1024;
        Log.d("MyLogs", "MEGABYTES: " + megabytes);
        if (megabytes < 1024) {
            return roundToOneDecimal(megabytes) + " MB";
        } else {
            return roundToOneDecimal(megabytes/1024) + " GB";
        }
    }

    public static double roundToOneDecimal(double number) {
        return (double) Math.round(number * 10) / 10;
    }

}
