package org.writer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.writer.annotation.CSVEntity;
import org.writer.annotation.CSVField;

import java.util.List;

@CSVEntity
@Data
@Builder
@AllArgsConstructor
public class Student {

    @CSVField
    private String name;

    private List<String> score;
}