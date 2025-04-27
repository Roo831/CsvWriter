package org.writer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.writer.annotation.CSVEntity;
import org.writer.annotation.CSVField;

@CSVEntity
@Data
@Builder
@AllArgsConstructor
public class Person {

    @CSVField(columnName = "First Name")
    private String firstName;

    @CSVField(columnName = "Last Name")
    private String lastName;

    @CSVField(columnName = "Day of Birth")
    private int dayOfBirth;

    @CSVField(columnName = "Month of Birth")
    private Months monthOfBirth;

    @CSVField(columnName = "Year of Birth")
    private int yearOfBirth;

}
