package com.yowyob.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchConfiguration;
import org.springframework.lang.NonNull;

import java.net.URI;

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

        // Fix for 406 Not Acceptable (Bonsai/OpenSearch vs Elastic Client 8)
        // We set a header to force JSON, hoping to override the versioned vendor header
        builder.withDefaultHeaders(new org.springframework.http.HttpHeaders() {
            {
                add("Content-Type", "application/json");
                // Also try to disable the compatibility header check by mimicking an older
                // client?
                // Actually, usually just avoiding the specific vendor header helps.
            }
        });

        // Important: Spring Data ES 5.x by default sends the compatibility header.
        // There isn't a simple "disableCompatibilityHeader" method in the builder
        // exposed easily.
        // But setting the default header might help if the underlying client
        // prioritizes it.

        return builder.build();
    }
}
