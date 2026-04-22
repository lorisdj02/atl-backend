package com.atl.backend.util;

import com.atl.backend.dto.FootballMatchEventModifiedValues;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.databind.ObjectMapper;

@Converter(autoApply = false)
public class FootballMatchEventModifiedTablesConverter implements AttributeConverter<FootballMatchEventModifiedValues, String> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(FootballMatchEventModifiedValues attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to convert FootballMatchEventModifiedValues to JSON", e);
        }
    }

    @Override
    public FootballMatchEventModifiedValues convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        try {
            return MAPPER.readValue(dbData, FootballMatchEventModifiedValues.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to convert JSON to FootballMatchEventModifiedValues", e);
        }
    }
}