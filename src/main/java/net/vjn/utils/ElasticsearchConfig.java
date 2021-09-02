package net.vjn.utils;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;

public final class ElasticsearchConfig {
    public static final String INDEX_PARENT = "object.parent";
    public static RestHighLevelClient getRestHighLevelClient() {
        return RestClients.create(ClientConfiguration.builder()
                .connectedTo("localhost:9200")
                .withSocketTimeout(1_000_000)
                .build()).rest();
    }
}
