package org.tafel.squating.adapters.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.tafel.squating.domain.value.HttpSnapshot;
import org.tafel.squating.ports.outbound.WebInspector;

@Component
public class HttpAdapter implements WebInspector {

    private final HttpClient httpClient;

    public HttpAdapter() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    @Override
    public HttpSnapshot inspect(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException(
                "domain must not be blank"
            );
        }

        String url = normalizeUrl(domain);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header(
                    "User-Agent",
                    "Mozilla/5.0"
                )
                .GET()
                .build();

            HttpResponse<String> response =
                httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );

            List<String> redirectChain =
                buildRedirectChain(response);

            String contentType =
                response.headers()
                    .firstValue("Content-Type")
                    .orElse(null);

            String title =
                extractTitle(response.body());

            long contentLength =
                response.body() != null
                    ? response.body().getBytes().length
                    : 0;

            return new HttpSnapshot(
                response.statusCode(),
                response.uri().toString(),
                redirectChain,
                title,
                contentType,
                contentLength
            );

        } catch (Exception e) {
            return new HttpSnapshot(
                0,
                url,
                List.of(),
                null,
                null,
                0
            );
        }
    }

    private String normalizeUrl(String domain) {
        String normalized = domain.trim();

        if (!normalized.startsWith("http://") &&
            !normalized.startsWith("https://")) {

            normalized = "https://" + normalized;
        }

        return normalized;
    }

    private List<String> buildRedirectChain(
        HttpResponse<String> response
    ) {
        List<String> chain = new ArrayList<>();

        chain.add(response.uri().toString());

        return List.copyOf(chain);
    }

    private String extractTitle(String html) {
        if (html == null || html.isBlank()) {
            return null;
        }

        String lower = html.toLowerCase();

        int start = lower.indexOf("<title>");

        if (start < 0) {
            return null;
        }

        int contentStart = start + "<title>".length();

        int end = lower.indexOf(
            "</title>",
            contentStart
        );

        if (end < 0) {
            return null;
        }

        return html
            .substring(contentStart, end)
            .trim();
    }
}