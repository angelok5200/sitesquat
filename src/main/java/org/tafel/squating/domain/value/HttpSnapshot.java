package org.tafel.squating.domain.value;

import java.util.List;

public record  HttpSnapshot(
    int statusCode,
    String finalUrl,
    List<String> redirectChain,
    String title,
    String contentType,
    long contentLenght
) {
    public   HttpSnapshot{
        redirectChain = List.copyOf(redirectChain);
    }
}
