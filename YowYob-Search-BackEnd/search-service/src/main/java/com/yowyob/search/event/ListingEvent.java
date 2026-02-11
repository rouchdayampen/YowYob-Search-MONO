package com.yowyob.search.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListingEvent implements Serializable {
    private UUID id;
    private String title;
    private String description;
    private Double price;
    private String category;
    private String address;
    private Double latitude;
    private Double longitude;
    private String status;
    private UUID sellerId;
    private String eventType;
}
