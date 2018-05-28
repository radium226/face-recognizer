package com.github.radium226.video.io;

import com.github.radium226.commons.io.InputStreamWithHeader;
import com.github.radium226.commons.io.InputStreams;
import com.github.radium226.video.VideoMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Created by adrien on 1/29/17.
 */
public class VideoFileInputStream extends AbstractVideoInputStream {

    final private static Logger LOGGER = LoggerFactory.getLogger(VideoFileInputStream.class);

    private Path filePath;

    private VideoMetaData metaData;

    private InputStream inputStreamThroughProcess = null;

    public VideoFileInputStream(Path filePath) throws IOException {
        super();

        this.filePath = filePath;
    }

    public void initInputStreamThroughProcess() throws IOException {
        LOGGER.debug("Initializing inputStreamThroughProcess");

        VideoMetaData metaData = getMetaData();
        LOGGER.debug("metaData={}", metaData);

        int width = metaData.getWidth();
        int height = metaData.getHeight();
        String[] ffmpegCommand = ffmpegCommand(filePath, width, height);

        Process ffmpegProcess = new ProcessBuilder()
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .command(ffmpegCommand)
            .start();

        inputStreamThroughProcess = ffmpegProcess.getInputStream();
    }

    private static String[] ffmpegCommand(Path filePath, int width, int height) {
        //width = 320;
        //height = 180;
        return new String[] {
                "ffmpeg",
                "-i", filePath.toString(),
                "-f", "image2pipe",
                "-s", width + "x" + height,
                "-pix_fmt", "bgr24",
                "-vcodec", "rawvideo",
                "-"
            };
    }

    public VideoMetaData getMetaData() throws IOException {
        if (metaData == null) {
            LOGGER.info("Getting metadata... ");
            metaData = VideoMetaData.of(filePath);
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
        //inputStreamWithHeader.close();
    }

}
