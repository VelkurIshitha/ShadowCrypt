package com.shadowcrypt.ui.main;

import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/** Runs JavaFX tasks on daemon threads and delivers results on the FX thread. */
public final class BackgroundTaskRunner {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "shadowcrypt-worker");
        thread.setDaemon(true);
        return thread;
    });

    private BackgroundTaskRunner() {
    }

    public static <T> void run(Task<T> task, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        task.setOnFailed(event -> {
            Throwable error = task.getException();
            onFailure.accept(error != null ? error : new IllegalStateException("The operation failed"));
        });
        EXECUTOR.execute(task);
    }
}
