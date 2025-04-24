package org.example.tablenow.domain.store.message.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.entity.StoreDocument;

import java.io.Serializable;

@Getter
public class StoreEventDto implements Serializable {
    private StoreDocument storeDocument;

    @Builder
    private StoreEventDto(StoreDocument storeDocument) {
        this.storeDocument = storeDocument;
    }

    public static StoreEventDto fromStore(Store store) {
        return new StoreEventDto(StoreDocument.fromStore(store));
    }
}
