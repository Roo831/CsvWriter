package org.writer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.writer.annotation.CSVEntity;
import org.writer.annotation.CSVField;
import org.writer.exception.CSVAnnotationNotFoundException;
import org.writer.exception.CSVWriteException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Реализация записи объектов в CSV файл с использованием аннотаций.
 * Поддерживает кастомные форматы через {@link CSVFormat}.
 * Требует наличия аннотаций {@link CSVEntity} и {@link CSVField}.
 */
public class CSVWriter implements Writable {


    private final CSVFormat csvFormat;

    public CSVWriter() {
        this(CSVFormat.DEFAULT
                .withIgnoreHeaderCase()
                .withTrim());
    }

    public CSVWriter(CSVFormat csvFormat) {
        this.csvFormat = Objects.requireNonNull(csvFormat, "CSVFormat cannot be null");
    }

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
    public void writeToFile(List<?> data, String fileName) {
        validateInput(data, fileName);

        Class<?> clazz = data.get(0).getClass();
        validateCSVAnnotations(clazz);

        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(fileName));
             CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {

            printer.printRecord(getHeaderNames(clazz));
            printDataRecords(printer, data);

        } catch (IOException e) {
            throw new CSVWriteException("Failed to write CSV to file: " + fileName);
        }
    }

    private void validateInput(List<?> data, String fileName) {
        if (CollectionUtils.isEmpty(data)) {
            throw new IllegalArgumentException("Data list cannot be null or empty");
        }
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("File name cannot be blank");
        }
    }

    private void validateCSVAnnotations(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(CSVEntity.class)) {
            throw new CSVAnnotationNotFoundException(
                    "Class " + clazz.getSimpleName() + " must be annotated with @CSVEntity");
        }

        boolean hasCSVFields = Arrays.stream(clazz.getDeclaredFields())
                .anyMatch(f -> f.isAnnotationPresent(CSVField.class));

        if (!hasCSVFields) {
            throw new CSVAnnotationNotFoundException(
                    "Class " + clazz.getSimpleName() + " must have at least one field with @CSVField");
        }
    }

    private List<String> getHeaderNames(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(CSVField.class))
                .map(f -> {
                    CSVField annotation = f.getAnnotation(CSVField.class);
                    return StringUtils.defaultIfBlank(annotation.columnName(), f.getName());
                })
                .collect(Collectors.toList());
    }

    private void printDataRecords(CSVPrinter printer, List<?> data) throws IOException {
        for (Object item : data) {
            printer.printRecord(getFieldValues(item));
        }
    }

    private List<Object> getFieldValues(Object obj) {
        return Arrays.stream(obj.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(CSVField.class))
                .map(f -> {
                    try {
                        f.setAccessible(true);
                        return Objects.toString(f.get(obj), "");
                    } catch (IllegalAccessException e) {
                        throw new CSVWriteException("Failed to access field " + f.getName());
                    }
                })
                .collect(Collectors.toList());
    }
}
