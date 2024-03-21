package example.api.controller;

import example.api.config.JwtTokenProvider;
import example.api.model.Student;
import example.api.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Autowired
    public JwtTokenProvider jwtTokenProvider;
    private static final Logger log = Logger.getLogger(StudentController.class.getName());
    @Autowired
    private StudentService studentService;

    public void checkToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            log.info(token);
        }
    }

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
        try {
            HttpHeaders headers = createCORSHeaders();
            return new ResponseEntity<>(headers, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/delete", method = {RequestMethod.OPTIONS, RequestMethod.DELETE})
    public ResponseEntity<Student> handleOptionsDelete() {
        try {
            HttpHeaders headers = createCORSHeaders();
            return new ResponseEntity<>(headers, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //Niveau d'autorisation : Tout le monde
    @GetMapping("/student/list")
    public Iterable<Student> getStudents(HttpServletRequest request) {
        Iterable<Student> studentList = null;

        checkToken(request);

        try {
            studentList = studentService.getStudents();
            log.info(consoleFormat(Status.SUCCESS,
                    "[Get Student List - DONE]  -- Status : " + HttpStatus.OK));
            return studentList;
        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Get Student List - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR));
            throw new RuntimeException(e);
        }
    }

    //Niveau d'autorisation : User
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @GetMapping("/student/")
    public Optional<Student> getStudent(final Long id, HttpServletRequest r) {

        Optional<Student> student = Optional.empty();
        //La classe Optional permet la gestion de null car elle renvoit l'élément attendu ou rien

        checkToken(r);

        try {
            student = studentService.getStudent(id);
            log.info(consoleFormat(Status.SUCCESS,
                    "[Get Student - DONE]  -- Status : " + HttpStatus.OK));
            return student;
        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Get Student - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR));
            throw new RuntimeException(e);
        }
    }

    //Niveau d'autorisation : Admin
    //@PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/student/delete")
    public ResponseEntity<String> deleteStudent (@RequestParam final Long id) {
        try {
            studentService.deleteStudent(id);
            log.info(consoleFormat(Status.SUCCESS,
                    "[Delete Student - DONE]  -- Status : " + HttpStatus.OK));
            String successMessage = "{\"Status\": \"200\", \"Méthode\": \"Delete student\"}";
            return new ResponseEntity<>(successMessage, createCORSHeaders() , HttpStatus.OK);
        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Base clear - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR));
            String errorMessage = "{\"Status\": \"Error\", \"Méthode\": \"Delete student\"}";
            return new ResponseEntity<>(errorMessage, createCORSHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //Niveau d'autorisation : Admin
    //@PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/student/clear")
    public ResponseEntity<String> deleteAllStudents(HttpServletRequest r) {
        try {
            studentService.clearDB();
            log.info(consoleFormat(Status.SUCCESS,
                    "[Base clear - DONE]  -- Status : " + HttpStatus.OK));
            String successMessage = "{\"Status\": \"200\", \"Méthode\": \"Clear base\"}";
            return new ResponseEntity<>(successMessage, createCORSHeaders(), HttpStatus.OK);
        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Base clear - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR));
            String errorMessage = "{\"Status\": \"Error\", \"Méthode\": \"Clear base\"}";
            return new ResponseEntity<>(errorMessage, createCORSHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Niveau d'autorisation : User
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @PostMapping("/student/save")
    public ResponseEntity<Student> saveStudent (@RequestBody Student student) {
        Student newStudent = null;
        try {
            newStudent = studentService.saveStudent(student);
            log.info(consoleFormat(Status.SUCCESS, "[Save Student - DONE]  -- Status : " + HttpStatus.OK));
            return new ResponseEntity<>(newStudent, createCORSHeaders(), HttpStatus.OK);
        } catch (Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Save Student - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR));
            return new ResponseEntity<>(newStudent, createCORSHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Method for editing student
    //@PreAuthorize("hasAuthority('USER') or hasAuthority('ADMIN')")
    @PutMapping("/student/edit")
    public ResponseEntity<Student> editStudent (@RequestBody Student student) {

        try {
            Optional<Student> existingStudent = studentService.getStudent(student.getId());

            if (existingStudent.isPresent()) {

                Student newStudent = studentService.saveStudent(student);
                log.info(consoleFormat(Status.SUCCESS, "[Edit Student - DONE]  -- Status : " + HttpStatus.OK));
                return new ResponseEntity<>(newStudent, createCORSHeaders(), HttpStatus.OK);

            } else {

                log.severe(consoleFormat(Status.ERROR, "[Edit Student - KO]  -- Status : " + HttpStatus.NOT_FOUND));
                return new ResponseEntity<>(createCORSHeaders(), HttpStatus.NOT_FOUND);

            }

        } catch ( Exception e) {
            log.severe(consoleFormat(Status.ERROR,
                    "[Edit Student - KO]  -- Status : " + HttpStatus.INTERNAL_SERVER_ERROR + e.getMessage()));
            // e.printStackTrace();
            return new ResponseEntity<>(createCORSHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}