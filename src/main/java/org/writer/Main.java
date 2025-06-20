package org.writer;

import org.writer.model.Months;
import org.writer.model.Person;
import org.writer.model.Student;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Person> people = List.of(
                Person.builder()
                        .firstName("John")
                        .lastName("Smith")
                        .dayOfBirth(15)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(1990)
                        .build(),

                Person.builder()
                        .firstName("Anna")
                        .lastName("Brown")
                        .dayOfBirth(22)
                        .monthOfBirth(Months.MARCH)
                        .yearOfBirth(1985)
                        .build()
        );

        List<Student> students = List.of(
                Student.builder()
                        .name("John")
                        .score(List.of("A", "B"))
                        .build()
        );

        Writable csvWriter = new CSVWriter();
        csvWriter.writeToFile(people, "people.csv");
        csvWriter.writeToFile(students, "students.csv");
    }
}