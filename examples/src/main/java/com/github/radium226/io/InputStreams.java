package com.github.radium226.io;

import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by adrien on 1/30/17.
 */
public class InputStreams {

    final public static int THROUGH_PROCESS_BYTE_BUFFER_SIZE = 1024 * 128; // 128Ko
    final public static Logger LOGGER = LoggerFactory.getLogger(InputStreams.class);

    public static InputStreamWithHeader withHeader(InputStream inputStream, int expectedHeaderSize) throws IOException {
        byte[] expectedHeaderByteArray = new byte[expectedHeaderSize];
        int n = 0;
        int actualHeaderSize = 0;
        while (n < expectedHeaderSize) {
            int readByteCount = inputStream.read(expectedHeaderByteArray, n, expectedHeaderSize - n);
            if (readByteCount < 0) {
                break;
            }
            n += readByteCount;
        }
        actualHeaderSize = n;
        byte[] actualHeaderByteArray = new byte[actualHeaderSize];
        System.arraycopy(expectedHeaderByteArray, 0, actualHeaderByteArray, 0, actualHeaderSize);

        return new InputStreamWithHeader(inputStream, actualHeaderByteArray);
    }

    public static InputStream throughProcess(InputStream inputStream, String[] processCommand) throws IOException {
        return throughProcess(inputStream, processCommand, THROUGH_PROCESS_BYTE_BUFFER_SIZE, Optional.empty());
    }

    public static InputStream throughProcess(InputStream inputStream, String[] processCommand, int byteBufferSize, Optional<RateLimiter> maybeRateLimiter) throws IOException {
        Process process = new ProcessBuilder(processCommand)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start();

        OutputStream processOutputStream = process.getOutputStream();

        ExecutorService writeToProcessExecutor = Executors.newSingleThreadExecutor();
        Future<Void> writeToProcessFuture = writeToProcessExecutor.submit(() -> {
            byte[] byteBuffer = new byte[byteBufferSize];
            int readByteCount = -1;
            while ((readByteCount = inputStream.read(byteBuffer)) > 0) {
                maybeRateLimiter.ifPresent((rateLimiter) -> rateLimiter.acquire());
                LOGGER.trace("Writing readByteCount={} to processCommand={}", readByteCount, processCommand);
                try {
                    processOutputStream.write(byteBuffer, 0, readByteCount);
                    processOutputStream.flush();
                } catch (IOException e) {
                    LOGGER.trace("The outputStream of the process {} may already be closed", processCommand, e);
                    break;
                }
            }
            return null;
        });

        InputStream processInputStream = process.getInputStream();
        return new InputStreamWithCloseHandler(processInputStream, (inputStreamToClose) -> {
            processOutputStream.close();

            try {
                writeToProcessFuture.get();
                writeToProcessExecutor.shutdown();
            } catch (ExecutionException | InterruptedException e) {
                throw new IOException(e);
            }

            processInputStream.close();
            inputStream.close();
        });
    }

}
