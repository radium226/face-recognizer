package com.github.radium226.commons.io.pipe;

import java.util.function.BiFunction;

public interface PipeReport {

    static PipeReport empty() {
        return new PipeReport() {

        };
    }

    static BiFunction<PipeReport, PipeReport, PipeReport> combine() {
        return (one, other) -> PipeReport.empty();
    }

}
