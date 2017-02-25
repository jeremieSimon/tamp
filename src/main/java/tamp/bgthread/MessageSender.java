package tamp.bgthread;


import com.google.common.base.Stopwatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageSender {

    final ExecutorService executorService;
    final Stopwatch stopwatch;
    final List<String> messages;
    int left;
    final AtomicInteger right;
    final AtomicBoolean hasComplete;

    MessageSender(final Stopwatch stopwatch) {
        this.stopwatch = stopwatch;
        executorService = Executors.newSingleThreadExecutor();
        right = new AtomicInteger();
        executorService.submit(newRunnable());
        messages = new ArrayList<>();
        hasComplete = new AtomicBoolean(false);
    }

    public void append(final String message) {
        messages.add(message);
        if (hasComplete.get()) {
            if (hasComplete.compareAndSet(true, false)) executorService.submit(newRunnable());
        }
        right.incrementAndGet();
    }

    Runnable newRunnable() {
        return () -> {
            long lastSent = 0L;
            while (true) {
                if (stopwatch.elapsed(TimeUnit.SECONDS) - lastSent > 5) {
                    int currentRightValue = right.get();
                    if (left == currentRightValue) break;
                }
                if (stopwatch.elapsed(TimeUnit.SECONDS) - lastSent > 0) {
                    int currentRightValue = right.get();
                    if (left == currentRightValue) continue;
                    System.out.println(messages.subList(left, currentRightValue));
                    lastSent = stopwatch.elapsed(TimeUnit.SECONDS);
                    left = currentRightValue;
                }
            }
            hasComplete.set(true);
        };
    }

    public void close() {
        executorService.shutdown();
    }
}
