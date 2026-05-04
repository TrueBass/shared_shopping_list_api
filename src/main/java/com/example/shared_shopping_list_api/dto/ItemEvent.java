package com.example.shared_shopping_list_api.dto;

public record ItemEvent(EventType type, ItemResponse item, Long itemId) {

    public enum EventType {
        ITEM_CREATED, ITEM_UPDATED, ITEM_DELETED
    }
}