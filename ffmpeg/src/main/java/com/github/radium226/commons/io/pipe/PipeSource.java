package com.github.radium226.commons.io.pipe;

import java.io.InputStream;

public interface PipeSource<T extends InputStream> {

    T beginInputStream();

    default void endInputStream(T inputStream) {

    }

    static PipeSource<InputStream> existingInputStream(InputStream inputStream) {
        return existingInputStream(inputStream, PipeListener.ignore());
    }

    static PipeSource<InputStream> existingInputStream(InputStream inputStream, PipeListener listener) {
        return new PipeSource<InputStream>() {

            @Override
            public InputStream beginInputStream() {
                return inputStream;
            }

            @Override
            public void endInputStream(InputStream inputStream) {
                listener.inputStreamEnded(inputStream);
            }

        };
    }

}
