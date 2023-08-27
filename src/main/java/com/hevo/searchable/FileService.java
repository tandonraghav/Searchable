package com.hevo.searchable;

import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface FileService {

    boolean index(@NonNull String client, @NonNull File file);
    List<String> search(@NonNull String client, @NonNull String query) throws IOException;
    boolean delete(@NonNull String client, @NonNull String fileName) throws IOException;
}
