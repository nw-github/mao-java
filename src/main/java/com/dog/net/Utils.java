package com.dog.net;

public class Utils {
    public static void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) { }
    }
}
