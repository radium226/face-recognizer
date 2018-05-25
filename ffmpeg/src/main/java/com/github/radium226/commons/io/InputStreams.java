package com.github.radium226.commons.io;

import com.github.radium226.commons.io.pipe.PipeFlow;
import com.github.radium226.commons.io.pipe.PipeSource;
import com.github.radium226.commons.io.pipe.Pipes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by adrien on 1/30/17.
 */
public class InputStreams {

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

    public static InputStream throughProcess(InputStream inputStream, Process process) {
        PipeFlow<OutputStream, InputStream> pipeFlow = PipeFlow.existingProcess(process);
        Pipes.pipe(PipeSource.existingInputStream(inputStream), pipeFlow);
        return pipeFlow.beginInputStream();
    }

}
