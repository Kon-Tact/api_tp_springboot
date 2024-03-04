package com.tp_api.API.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tp_api.API.model.Student;
import com.tp_api.API.service.StudentService;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.logging.Logger;

@RestController
@CrossOrigin(origins = "*", methods = {
        RequestMethod.OPTIONS,
        RequestMethod.DELETE,
        RequestMethod.POST,
        RequestMethod.GET,
        RequestMethod.HEAD,
        RequestMethod.PATCH,
        RequestMethod.PUT,
        RequestMethod.TRACE
})
public class StudentController {

    public enum Status {
        SUCCESS,
        ERROR
    }
    private static final Logger log = Logger.getLogger(StudentController.class.getName());
    @Autowired
    private StudentService studentService;

    public String consoleFormat(Status status, String message) {

        String successColor = "\u001B[32m";
        String errorColor = "\u001B[31m";
        String resetColor = "\u001B[0m";
        String finalMessage = "";

        return switch (status) {
            case SUCCESS -> finalMessage = successColor + message + resetColor;
            case ERROR -> finalMessage = errorColor + message + resetColor;
        };
    }

    private HttpHeaders createCORSHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Headers", "Content-Type, Origin, X-Requested-With, Accept, Content, Authorization");
        return headers;
    }

    @RequestMapping(value = "/save", method = {RequestMethod.OPTIONS, RequestMethod.POST})
    public ResponseEntity<Student> handleOptionsSave() {
        HttpHeaders headers = createCORSHeaders();
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/delete", method = {RequestMethod.OPTIONS, RequestMethod.DELETE})
    public ResponseEntity<Student> handleOptionsDelete() {
        HttpHeaders headers = createCORSHeaders();
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }
    @GetMapping("/student/list")
    public Iterable<Student> getStudents() {
        Iterable<Student> studentList = null;
        try {
            studentList = studentService.getStudents();
            log.info(consoleFormat(Status.SUCCESS,
                    "[Get Student List - DONE]  -- Status : " + HttpStatus.OK));
            return studentList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/student")
    public Optional<Student> getStudent(final Long id) {
        Optional<Student> student = Optional.empty();
        try {
            student = studentService.getStudent(id);
            log.info(consoleFormat(Status.SUCCESS,
                    "[Get Student - DONE]  -- Status : " + HttpStatus.OK));
            return student;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //@CrossOrigin(origins = "*", methods = {RequestMethod.DELETE, RequestMethod.OPTIONS})
    @DeleteMapping("/student/delete")
    public ResponseEntity<String> deleteStudent (@RequestParam final Long id) {
        try {
            studentService.deleteStudent(id);
            log.info(consoleFormat(Status.SUCCESS,
                    "[Delete Student - DONE]  -- Status : " + HttpStatus.OK));
            String strResMessage = "{\"Status\": \"200\", \"Méthode\": \"Delete Student\"}";
            return new ResponseEntity<>( strResMessage, createCORSHeaders() , HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/student/clear")
    public ResponseEntity<String> deleteAllStudents() {
        try {
            studentService.clearDB();
            log.info(consoleFormat(Status.SUCCESS,
                    "[Base clear - DONE]  -- Status : " + HttpStatus.OK));
            return new ResponseEntity<>("[Base Clear - DONE]", HttpStatus.OK);
        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Base clear - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR));
            return new ResponseEntity<>("[Base Clear - KO]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/student/save")
    public ResponseEntity<Student> saveStudent (@RequestBody Student student) {
        Student newStudent = null;
        try {
            newStudent = studentService.saveStudent(student);
            log.info(consoleFormat(Status.SUCCESS, "[Save Student - DONE]  -- Status : " + HttpStatus.OK));
            return new ResponseEntity<>(newStudent, createCORSHeaders(), HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
