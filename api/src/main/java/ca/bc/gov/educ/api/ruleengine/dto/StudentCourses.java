package ca.bc.gov.educ.api.ruleengine.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Data
@Component
public class StudentCourses implements Serializable {
    private List<StudentCourse> studentCourseList;

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("");

        for (StudentCourse sc : studentCourseList) {
            output.append(sc.toString())
                    .append("\n");
        }
        return output.toString();
    }
}