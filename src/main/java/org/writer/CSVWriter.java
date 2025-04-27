package org.writer;

import org.writer.annotation.CSVEntity;
import org.writer.annotation.CSVField;
import org.writer.exception.CSVAnnotationNotFoundException;
import org.writer.exception.CSVWriteException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class CSVWriter implements Writable {
    @Override
    public void writeToFile(List<?> data, String fileName) throws IllegalArgumentException {

        if (data.isEmpty()) {
            throw new IllegalArgumentException("Data list cannot be empty");
        }

        Class<?> clazz = data.get(0).getClass();

        if (!clazz.isAnnotationPresent(CSVEntity.class)) {
            throw new CSVAnnotationNotFoundException("Class " + clazz.getSimpleName() +
                    " is not annotated with @CSVEntity");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Пишем заголовок CSV в файл
            writer.write(generateHeader(clazz));
            writer.newLine();

            // Пишем данные CSV в файл
            for (Object o : data) {
                writer.write(generateRow(o));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new CSVWriteException("Error writing to file " + fileName + ": " + e.getMessage());
        }
    }

    private String generateHeader(Class<?> clazz) {
        StringBuilder header = new StringBuilder();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(CSVField.class)) {
                CSVField annotation = field.getAnnotation(CSVField.class);
                String columnName = annotation.columnName().isEmpty()
                        ? field.getName()
                        : annotation.columnName();
                header.append(columnName).append(",");
            }
        }
        return header.substring(0, header.length() - 1);
    }

    private String generateRow(Object obj) {
        StringBuilder row = new StringBuilder();
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(CSVField.class)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    row.append(value != null ? value.toString() : "").append(",");
                } catch (IllegalAccessException e) {
                    throw new CSVWriteException("Failed to access field " + field.getName() + ": " + e.getMessage());
                }
            }
        }
        return row.substring(0, row.length() - 1);
    }

}
