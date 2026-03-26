package com.dormrepair.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class OrderNoUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OrderNoUtils() {
    }

    public static String nextOrderNo() {
        return "BX" + LocalDateTime.now().format(FORMATTER) + ThreadLocalRandom.current().nextInt(100, 999);
    }
}
