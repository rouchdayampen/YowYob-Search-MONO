package com.yowyob.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchConfiguration;
import org.springframework.lang.NonNull;

import java.net.URI;

import org.elasticsearch.client.RestClientBuilder;

@Configuration
public class ElasticsearchConfig extends ReactiveElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUriString;

    @Override
    @NonNull
    public ClientConfiguration clientConfiguration() {
        URI uri = URI.create(elasticsearchUriString);
        String host = uri.getHost();
        int port = uri.getPort() == -1 ? (uri.getScheme().equals("https") ? 443 : 80) : uri.getPort();
        String userInfo = uri.getUserInfo();

        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
                .connectedTo(host + ":" + port);

        if ("https".equals(uri.getScheme())) {
            builder.usingSsl();
        }

        if (userInfo != null) {
            String[] parts = userInfo.split(":");
            if (parts.length == 2) {
                builder.withBasicAuth(parts[0], parts[1]);
            }
        }

        // Fix for 406 Not Acceptable: Use Interceptor to strip incompatible headers
        builder.withClientConfigurer((RestClientBuilder restClientBuilder) -> {
            restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
                return httpClientBuilder.addInterceptorLast(
                        (org.apache.http.HttpRequestInterceptor) (request, context) -> {
                            org.apache.http.Header contentType = request.getFirstHeader("Content-Type");
                            if (contentType != null && contentType.getValue().contains("compatible-with=8")) {
                                request.setHeader("Content-Type", "application/json");
                                request.setHeader("Accept", "application/json");
                            }
                        });
            });
            return restClientBuilder;
        });

        // Important: Spring Data ES 5.x by default sends the compatibility header.
        // There isn't a simple "disableCompatibilityHeader" method in the builder
        // exposed easily.
        // But setting the default header might help if the underlying client
        // prioritizes it.

        return builder.build();
    }
}
