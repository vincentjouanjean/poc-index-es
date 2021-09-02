package net.vjn.repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.vjn.entity.ParentObject;
import net.vjn.utils.ElasticsearchConfig;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.io.IOException;
import java.util.List;

/**
 * By default requests have a disabled reload
 * request.setRefreshPolicy(WriteRequest.RefreshPolicy.NONE)
 */
public interface ObjetParentRepo extends ElasticsearchRepository<ParentObject, String> {
    default void customUpdate(final ParentObject obj) throws IOException {
        final UpdateRequest request = new UpdateRequest(ElasticsearchConfig.INDEX_PARENT, String.valueOf(obj.getId()));
        request.doc(new ObjectMapper().writeValueAsString(obj.getObj1()), XContentType.JSON);
        ElasticsearchConfig.getRestHighLevelClient().update(request, RequestOptions.DEFAULT);
    }

    default void customSave(final ParentObject obj) throws IOException {
        final IndexRequest request = new IndexRequest(ElasticsearchConfig.INDEX_PARENT);
        request.source(new ObjectMapper().writeValueAsString(obj), XContentType.JSON);
        ElasticsearchConfig.getRestHighLevelClient().index(request, RequestOptions.DEFAULT);
    }

    default void customBulk(final List<ParentObject> objects) throws IOException {
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
}
