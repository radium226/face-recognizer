package com.github.radium226.commons.io.pipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface PipeFlow<O extends OutputStream, I extends InputStream> extends PipeSource<I>, PipeSink<O> {

    static PipeFlow<OutputStream, InputStream> existingProcess(Process process) {
        return new PipeFlow<OutputStream, InputStream>() {

            @Override
            public InputStream beginInputStream() {
                return process.getInputStream();
            }

            @Override
            public OutputStream beginOutputStream() {
                return process.getOutputStream();
            }

            @Override
            public void endOutputStream(OutputStream outputStream) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }

        };
    }


}
