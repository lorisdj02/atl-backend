package com.atl.backend.util;

import com.atl.backend.dto.PurchaseDetail;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.databind.ObjectMapper;

@Converter(autoApply = false)
public class PurchaseDetailConverter implements AttributeConverter<PurchaseDetail, String> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(PurchaseDetail attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to convert PurchaseDetail to JSON", e);
        }
    }

    @Override
    public PurchaseDetail convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        try {
            return MAPPER.readValue(dbData, PurchaseDetail.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to convert JSON to PurchaseDetail", e);
        }
    }
}