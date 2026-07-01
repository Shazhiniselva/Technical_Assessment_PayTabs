package com.bankingpoc.model;

import com.bankingpoc.util.JsonHelper;

import java.util.Map;

public record Customer(String id, String name) {
    public String toJson() {
        return JsonHelper.object(Map.of("id", id, "name", name));
    }
}
