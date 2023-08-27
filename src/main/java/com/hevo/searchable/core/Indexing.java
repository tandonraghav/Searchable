package com.hevo.searchable.core;

import lombok.NonNull;

import java.io.File;
import java.io.IOException;

public interface Indexing {
    boolean indexFile(@NonNull String client, @NonNull File file);
    boolean deleteFile(@NonNull String client, @NonNull String fileName) throws IOException;
}
