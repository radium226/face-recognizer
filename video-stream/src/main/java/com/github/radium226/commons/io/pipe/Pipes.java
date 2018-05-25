package com.github.radium226.commons.io.pipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

public class Pipes {

    final private static Logger LOGGER = LoggerFactory.getLogger(Pipes.class);

    final public static int PIPE_BUFFER_SIZE = 128 * 1024;

    private Pipes() {
        super();
    }

    public static CompletableFuture<PipeReport> pipe(InputStream inputStream, Process process, OutputStream outputStream) {
        return pipe(PipeSource.existingInputStream(inputStream), PipeFlow.existingProcess(process), PipeSink.existingOutputStream(outputStream));
    }

    public static CompletableFuture<PipeReport> pipe(InputStream inputStream, OutputStream outputStream) {
        return pipe(PipeSource.existingInputStream(inputStream), PipeSink.existingOutputStream(outputStream));
    }

    public static <I extends InputStream> I through(PipeSource<InputStream> source, PipeFlow<OutputStream, I> flow) {
        CompletableFuture<PipeReport> reportFuture = pipe(source, flow);
        return flow.beginInputStream();
    }

    public static <I extends InputStream> I through(InputStream inputStream, PipeFlow<OutputStream, I> flow) {
        return through(PipeSource.existingInputStream(inputStream), flow);
    }

    public static CompletableFuture<PipeReport> pipe(PipeSource<InputStream> source, PipeSink<OutputStream> sink) {
        InputStream sourceInputStream = source.beginInputStream();
        OutputStream sinkOutputStream = sink.beginOutputStream();

        CompletableFuture<PipeReport> pipeReportFuture = new CompletableFuture<>();
        Thread pipeThread = new Thread(() -> {
            byte[] readBytes = new byte[PIPE_BUFFER_SIZE];
            int readByteCount = 0;
            try {
                while (true) {
                    readByteCount = sourceInputStream.read(readBytes);
                    if (readByteCount < 0) {
                        break;
                    }

                    //System.out.println("readByteCount=" + readByteCount);
                    sinkOutputStream.write(readBytes, 0, readByteCount);
                }
            } catch (IOException exception) {
                LOGGER.error("There was an issue while piping InputStream to OutputStream", exception);
            } finally {
                sink.endOutputStream(sinkOutputStream);
                source.endInputStream(sourceInputStream);
                pipeReportFuture.complete(PipeReport.empty()); // TODO We may need to hande failure
            }
        });

        pipeThread.start();
        return pipeReportFuture;
    }

    public static CompletableFuture<PipeReport> pipe(PipeSource<InputStream> source, PipeFlow<OutputStream, InputStream> flow, PipeSink<OutputStream> sink) {
        return pipe(source, flow).thenCombineAsync(pipe(flow, sink), PipeReport.combine());
    }

}
