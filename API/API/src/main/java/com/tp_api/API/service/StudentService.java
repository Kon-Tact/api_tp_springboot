package com.tp_api.API.service;

import com.tp_api.API.model.Student;
import com.tp_api.API.repository.StudentRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

@Data
@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public Optional<Student> getStudent(final Long id) {
        return studentRepository.findById(id);
    }

    public Iterable<Student> getStudents() {
        return studentRepository.findAll();
    }

    public void deleteStudent (final Long id) {
        studentRepository.deleteById(id);
    }

    public Student saveStudent (Student student) {
        return studentRepository.save(student);
    }

    public void clearDB () {
        studentRepository.deleteAll();
    }
}
