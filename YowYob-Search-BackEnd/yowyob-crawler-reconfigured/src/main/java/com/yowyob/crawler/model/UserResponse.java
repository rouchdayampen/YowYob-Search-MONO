package com.yowyob.crawler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder; // Added Builder import

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder // Added @Builder
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {

    private UUID id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @JsonProperty("role")
    private String role;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @JsonProperty("bio")
    private String bio;

    @JsonProperty("address")
    private String address;

    @JsonProperty("city")
    private String city;

    @JsonProperty("country")
    private String country;

    @JsonProperty("avatarUrl")
    private String avatarUrl;

    @JsonProperty("socialLinksJson")
    private String socialLinksJson;

    @JsonProperty("userId")
    private UUID userId;
}
