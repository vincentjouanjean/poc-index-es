package net.vjn.flow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.vjn.entity.ParentObject;
import net.vjn.utils.ElasticsearchConfig;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.function.Consumer;

@Slf4j
class BulkObjetParentSubscriber implements Flow.Subscriber<ParentObject> {
    final Consumer<? super ParentObject> consumer;
    Flow.Subscription subscription;
    final long buffer;
    long count;
    private final List<ParentObject> objects;

    BulkObjetParentSubscriber(final long buffer, final Consumer<? super ParentObject> consumer) {
        this.buffer = buffer;
        this.consumer = consumer;
        objects = new ArrayList<>();
    }

    public void onSubscribe(final Flow.Subscription subscription) {
        (this.subscription = subscription).request(buffer);
    }

    public void onNext(final ParentObject item) {
        objects.add(item);
        consumer.accept(item);
    }

    public void customBulk(final List<ParentObject> objects) throws IOException {
        final BulkRequest request = new BulkRequest(ElasticsearchConfig.INDEX_PARENT);
        objects.forEach(object -> {
            final IndexRequest indexRequest = new IndexRequest(ElasticsearchConfig.INDEX_PARENT);
            try {
                indexRequest.source(new ObjectMapper().writeValueAsString(object), XContentType.JSON);
            } catch (final JsonProcessingException ignored) {
            }
            request.add(indexRequest);
        });
        request.timeout(TimeValue.timeValueNanos(1_000_000));
        ElasticsearchConfig.getRestHighLevelClient().bulk(request, RequestOptions.DEFAULT);
    }

    public void onError(final Throwable ex) {
        log.error(ex.getMessage());
    }

    public void onComplete() {
        try {
            customBulk(objects);
            objects.clear();
        } catch (final IOException ex) {
            log.error(ex.getMessage());
        }
        subscription.request(count = buffer);
    }
}
