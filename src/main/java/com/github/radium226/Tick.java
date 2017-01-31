package com.github.radium226;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by adrien on 1/26/17.
 */
public class Tick {

    @FunctionalInterface
    public static interface Handler {

        public boolean onTick() throws Exception;

    }

    public static void every(long delay, Handler handler) throws Exception {
        while (true) {
            long start = System.nanoTime();
            if (!handler.onTick()) {
                break;
            }
            while(start + delay >= System.nanoTime());
        }
    }

}
