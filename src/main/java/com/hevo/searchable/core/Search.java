package com.hevo.searchable.core;

import lombok.NonNull;

import java.io.IOException;
import java.util.List;

public interface Search {

    List<String> fullTextSearch(@NonNull String client, @NonNull String query) throws IOException;
}
