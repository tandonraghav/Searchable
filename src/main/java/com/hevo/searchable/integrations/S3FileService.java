package com.hevo.searchable.integrations;

import com.hevo.searchable.FileService;
import com.hevo.searchable.core.Indexing;
import com.hevo.searchable.core.Search;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Log4j2
@Component
public class S3FileService implements FileService {

    @Autowired
    private Indexing indexingService;

    @Autowired
    private Search searchService;

    @Override
    public boolean index(@NonNull String client, @NonNull File file) {
        log.info("Indexing files from S3");
        return indexingService.indexFile(client, file);
    }

    @Override
    public List<String> search(@NonNull String client, @NonNull String query) throws IOException {
        log.info("Searching files from S3 client {} query {}", client, query);
        List<String> matchedFiles = searchService.fullTextSearch(client, query);
        log.info(matchedFiles);
        return matchedFiles;
    }

    @Override
    public boolean delete(@NonNull String client, @NonNull String fileName) throws IOException {
        log.info("Deleting file {} from S3 client {}", fileName, client);
        return indexingService.deleteFile(client, fileName);
    }
}
