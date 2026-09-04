package io.tenoro.app.infra.adapter.inbound.web.mappers;

import io.tenoro.app.api.dto.StockResponse;
import io.tenoro.app.domain.model.InventoryItem;

public final class StockMapper {

    private StockMapper() {
    }

    public static StockResponse toResponse(InventoryItem item) {
        if (item == null) {
            return null;
        }
        return new StockResponse(item.getSku(), item.getLocationCode(), item.getQuantity());
    }
}
