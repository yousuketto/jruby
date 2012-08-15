package org.jruby.ir.persistence.read;

import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;


public enum IRReadingContext {
    INSTANCE;
    
    private ThreadLocal<String> fileNameLocal = new ThreadLocal<String>();
    private StopWatch watch;
    private long totalTime;

    public void setFileName(String fileName) {
        fileNameLocal.set(fileName);
    }
    
    public String getFileName() {
        return fileNameLocal.get();
    }
    
    public void start(String label) {
        if (watch == null) {
            watch = new LoggingStopWatch(label);
        } else {
            watch.start(label);
        }
    }
    
    public void stop() {
        watch.stop();
        totalTime += watch.getElapsedTime();
    }
    
    public long getTotalTime() {
        return totalTime;
    }
    
}
