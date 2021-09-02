package net.vjn.flow;

import net.vjn.utils.ResultsAsync;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

class OneSubscription<T> implements Flow.Subscription {
    private final Flow.Subscriber<? super T> subscriber;
    private final ExecutorService executor;
    private Future<?> future;
    private boolean firstRequest;
    private final List<T> data;
    private long time;

    OneSubscription(final Flow.Subscriber<? super T> subscriber,
                    final ExecutorService executor,
                    final List<T> data) {
        this.subscriber = subscriber;
        this.executor = executor;
        this.data = data;
        this.firstRequest = true;
    }

    public synchronized void request(final long nb) {
        if (firstRequest) {
            time = System.currentTimeMillis();
            firstRequest = false;
        }
        if (!data.isEmpty()) {
            future = executor.submit(() -> {
                final List<T> list;
                synchronized (data) {
                    list = data.stream().limit(nb).collect(Collectors.toList());
                    data.removeAll(list);
                }
                list.forEach(subscriber::onNext);
                subscriber.onComplete();
            });
        } else {
            ResultsAsync.result = System.currentTimeMillis() - time;
        }
    }

    public synchronized void cancel() {
        if (future != null) {
            future.cancel(false);
        }
    }
}
