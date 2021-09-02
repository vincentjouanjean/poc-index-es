package net.vjn.flow;

import java.util.concurrent.Flow;

public interface OneSubscriber<T> {
    Flow.Subscriber<T> create(int size);
}
