package ru.cubesolutions.evam.way4pusheraction;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Garya on 09.02.2018.
 */
public class Way4Pusher {

    private final static Logger log = Logger.getLogger(Way4Pusher.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        Way4Pusher way4Pusher = new Way4Pusher();
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleWithFixedDelay(way4Pusher::taskPushOneFile, 0, Config.TIME_PAUSE_BETWEEN_TASKS_IN_SECONDS, TimeUnit.SECONDS);
    }

    private void taskPushOneFile() {
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(ConsumerJob.INSTANCE::start);
        sleep(Config.TIME_READING_FOR_ONE_FILE_IN_SECONDS);
        ConsumerJob.INSTANCE.stop();
        stop(es, 60);
    }

    public static void stop(ExecutorService executor, int awaitTerminationInSeconds) {
        try {
            executor.shutdown();
            executor.awaitTermination(awaitTerminationInSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Internal error, termination task interrupted", e);
        } finally {
            if (!executor.isTerminated()) {
                log.warn("Internal error, killing non-finished tasks");
            }
            executor.shutdownNow();
        }
    }

    public static void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            log.error("Internal error", e);
            throw new IllegalStateException(e);
        }
    }
}
