package tamp.queue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleProducerMultipleConsumers<T> {


    private final BlockingQueue<T> queue;
    private final Iterator<T> iterator;
    private volatile boolean isFinished = false;
    private List<Throwable> finishedWErrors = new ArrayList<>();

    public List<Throwable> finishedWithErrors() {
        return finishedWErrors;
    }

    /**
     * @return null is there is no more account to pull
     */
    public T get() {
        while (true) {
            T element = queue.poll();
            if (isFinished && element == null) {
                // it is possible that the condition is true but from the moment
                // of the poll to the moment of the evaluation of the condition, more elements were pushed
                // so poll one more time to make sure that the queue is indeed empty
                element = queue.poll();
                if (element == null) break;
            }
            if (element != null) return element;
        }
        return null;
    }

    public static class Builder<T> {

        private final Iterator<T> iterator;
        private final int capacity;

        public Builder(final Iterator<T> iterator,
                       final int capacity) {
            this.iterator = iterator;
            this.capacity = capacity;
        }

        public SingleProducerMultipleConsumers start() {
            BlockingQueue<T> queue = new ArrayBlockingQueue<>(capacity);
            SingleProducerMultipleConsumers singleProducerMultipleConsumers = new SingleProducerMultipleConsumers(queue,
                    iterator);
            singleProducerMultipleConsumers.start();
            return singleProducerMultipleConsumers ;
        }
    }

    private SingleProducerMultipleConsumers(final BlockingQueue<T> queue,
                                            final Iterator<T> iterator) {
        this.queue = queue;
        this.iterator = iterator;
    }

    private void start() {
        final ExecutorService service = Executors.newSingleThreadExecutor();
        Runnable runnable = () -> {
            try {
                while (iterator.hasNext()) {
                    queue.put(iterator.next());
                }
            } catch (final InterruptedException e) {
                finishedWErrors.add(new Throwable(e));
            } finally {
                isFinished = true;
            }
        };
        service.submit(runnable);
        service.shutdown();
    }
}
