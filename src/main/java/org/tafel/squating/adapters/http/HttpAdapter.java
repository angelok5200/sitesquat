package org.tafel.squating.adapters.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.tafel.squating.domain.value.HttpSnapshot;
import org.tafel.squating.ports.outbound.WebInspector;

@Component
public class HttpAdapter implements WebInspector {

    private static final String USER_AGENT =
            "Mozilla/5.0 (compatible; SiteSquating/1.0)";

    private static final int MAX_REDIRECTS = 10;

    private static final int MAX_RESPONSE_SIZE_BYTES =
            1024 * 1024;

    private static final Pattern TITLE_PATTERN =
            Pattern.compile(
                    "<title[^>]*>(.*?)</title>",
                    Pattern.CASE_INSENSITIVE
                            | Pattern.DOTALL
            );

    private final HttpClient httpClient;

    public HttpAdapter(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public HttpSnapshot inspect(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("domain must not be blank");
        }

        URI initialUri = createInitialUri(domain);

        List<String> redirectChain = new ArrayList<>();

        URI currentUri = initialUri;

        for (int redirectCount = 0;
             redirectCount <= MAX_REDIRECTS;
             redirectCount++) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(currentUri)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,*/*")
                    .GET()
                    .build();

            try {
                HttpResponse<InputStream> response =
                        httpClient.send(
                                request,
                                HttpResponse.BodyHandlers.ofInputStream()
                        );

                int statusCode = response.statusCode();

                if (isRedirect(statusCode)) {
                    String location =
                            response.headers()
                                    .firstValue("Location")
                                    .orElse(null);

                    if (location == null || location.isBlank()) {
                        return createSnapshot(
                                response,
                                currentUri,
                                redirectChain
                        );
                    }

                    URI nextUri = currentUri.resolve(location);

                    redirectChain.add(nextUri.toString());
                    currentUri = nextUri;

                    response.body().close();

                    continue;
                }

                return createSnapshot(
                        response,
                        currentUri,
                        redirectChain
                );

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                throw new IllegalStateException(
                        "HTTP inspection interrupted for " + domain,
                        e
                );

            } catch (IOException e) {
                throw new IllegalStateException(
                        "HTTP inspection failed for " + domain,
                        e
                );
            }
        }

        throw new IllegalStateException(
                "Maximum HTTP redirects exceeded for " + domain
        );
    }

    private HttpSnapshot createSnapshot(
            HttpResponse<InputStream> response,
            URI finalUri,
            List<String> redirectChain
    ) throws IOException {

        String body = readBody(response.body());

        return new HttpSnapshot(
                response.statusCode(),
                finalUri.toString(),
                redirectChain,
                extractTitle(body),
                response.headers()
                        .firstValue("Content-Type")
                        .orElse(null),
                body != null
                        ? body.getBytes(StandardCharsets.UTF_8).length
                        : 0,
                body
        );
    }

    private String readBody(InputStream inputStream)
            throws IOException {

        if (inputStream == null) {
            return null;
        }

        try (InputStream input = inputStream;
             ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];

            int totalBytes = 0;
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                totalBytes += bytesRead;

                if (totalBytes > MAX_RESPONSE_SIZE_BYTES) {
                    throw new IllegalStateException(
                            "HTTP response exceeds maximum allowed size of "
                                    + MAX_RESPONSE_SIZE_BYTES
                                    + " bytes"
                    );
                }

                output.write(buffer, 0, bytesRead);
            }

            return output.toString(StandardCharsets.UTF_8);
        }
    }

    private URI createInitialUri(String domain) {
        String normalized = domain.trim();

        if (normalized.startsWith("http://")
                || normalized.startsWith("https://")) {
            return URI.create(normalized);
        }

        return URI.create("https://" + normalized);
    }

    private boolean isRedirect(int statusCode) {
        return statusCode >= 300 && statusCode < 400;
    }

    private String extractTitle(String html) {
        if (html == null || html.isBlank()) {
            return null;
        }

        Matcher matcher = TITLE_PATTERN.matcher(html);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1)
                .replaceAll("\\s+", " ")
                .trim();
    }
}