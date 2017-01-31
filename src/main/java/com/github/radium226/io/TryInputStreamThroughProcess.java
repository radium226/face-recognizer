package com.github.radium226.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by adrien on 1/30/17.
 */
public class TryInputStreamThroughProcess {

    final public static Logger LOGGER = LoggerFactory.getLogger(TryInputStreamThroughProcess.class);

    public static void main(String[] arguments) throws IOException {
        tryWithZeroInputStreamAndCatProcess();
        tryWithZeroInputStreamAndSleepProcess();
    }

    public static void tryWithZeroInputStreamAndCatProcess() throws IOException {
        try (InputStream catInputStream = InputStreams.throughProcess(Files.newInputStream(Paths.get("/dev/zero")), new String[] { "cat" })) {
            int readByteCount = -1;
            int byteBufferSize = 1024 * 128; // 128Ko
            int maxTotalReadByteCount = 1024 * 1024 * 5; // 5Mo
            int totalReadByteCount = 0;
            byte[] byteBuffer = new byte[byteBufferSize];
            while ((readByteCount = catInputStream.read(byteBuffer)) > 0 && totalReadByteCount < maxTotalReadByteCount) {
                LOGGER.trace("readByteCount={}", readByteCount);
                totalReadByteCount += readByteCount;
            }
        }
    }

    public static void tryWithZeroInputStreamAndSleepProcess() throws IOException {
        try (InputStream catInputStream = InputStreams.throughProcess(Files.newInputStream(Paths.get("/dev/zero")), new String[] { "sleep", "5" })) {
            int readByteCount = -1;
            int byteBufferSize = 1024 * 128; // 128Ko
            int maxTotalReadByteCount = 1024 * 1024 * 5; // 5Mo
            int totalReadByteCount = 0;
            byte[] byteBuffer = new byte[byteBufferSize];
            while ((readByteCount = catInputStream.read(byteBuffer)) > 0 && totalReadByteCount < maxTotalReadByteCount) {
                LOGGER.trace("readByteCount={}", readByteCount);
                totalReadByteCount += readByteCount;
            }
        }
    }

}
