package net.vjn.flow;


import net.vjn.entity.ParentObject;

import java.util.concurrent.Flow;
import java.util.function.Consumer;

public class BulkObjectParentOneSubscriber implements OneSubscriber<ParentObject> {
    @Override
    public Flow.Subscriber<ParentObject> create(final int size) {
        final Consumer<ParentObject> consumer = o -> {
        };
        return new BulkObjetParentSubscriber(size, consumer);
    }
}
