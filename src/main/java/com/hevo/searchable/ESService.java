package com.hevo.searchable;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkListener;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hevo.searchable.entities.MatchedFiles;
import com.hevo.searchable.utils.TikaUtils;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpHost;
import org.apache.tika.exception.TikaException;
import org.elasticsearch.client.RestClient;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Log4j2
public class ESService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final ElasticsearchClient esClient;


    public ESService() {
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http"),
                new HttpHost("localhost", 9201, "http")).build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        esClient = new ElasticsearchClient(transport);
    }

    //ToDo: Handle Big files, in chunks?? or use the ES attachement??
    public void addFileToIndex(@NonNull String client, @NonNull File file) throws IOException,
            TikaException, SAXException {
        log.info("Indexing file {} for client {}", file.getName(), client);
        String content = TikaUtils.parseDocument(file);
        JsonNode data = MAPPER.createObjectNode();
        ((ObjectNode) data).put("name", file.getName());
        ((ObjectNode) data).put("content", content);

        esClient.index(index -> index
                .index(client)
                .id(file.getName())
                .document(data)
        );
        log.info("Indexed file {} for client {}", file.getName(), client);
    }

    private BulkIngester<String> createBulkIngestor(){
        BulkListener<String> listener = new BulkListener<>() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request, List<String> contexts) {
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, List<String> contexts, BulkResponse response) {
                // The request was accepted, but may contain failed items.
                // The "context" list gives the file name for each bulk item.
                log.info("Bulk request " + executionId + " completed");
                for (int i = 0; i < contexts.size(); i++) {
                    BulkResponseItem item = response.items().get(i);
                    if (item.error() != null) {
                        // Inspect the failure cause
                        //Todo: Update the file status to failed in DB, and retry again
                        log.error("Failed to index file " + contexts.get(i) + " - " + item.error().reason());
                    }
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, List<String> contexts, Throwable failure) {
                // The request could not be sent
                //Todo: Update the file status to failed in DB, and retry again
                log.error("Bulk request " + executionId + " failed", failure);
            }
        };

        return BulkIngester.of(b -> b
                .client(esClient)
                .maxOperations(100)
                .flushInterval(1, TimeUnit.SECONDS)
                .listener(listener)
        );
    }

    public List<String> search(@NonNull String client, @NonNull String searchText) throws IOException {
        List<String> files = new ArrayList<>();
        SearchResponse response = esClient.search(s -> s
                        .index(client)
                        .query(q -> q
                                .match(t -> t
                                        .field("content")
                                        .query(searchText)
                                )
                        ),
                MatchedFiles.class
        );

        TotalHits total = response.hits().total();

        if (total == null) {
            log.info("There are no results");
            return files;
        }

        boolean isExactResult = total.relation() == TotalHitsRelation.Eq;

        if (isExactResult) {
            log.info("There are " + total.value() + " results");
        } else {
            log.info("There are more than " + total.value() + " results");
        }

        List<Hit<MatchedFiles>> hits = response.hits().hits();
        for (Hit<MatchedFiles> mf: hits) {
            if (mf.source() != null){
                files.add(mf.source().getName());
            }
        }
        return files;
    }

    public boolean delete(@NonNull String client, @NonNull String fileName) throws IOException {
        log.info("Deleting file {} for client {}", fileName, client);
        DeleteResponse response = esClient.delete(delete -> delete
                .index(client)
                .id(fileName)
        );
        return response.result() == Result.Deleted;
    }
}
