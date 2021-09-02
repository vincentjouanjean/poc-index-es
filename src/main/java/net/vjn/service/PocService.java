package net.vjn.service;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.vjn.entity.ParentObject;
import net.vjn.flow.BulkObjectParentOneSubscriber;
import net.vjn.flow.OnePublisher;
import net.vjn.repo.ObjetParentRepo;
import net.vjn.utils.DataMock;
import net.vjn.utils.ResultsAsync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class PocService {
    @Value("${jvn.nbiterate}")
    private int nbIterate;

    private static final int val1 = 1;
    private static final int val2 = 10;
    private static final int val3 = 19;

    private final ObjetParentRepo repo;

    @Autowired
    public PocService(final ObjetParentRepo repo) {
        this.repo = repo;
    }

    public void pocAsyncBulk(StringBuilder builder, final int sub, final int batch, final int sleep) throws InterruptedException {
        repo.deleteAll();
        final List<ParentObject> objects = new ArrayList<>();
        for (int i = 0; i < 6400; i++) {
            objects.add(DataMock.getObjects(i, val3));
        }
        new OnePublisher<>(objects, sub, batch, new BulkObjectParentOneSubscriber());
        Thread.sleep(sleep);
        builder.append(sub).append(" thread(s) with ").append(batch).append(" per batch : ").append(ResultsAsync.result).append("\n");
    }

    public void pocAction(StringBuilder builder) {
        results(builder, "save 1", save(nbIterate, val1, false, false));
        results(builder, "save 10", save(nbIterate, val2, false, false));
        results(builder, "save 19", save(nbIterate, val3, false, false));

        results(builder, "saveNew 1", save(nbIterate, val1, true, false));
        results(builder, "saveNew 10", save(nbIterate, val2, true, false));
        results(builder, "saveNew 19", save(nbIterate, val3, true, false));

        results(builder, "customSave 1", save(nbIterate, val1, false, true));
        results(builder, "customSave 10", save(nbIterate, val2, false, true));
        results(builder, "customSave 19", save(nbIterate, val3, false, true));

        results(builder, "customSaveNew 1", save(nbIterate, val1, true, true));
        results(builder, "customSaveNew 10", save(nbIterate, val2, true, true));
        results(builder, "customSaveNew 19", save(nbIterate, val3, true, true));

        results(builder, "delete 1", delete(nbIterate, val1));
        results(builder, "delete 10", delete(nbIterate, val2));
        results(builder, "delete 19", delete(nbIterate, val3));

        results(builder, "update 1", update(nbIterate, val1));
        results(builder, "update 10", update(nbIterate, val2));
        results(builder, "update 19", update(nbIterate, val3));

        bulkResult(builder, "bulk 1", bulk(val1));
        bulkResult(builder, "bulk 10", bulk(val2));
        bulkResult(builder, "bulk 19", bulk(val3));

        repo.deleteAll();
    }

    private List<Long> bulk(final int nbObject) {
        // 128000 = 413 Entity too large
        return Stream.of(10, 100, 200, 400, 800, 1600, 3200, 6400)
                .map(nb -> oneBulk(nb, nbObject)).collect(Collectors.toList());
    }

    @SneakyThrows
    private long oneBulk(final int limit, final int nbObject) {
        // request [POST http://localhost:9200/_bulk?timeout=1000000nanos]
        final List<ParentObject> data = new ArrayList<>();
        Stream.iterate(0, n -> n + 1)
                .limit(limit)
                .forEach(x -> data.add(DataMock.getObjects(x, nbObject)));
        final long time = System.currentTimeMillis();
        repo.customBulk(data);
        return System.currentTimeMillis() - time;
    }

    private List<Long> update(final int iterate, final int nbObject) {
        // request [POST http://localhost:9200/object.parent/_update/1?refresh=true&timeout=1m]
        repo.deleteAll();
        save(iterate, nbObject, true, false);
        final List<Long> times = new ArrayList<>();
        Stream.iterate(0, n -> n + 1)
                .limit(iterate)
                .forEach(x -> {
                    final long time = System.currentTimeMillis();
                    try {
                        repo.customUpdate(DataMock.getObjects(x, nbObject));
                    } catch (final IOException ignored) {
                    }
                    times.add(System.currentTimeMillis() - time);
                });
        return times;
    }

    private List<Long> delete(final int nbIterate, final int nbObject) {
        // request [DELETE http://localhost:9200/object.parent/_doc/1?timeout=1m]
        // request [POST http://localhost:9200/object.parent/_refresh?ignore_throttled=false&ignore_unavailable=false&expand_wildcards=open&allow_no_indices=true]
        repo.deleteAll();
        final List<Long> times = new ArrayList<>();
        Stream.iterate(0, n -> n + 1)
                .limit(nbIterate)
                .forEach(x -> {
                    repo.save(DataMock.getObjects(x, nbObject));
                    final long time = System.currentTimeMillis();
                    delete(x);
                    times.add(System.currentTimeMillis() - time);
                });
        return times;
    }

    private void delete(final Integer id) {
        repo.deleteById(String.valueOf(id));
    }

    private List<Long> save(final int nbIterate, final int nbObject, final boolean newId, final boolean custom) {
        // request [PUT http://localhost:9200/object.parent/_doc/1?timeout=1m]
        // request [POST http://localhost:9200/object.parent/_refresh?ignore_throttled=false&ignore_unavailable=false&expand_wildcards=open&allow_no_indices=true]
        // OR for custom, just :
        // request [PUT http://localhost:9200/object.parent/_doc/1?timeout=1m]
        repo.deleteAll();
        final List<Long> times = new ArrayList<>();
        Stream.iterate(0, n -> n + 1)
                .limit(nbIterate)
                .forEach(x -> {
                    final long time = System.currentTimeMillis();
                    int id = 1;
                    if (newId) {
                        id = x;
                    }
                    if (custom) {
                        try {
                            repo.customSave(DataMock.getObjects(id, nbObject));
                        } catch (final IOException ignored) {
                        }
                    } else {
                        repo.save(DataMock.getObjects(id, nbObject));
                    }
                    times.add(System.currentTimeMillis() - time);
                });
        return times;
    }

    private void results(final StringBuilder builder, final String action, final List<Long> times) {
        builder.append(action).append(" - MIN - ").append(times.stream().min(Long::compareTo)).append(" ")
                .append(action).append(" - MAX - ").append(times.stream().max(Long::compareTo)).append(" ")
                .append(action).append(" - AVG - ").append(times.stream().mapToDouble(value -> value).average())
                .append("\n");
    }

    private void bulkResult(final StringBuilder builder, final String action, final List<Long> times) {
        builder.append(action).append(" - Bulk 10 - ").append(times.get(0)).append(" ").append("\n");
        builder.append(action).append(" - Bulk 100 - ").append(times.get(1)).append(" ").append("\n");
        builder.append(action).append(" - bulk 200 - ").append(times.get(2)).append(" ").append("\n");
        builder.append(action).append(" - bulk 400 - ").append(times.get(3)).append(" ").append("\n");
        builder.append(action).append(" - bulk 800 - ").append(times.get(4)).append(" ").append("\n");
        builder.append(action).append(" - bulk 1600 - ").append(times.get(5)).append(" ").append("\n");
        builder.append(action).append(" - bulk 3200 - ").append(times.get(6)).append(" ").append("\n");
        builder.append(action).append(" - bulk 6400 - ").append(times.get(7)).append(" ").append("\n");
    }
}
