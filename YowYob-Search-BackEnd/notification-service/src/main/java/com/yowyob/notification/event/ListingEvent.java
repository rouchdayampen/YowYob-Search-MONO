package com.yowyob.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
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
    private UUID sellerId;
    private String eventType;
}
