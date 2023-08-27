package com.hevo.searchable.core;

import com.hevo.searchable.ESService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class SearchService implements Search {

    @Autowired private ESService esService;

    @Override
    public List<String> fullTextSearch(@NonNull String client, @NonNull String query) throws IOException {
        return esService.search(client, query);
    }
}
