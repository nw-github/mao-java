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
}
