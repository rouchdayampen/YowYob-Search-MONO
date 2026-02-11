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

        // Fix for 406 Not Acceptable: STRICTLY force headers via low-level
        // RestClientBuilder
        builder.withClientConfigurer((RestClientBuilder restClientBuilder) -> {
            // 1. Set default headers
            restClientBuilder.setDefaultHeaders(new org.apache.http.Header[] {
                    new org.apache.http.message.BasicHeader("Content-Type", "application/json"),
                    new org.apache.http.message.BasicHeader("Accept", "application/json")
            });

            // 2. Add interceptor to OVERWRITE any headers added by Spring Data / Client
            restClientBuilder.setHttpClientConfigCallback(
                    (org.apache.http.impl.nio.client.HttpAsyncClientBuilder httpClientBuilder) -> {
                        return httpClientBuilder.addInterceptorLast(
                                (org.apache.http.HttpRequestInterceptor) (org.apache.http.HttpRequest request,
                                        org.apache.http.protocol.HttpContext context) -> {
                                    // Unconditionally overwrite these headers
                                    request.setHeader("Content-Type", "application/json");
                                    request.setHeader("Accept", "application/json");
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
