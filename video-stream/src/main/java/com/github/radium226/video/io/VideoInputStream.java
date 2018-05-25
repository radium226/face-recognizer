package com.github.radium226.video.io;

import com.github.radium226.commons.io.InputStreamWithHeader;
import com.github.radium226.commons.io.InputStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by adrien on 1/29/17.
 */
public class VideoInputStream extends InputStream {

    final private static Logger LOGGER = LoggerFactory.getLogger(VideoInputStream.class);

    final public static int HEADER_SIZE = 1024 * 1024; // 1MB

    private InputStream inputStream;

    private InputStreamWithHeader inputStreamWithHeader = null;
    private InputStream inputStreamThroughProcess = null;
    private VideoMetaData metaData;

    public VideoInputStream(InputStream inputStream) throws IOException {
        super();

        this.inputStream = inputStream;
    }

    public void initInputStreamThroughProcess() throws IOException {
        LOGGER.debug("Initializing inputStreamThroughProcess");

        VideoMetaData metaData = getMetaData();
        LOGGER.debug("metaData={}", metaData);

        int width = metaData.getWidth();
        int height = metaData.getHeight();
        String[] ffmpegCommand = ffmpegCommand(width, height);

        Process ffmpegProcess = new ProcessBuilder()
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .command(ffmpegCommand)
            .start();

        inputStreamThroughProcess = InputStreams.throughProcess(inputStreamWithHeader, ffmpegProcess);
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
