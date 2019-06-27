package org.tasklauncher.util;

import org.tasklauncher.domain.Task;

import javax.persistence.AttributeConverter;

public class EntityTypeEnumConverter implements AttributeConverter<Task, String> {
    @Override
    public String convertToDatabaseColumn(Task attribute) {
        return attribute.getDbValue();
    }

    @Override
    public Task convertToEntityAttribute(String dbData) {
        for(Task value : Task.values()) {
            if(value.getDbValue().equalsIgnoreCase(dbData)) return value;
        }
        return null;
    }
}
