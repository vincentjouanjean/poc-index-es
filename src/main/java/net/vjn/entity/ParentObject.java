package net.vjn.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.vjn.utils.ElasticsearchConfig;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(indexName = ElasticsearchConfig.INDEX_PARENT)
public class ParentObject {
    @Id
    private int id;
    private Object1 obj1;
    private Object2 obj2;
}
