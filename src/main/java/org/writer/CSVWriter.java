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

    /**
     * Записывает список объектов в CSV файл.
     *
     * @param data список объектов для записи
     * @param fileName имя файла для записи
     * @throws IllegalArgumentException если передан пустой список
     * @throws CSVAnnotationNotFoundException если класс объекта не аннотирован @CSVEntity
     * @throws CSVWriteException если произошла ошибка при записи в файл
     */
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

    /**
     * Генерирует строку заголовка CSV на основе аннотаций полей класса.
     *
     * @param clazz класс объекта
     * @return строка с заголовками столбцов CSV
     */
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

    /**
     * Генерирует строку данных CSV для одного объекта.
     *
     * @param obj объект для преобразования в CSV строку
     * @return CSV строка с данными объекта
     * @throws CSVWriteException если произошла ошибка доступа к полю объекта
     */
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
