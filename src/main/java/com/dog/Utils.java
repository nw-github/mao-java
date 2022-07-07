package com.dog;

public class Utils {
    /* Silent sleep */
    public static void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) { }
    }

    /* Silent close */
    public static void close(AutoCloseable obj) {
        try {
            obj.close();
        } catch (Throwable th) { }
    }

    public static int clamp(int min, int max, int value) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    public static String toTitleCase(String text) {
        var builder = new StringBuilder();
        var split   = text.split(" ");
        for (int i = 0; i < split.length; i++) {
            builder.append(split[i].substring(0, 1).toUpperCase());
            builder.append(split[i].substring(1).toLowerCase());
            if (i != split.length - 1)
                builder.append(" ");
        }

        return builder.toString();
    }

    public static boolean endsWithAny(String source, String... suffixes) {
        for (var suffix : suffixes)
            if (source.endsWith(suffix))
                return true;
        return false;
    }
}
