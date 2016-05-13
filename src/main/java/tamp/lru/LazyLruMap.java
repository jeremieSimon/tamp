package tamp.lru;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Implementation based on a classic concurrent hashmap.
 * The management of the lru (eg: how to place element in queue / how to evict element) is done asynchornously.
 */
public class LazyLruMap<K, V> implements LruMap<K, V> {

    final int capacity;
    final ConcurrentMap<K, V> map;
    final LruManager<K> lruManager;

    final ListeningExecutorService lruManagerService;
    final ThreadFactory lruManagerFactory = new ThreadFactoryBuilder()
    .setNameFormat("lru-scheduling-%d")
    .build();

    final ThreadFactory evictatorFactory = new ThreadFactoryBuilder()
        .setNameFormat("eviction-scheduling-%d")
        .build();
    final ExecutorService evictatorService;

    public LazyLruMap(int capacity) {
        this.capacity = capacity;
        lruManager = new LruManager<>(capacity);
        map = Maps.newConcurrentMap();

        lruManagerService = MoreExecutors
            .listeningDecorator(Executors.newFixedThreadPool(1, lruManagerFactory));
        evictatorService = Executors.newFixedThreadPool(2, evictatorFactory);
    }

    @Override
    public V get(final K k) {
        V v = map.containsKey(k) ? map.get(k) : fetch(k);
        ListenableFuture<Optional<K>> evictedEntry = lruManagerService
            .submit(lruManager.add(k));
        Futures.addCallback(evictedEntry, new FutureCallback<Optional<K>>(){

            @Override
            public void onFailure(Throwable arg0) {
            }

            @Override
            public void onSuccess(Optional<K> entry) {
                if (!entry.isPresent()) {
                    return;
                }
                map.remove(entry.get());
            }
        }, evictatorService);
        return v;
    }

    @Override
    public String toString() {
        return lruManager.queueSet.toString();
    }

    @Override
    public void finalize() {
        lruManagerService.shutdown();
        evictatorService.shutdown();
    }

    private V fetch(K k) {
        //simulate some expensive computation
        return null;
    }

    static class LruManager<K> {

        QueueLinkedSet<K> queueSet;
        final int capacity;

        LruManager(final int capacity) {
            this.capacity = capacity;
            queueSet = new QueueLinkedSet<>(capacity);
        }

        Callable<Optional<K>> add(final K k) {
            return new Callable<Optional<K>>() {
                @Override
                public Optional<K> call() throws Exception {
                    if (queueSet.contains(k)) {
                        queueSet.remove(k);
                    }
                    return queueSet.addAndPopIfFull(k);
                }
            };
        }
    }
}
