package org.jruby.ir.persistence.util;

import org.jruby.RubyInstanceConfig;
import org.perf4j.LoggingStopWatch;
import org.perf4j.StopWatch;

public enum ProfilingContext {
    INSTANCE;

    private StopWatch watch;
    private long totalTime;
    private static final boolean switchedOn = RubyInstanceConfig.IR_PERSISTENCE_PROFILE;
    
    static {
        System.out.print(switchedOn);
    }
    
    public boolean isSwitchedOn() {
        return switchedOn;
    }

    public void start(String label) {
        if (switchedOn) {
            if (watch == null) {
                watch = new LoggingStopWatch(label);
            } else {
                watch.start(label);
            }
        }
    }

    public void stop() {
        if (switchedOn) {
            totalTime += watch.getElapsedTime();
            watch.stop();
        }
    }
    
    public void reportTotalResult() {
        if (switchedOn) {
            System.out.println("Total time: " + totalTime);
        }
    }

    public void enterFile(String file) {
        if(switchedOn) {
            System.out.println(file);
       }
    }

}
