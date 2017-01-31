package com.github.radium226;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by adrien on 1/26/17.
 */
public class Processes {

    private Processes() {
        super();
    }

    public static void pipe(Process leftProcess, Process rightProcess) throws IOException {
        Executors.newSingleThreadExecutor().submit(() -> {
            ByteStreams.copy(leftProcess.getInputStream(), rightProcess.getOutputStream());
            return null;
        });

    }

}
