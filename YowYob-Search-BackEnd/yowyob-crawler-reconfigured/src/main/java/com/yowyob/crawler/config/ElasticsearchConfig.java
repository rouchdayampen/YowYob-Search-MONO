package com.yowyob.crawler.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.net.URI;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String uris;

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Ensure ISO-8601 string format
        return mapper;
    }

    @Bean
    @Primary
    public JacksonJsonpMapper jacksonJsonpMapper(ObjectMapper objectMapper) {
        return new JacksonJsonpMapper(objectMapper);
    }

    @Bean
    @Primary
    public ElasticsearchClient elasticsearchClient(RestClient restClient, JacksonJsonpMapper mapper) {
        ElasticsearchTransport transport = new RestClientTransport(restClient, mapper);
        return new ElasticsearchClient(transport);
    }

    @Bean
    public RestClient restClient() {
        String firstUri = uris.split(",")[0];
        URI uri = URI.create(firstUri);

        RestClientBuilder builder = RestClient.builder(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()));

        // Headers gérés par le client Elasticsearch natif, suppression des headers
        // manuels qui causent "media_type_header_exception"

        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.addInterceptorLast(
                    (org.apache.http.HttpResponse response, org.apache.http.protocol.HttpContext context) -> {
                        if (!response.containsHeader("X-Elastic-Product")) {
                            response.addHeader("X-Elastic-Product", "Elasticsearch");
                        }
                    });

            String userInfo = uri.getUserInfo();
            if (userInfo != null && userInfo.contains(":")) {
                String[] creds = userInfo.split(":");
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(creds[0], creds[1]));
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            return httpClientBuilder;
        });

        return builder.build();
    }
}
