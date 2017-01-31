package com.github.radium226.io;

import com.github.radium226.VideoMetaData;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by adrien on 1/29/17.
 */
public class VideoInputStream extends InputStream {

    final public static int HEADER_SIZE = 1024 * 1024;

    private InputStream inputStream;

    private InputStreamWithHeader inputStreamWithHeader = null;
    private InputStream inputStreamThroughProcess = null;
    private VideoMetaData metaData;

    public VideoInputStream(InputStream inputStream) throws IOException {
        super();

        this.inputStream = inputStream;
    }

    public void initInputStreamThroughProcess() throws IOException {
        VideoMetaData metaData = getMetaData();

        int width = metaData.getWidth();
        int height = metaData.getHeight();
        String[] ffmpegCommand = ffmpegCommand(width, height);

        inputStreamThroughProcess = InputStreams.throughProcess(inputStreamWithHeader, ffmpegCommand);
    }

    private static String[] ffmpegCommand(int width, int height) {
        return new String[] {
                "ffmpeg",
                "-i", "-",
                "-f", "image2pipe",
                "-s", width + "x" + height,
                "-pix_fmt", "bgr24",
                "-vcodec", "rawvideo",
                "-"
            };
    }

    public VideoMetaData getMetaData() throws IOException {
        if (inputStreamWithHeader == null) {
            inputStreamWithHeader = InputStreams.withHeader(inputStream, HEADER_SIZE);
            metaData = VideoMetaData.of(inputStreamWithHeader.getHeader());
        }

        return metaData;
    }

    public int read() throws IOException {
        if (inputStreamThroughProcess == null) {
            initInputStreamThroughProcess();
        }

        return inputStreamThroughProcess.read();
    }

    public int read(byte[] bytes) throws IOException {
        if (inputStreamThroughProcess == null) {
            initInputStreamThroughProcess();
        }

        return inputStreamThroughProcess.read(bytes);
    }

    public int read(byte[] bytes, int i, int i1) throws IOException {
        if (inputStreamThroughProcess == null) {
            initInputStreamThroughProcess();
        }

        return inputStreamThroughProcess.read(bytes, i, i1);
    }

    public long skip(long l) throws IOException {
        if (inputStreamThroughProcess == null) {
            initInputStreamThroughProcess();
        }

        return inputStreamThroughProcess.skip(l);
    }

    public int available() throws IOException {
        if (inputStreamThroughProcess == null) {
            initInputStreamThroughProcess();
        }

        return inputStreamThroughProcess.available();
    }

    public void close() throws IOException {
        inputStreamWithHeader.close();
    }

}
