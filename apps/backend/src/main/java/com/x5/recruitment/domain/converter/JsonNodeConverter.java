package com.x5.recruitment.domain.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;

/**
 * Converts {@link JsonNode} entities to PostgreSQL JSONB and back.
 */
@Converter(autoApply = true)
@Slf4j
public class JsonNodeConverter implements AttributeConverter<JsonNode, PGobject> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public PGobject convertToDatabaseColumn(JsonNode attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            PGobject pgObject = new PGobject();
            pgObject.setType("jsonb");
            pgObject.setValue(MAPPER.writeValueAsString(attribute));
            return pgObject;
        } catch (Exception e) {
            log.error("Failed to convert JsonNode to PGobject", e);
            throw new IllegalStateException("Could not persist JSON attribute", e);
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(PGobject dbData) {
        if (dbData == null || dbData.getValue() == null) {
            return null;
        }

        try {
            return MAPPER.readTree(dbData.getValue());
        } catch (Exception e) {
            log.error("Failed to convert PGobject to JsonNode", e);
            throw new IllegalStateException("Could not read JSON attribute", e);
        }
    }
}
