package com.hevo.searchable.core;

import com.hevo.searchable.ESService;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Log4j2
public class IndexingService implements Indexing {

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Autowired private ESService esService;

    @Override
    public boolean indexFile(@NonNull String client, @NonNull File file) {
    executorService.submit(() -> {
        try{
            log.info("Indexing file {} for client {}", file.getName(), client);
            esService.addFileToIndex(client, file);
        }catch(Exception e){
            log.error("Error indexing file {} for client {}", file.getName(), client, e);
            //Todo: Update in Db that file is not indexed. Retries ???
        }});
        return true;
    }

    @Override
    public boolean deleteFile(@NonNull String client, @NonNull String fileName) throws IOException {
        return esService.delete(client, fileName);
    }
}
