package com.example.affinity.affinity.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class Helpers {

    private static final Map<String, Integer> MONTH_MAP = new HashMap<>();

    static {
        MONTH_MAP.put("JAN", 1);
        MONTH_MAP.put("FEB", 2);
        MONTH_MAP.put("MAR", 3);
        MONTH_MAP.put("APR", 4);
        MONTH_MAP.put("MAY", 5);
        MONTH_MAP.put("JUN", 6);
        MONTH_MAP.put("JUL", 7);
        MONTH_MAP.put("AUG", 8);
        MONTH_MAP.put("SEP", 9);
        MONTH_MAP.put("OCT", 10);
        MONTH_MAP.put("NOV", 11);
        MONTH_MAP.put("DEC", 12);
    }

    public static int convertMonthToNumber(String monthAbbreviation) {
        if (monthAbbreviation == null || monthAbbreviation.length() != 3) {
            throw new IllegalArgumentException("Invalid month abbreviation: " + monthAbbreviation);
        }

        Integer monthNumber = MONTH_MAP.get(monthAbbreviation.toUpperCase());
        if (monthNumber == null) {
            throw new IllegalArgumentException("Invalid month abbreviation: " + monthAbbreviation);
        }

        return monthNumber;
    }

    public static String getCompanyInvoiceFileName(String month, String year, String companyName) {
        int monthNumber = convertMonthToNumber(month);
        return "invoice-" + companyName + "-" + year + "-" + monthNumber + ".xlsx";
    }

    public static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    public static HashMap<String, Object> setFileName(String fileName) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("file-name", fileName);
        response.put("status", "OK");
        return response;
    }

    public List<List<String>> parseCSV(String csvData) {
        List<List<String>> records = new ArrayList<>();
        try (Scanner scanner = new Scanner(csvData)) {
            // Skip the first line (headers)
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] values = line.split(",");
                records.add(List.of(values));
            }
        }
        return records;
    }

    public static float calculateHours(String startTime, String endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");

        // Parse the time strings into LocalTime
        LocalTime start = LocalTime.parse(startTime, formatter);
        LocalTime end = LocalTime.parse(endTime, formatter);

        // Calculate the duration in hours
        long minutes = Duration.between(start, end).toMinutes();
        return minutes / 60.0f; // Convert minutes to hours
    }

    public static float calculateCost(Float hours, Float rate) {
        return BigDecimal
                .valueOf(hours * rate)
                .setScale(2, RoundingMode.HALF_UP)
                .floatValue();
    }

    public static boolean isWithinCurrentOrPreviousMonth(Date date) {
        // Convert java.util.Date to LocalDate
        LocalDate givenDate = date.toLocalDate();

        // Get current and previous months
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);
        YearMonth givenMonth = YearMonth.from(givenDate);

        // Check if the given date is in the current or previous month
        return givenMonth.equals(currentMonth) || givenMonth.equals(previousMonth);
    }

}
