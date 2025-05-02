package org.writer;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.writer.exception.CSVAnnotationNotFoundException;
import org.writer.exception.CSVWriteException;
import org.writer.model.Months;
import org.writer.model.Person;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Тестовый класс для проверки функциональности CSVWriter.
 */
class CSVWriterTest {
    private final Faker faker = new Faker();
    private final CSVWriter csvWriter = new CSVWriter();
    @TempDir
    Path tempDir;

    /**
     * Тест проверяет выброс исключения при попытке записи пустого списка.
     */
    @Test
    void whenEmptyList_thenThrowIllegalArgumentException() {
        List<Person> emptyList = Collections.emptyList();
        assertThrows(IllegalArgumentException.class,
                () -> csvWriter.writeToFile(emptyList, "emptyList.csv"));
    }

    /**
     * Тест проверяет выброс исключения при попытке записи класса без аннотации @CSVEntity.
     */
    @Test
    void whenClassNotAnnotated_thenThrowCSVAnnotationNotFoundException() {
        class NotAnnotatedClass {
            private String field;
        }

        List<NotAnnotatedClass> data = List.of(new NotAnnotatedClass());
        assertThrows(CSVAnnotationNotFoundException.class,
                () -> csvWriter.writeToFile(data, "notAnnotated.csv"));
    }

    /**
     * Тест проверяет выброс исключения при ошибке записи в файл.
     *
     * @throws IOException если возникла ошибка при работе с файловой системой
     */
    @Test
    void whenInvalidFilePath_thenThrowCSVWriteException() throws IOException {
        List<Person> data = List.of(createTestPerson());
        // Используем заведомо неверный путь
        File tempFile = tempDir.resolve("temp.csv").toFile();

        tempFile.createNewFile();
        tempFile.setReadOnly();
        tempFile.deleteOnExit();

        assertThrows(CSVWriteException.class,
                () -> csvWriter.writeToFile(data, tempFile.getAbsolutePath()));
    }

    /**
     * Тест проверяет успешную запись данных в CSV файл.
     *
     * @throws IOException если возникла ошибка при чтении файла
     */
    @Test
    void whenValidData_thenCreateCorrectCSVFile() throws IOException {
        Person person = createTestPerson();
        List<Person> data = List.of(person);
        Path testFile = tempDir.resolve("success.csv");

        csvWriter.writeToFile(data, testFile.toString());

        assertTrue(Files.exists(testFile), "File should be created");
        String content = Files.readString(testFile);
        assertAll(
                () -> assertTrue(content.contains("First Name"), "Header should contain column names"),
                () -> assertTrue(content.contains(person.getFirstName()), "File should contain person data"),
                () -> assertEquals(2, content.lines().count(), "File should have header + 1 data row")
        );
    }

    /**
     * Создает тестовый объект Person со случайными данными.
     *
     * @return объект Person со случайными данными
     */
    private Person createTestPerson() {
        return Person.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .dayOfBirth(faker.number().numberBetween(1, 31))
                .monthOfBirth(Months.values()[faker.number().numberBetween(0, 11)])
                .yearOfBirth(faker.number().numberBetween(1970, 2000))
                .build();
    }
}