package com.yowyob.listing.domain.port.out;

import com.yowyob.listing.domain.model.Listing;

/**
 * Port de sortie — interface vers la publication d'événements Listing.
 * Implémenté par RabbitMQListingPublisher dans la couche adapter/out/messaging.
 * Le domaine ne connaît pas RabbitMQ.
 */
public interface ListingEventPublisher {

    void publishCreated(Listing listing);

    void publishUpdated(Listing listing);

    void publishDeleted(Listing listing);
}
