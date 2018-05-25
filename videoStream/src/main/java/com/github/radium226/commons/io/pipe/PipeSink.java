package com.github.radium226.commons.io.pipe;

import java.io.OutputStream;

public interface PipeSink<T extends OutputStream> {

    T beginOutputStream();

    default void endOutputStream(T outputStream) {

    }

    static PipeSink<OutputStream> existingOutputStream(OutputStream outputStream) {
        return PipeSink.existingOutputStream(outputStream, PipeListener.ignore());
    }

    static PipeSink<OutputStream> existingOutputStream(OutputStream outputStream, PipeListener listener) {
        return new PipeSink<OutputStream>() {

            @Override
            public OutputStream beginOutputStream() {
                return outputStream;
            }

            @Override
            public void endOutputStream(OutputStream outputStream) {
                listener.outputStreamEnded(outputStream);
            }

        };
    }

}
