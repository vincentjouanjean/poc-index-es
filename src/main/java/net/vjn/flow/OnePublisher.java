package net.vjn.flow;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.ForkJoinPool;

public class OnePublisher<T> implements Flow.Publisher<T> {
    private final ExecutorService executor = ForkJoinPool.commonPool();

    private final List<T> data;

    public OnePublisher(final List<T> data, final int nbSub, final int size, final OneSubscriber<T> subscriber) {
        this.data = data;
        for (int i = 0; i < nbSub; i++) {
            this.subscribe(subscriber.create(size));
        }
    }

    public synchronized void subscribe(final Flow.Subscriber<? super T> subscriber) {
        subscriber.onSubscribe(new OneSubscription<T>(subscriber, executor, data));
    }
}
