package com.github.radium226.video;

import com.google.common.base.MoreObjects;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by adrien on 1/29/17.
 */
public class VideoMetaData {

    final private static Logger LOGGER = LoggerFactory.getLogger(VideoMetaData.class);

    final private static String[] FFPROBE_COMMAND = new String[] { "ffprobe", "-v", "error", "-i", "-", "-show_entries", "stream=height,width" };

    public static class Builder {

        private int width = -1;
        private int height = -1;

        private Builder() {
            super();
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public VideoMetaData build() {
            return new VideoMetaData(this.width, this.height);
        }

        public boolean canBuild() {
            return width > -1 && height > -1;
        }

    }

    private int width;

    private int height;

    public VideoMetaData(int width, int height) {
        super();

        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public boolean equals(Object o) {
        boolean equality = false;
        if (o instanceof VideoMetaData) {
            VideoMetaData that = (VideoMetaData) o;
            equality = Objects.equals(this.width, that.width) && Objects.equals(this.height, that.height);
        }
        return equality;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.width, this.height);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("width", this.width)
                .add("height", this.height)
            .toString();
    }

    public static VideoMetaData of(byte[] byteArray) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(byteArray)) {
            return of(inputStream);
        }
    }

    public static VideoMetaData of(Path filePath) throws IOException {
        Process ffprobeProcess = new ProcessBuilder(new String[] { "ffprobe", "-v", "error", "-i", filePath.toString(), "-show_entries", "stream=height,width" })
                .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start();

        InputStream ffprobeProcessInputStream = ffprobeProcess.getInputStream();

        // We read the stdout of ffprobe and parse in order to obtain the VideoMetaData
        ExecutorService parseVideoMetaDataExecutor = Executors.newSingleThreadExecutor();
        Future<VideoMetaData> parseVideoMetaDataFuture = parseVideoMetaDataExecutor.submit(() -> {
            return CharStreams.readLines(new InputStreamReader(ffprobeProcessInputStream), new LineProcessor<VideoMetaData>() {

                private Builder builder = VideoMetaData.builder();

                @Override
                public boolean processLine(String line) throws IOException {
                    System.out.println("line=" + line);
                    String regex = "^(height|width)=([0-9]+)$";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        switch (matcher.group(1)) {
                            case "width":
                                builder.width(Integer.valueOf(matcher.group(2)));
                                break;
                            case "height":
                                builder.height(Integer.valueOf(matcher.group(2)));
                        }
                    }

                    return !builder.canBuild();
                }

                @Override
                public VideoMetaData getResult() {
                    return builder.build();
                }

            });
        });

        try {
            VideoMetaData videoMetaData = parseVideoMetaDataFuture.get(1, TimeUnit.MINUTES);
            LOGGER.debug("About to shutdown parseVideoMetaDataExecutor");
            parseVideoMetaDataExecutor.shutdown();

            try {
                int exitCode = ffprobeProcess.waitFor();
                if (exitCode > 0) {
                    throw new MetaDataException("The ffprobe process exited " + Integer.toString(exitCode));
                }
            } catch (IllegalThreadStateException e) {
                throw new MetaDataException("Something may be wrong with the provided InputStream", e);
            }

            return videoMetaData;
        } catch (TimeoutException e) {
            throw new MetaDataException("The parsing of the meta data took too much time", e);
        } catch (Exception e) {
            throw new MetaDataException("Something appended while parsing the meta data", e);
        }
    }

    public static VideoMetaData of(ByteBuffer byteBuffer) throws IOException {
        byte[] byteArray = new byte[byteBuffer.remaining()];
        byteBuffer.get(byteArray);
        return of(byteArray);
    }

    public static VideoMetaData of(InputStream inputStream) throws IOException {
        LOGGER.info("Starting ffprobe process... ");
        Process ffprobeProcess = new ProcessBuilder(FFPROBE_COMMAND)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start();

        // We retreive ffprobe input & output
        OutputStream ffprobeProcessOutputStream = ffprobeProcess.getOutputStream();
        InputStream ffprobeProcessInputStream = ffprobeProcess.getInputStream();

        // We copy byte by byte until ffprobe exits
        ExecutorService pipeToFFProbeExecutor = Executors.newSingleThreadExecutor();
        Future<Void> pipeToFFProbeFuture = pipeToFFProbeExecutor.submit(() -> {
            int readInt = -1;
            while (ffprobeProcess.isAlive() && (readInt = inputStream.read()) > -1) {
                ffprobeProcessOutputStream.write(readInt);
            }
            return null;
        });

        // We read the stdout of ffprobe and parse in order to obtain the VideoMetaData
        ExecutorService parseVideoMetaDataExecutor = Executors.newSingleThreadExecutor();
        Future<VideoMetaData> parseVideoMetaDataFuture = parseVideoMetaDataExecutor.submit(() -> {
            return CharStreams.readLines(new InputStreamReader(ffprobeProcessInputStream), new LineProcessor<VideoMetaData>() {

                private Builder builder = VideoMetaData.builder();

                @Override
                public boolean processLine(String line) throws IOException {
                    System.out.println("line=" + line);
                    String regex = "^(height|width)=([0-9]+)$";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        switch (matcher.group(1)) {
                            case "width":
                                builder.width(Integer.valueOf(matcher.group(2)));
                                break;
                            case "height":
                                builder.height(Integer.valueOf(matcher.group(2)));
                        }
                    }

                    return !builder.canBuild();
                }

                @Override
                public VideoMetaData getResult() {
                    return builder.build();
                }

            });
        });

        try {
            VideoMetaData videoMetaData = parseVideoMetaDataFuture.get(1, TimeUnit.MINUTES);
            LOGGER.debug("About to shutdown parseVideoMetaDataExecutor");
            parseVideoMetaDataExecutor.shutdown();

            // Let's cleanup what's remain
            //pipeToFFProbeFuture.get();
            LOGGER.debug("About to shutdown pipeToFFProbeExecutor");
            pipeToFFProbeExecutor.shutdown();

            try {
                int exitCode = ffprobeProcess.waitFor();
                if (exitCode > 0) {
                    throw new MetaDataException("The ffprobe process exited " + Integer.toString(exitCode));
                }
            } catch (IllegalThreadStateException e) {
                throw new MetaDataException("Something may be wrong with the provided InputStream", e);
            }

            return videoMetaData;
        } catch (TimeoutException e) {
            throw new MetaDataException("The parsing of the meta data took too much time", e);
        } catch (Exception e) {
            throw new MetaDataException("Something appended while parsing the meta data", e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

}
