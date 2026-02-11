package com.yowyob.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorResponse {

    private Object data;
    private LocalDateTime updatedAt;
    private String sourceService;
}
