package ca.quanta.quantaevents.viewmodels;

import static org.junit.Assert.fail;

import com.google.android.gms.tasks.Task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TaskHandler {

    public static <T> void handle(Task<T> task, Consumer<T> resultConsumer) throws InterruptedException {
        handle(task, resultConsumer, 10);
    }

    public static <T> void handle(Task<T> task, Consumer<T> resultConsumer, long seconds) throws InterruptedException {
        CountDownLatch timeoutLatch = new CountDownLatch(1);
        CountDownLatch safetyLatch = new CountDownLatch(1);

        task.addOnSuccessListener(value -> {
                    timeoutLatch.countDown();
                    resultConsumer.accept(value);
                    safetyLatch.countDown();
                })
                .addOnFailureListener(exc -> {
                    fail("Failed to call function with exception: " + exc);
                    timeoutLatch.countDown();
                });

        boolean succeeded = timeoutLatch.await(seconds, TimeUnit.SECONDS);
        if (!succeeded) {
            fail("Function timed out");
            return;
        }

        safetyLatch.await();
    }
}
