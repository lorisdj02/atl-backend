package com.atl.backend.util;

import com.atl.backend.dto.CheckoutPayload;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.databind.ObjectMapper;

@Converter(autoApply = false)
public class CheckoutPayloadConverter implements AttributeConverter<CheckoutPayload, String> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(CheckoutPayload attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to convert CheckoutPayload to JSON", e);
        }
    }

    @Override
    public CheckoutPayload convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        try {
            return MAPPER.readValue(dbData, CheckoutPayload.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to convert JSON to CheckoutPayload", e);
        }
    }
}