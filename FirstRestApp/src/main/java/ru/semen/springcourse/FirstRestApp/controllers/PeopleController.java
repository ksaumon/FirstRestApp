package ru.semen.springcourse.FirstRestApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.semen.springcourse.FirstRestApp.models.Person;
import ru.semen.springcourse.FirstRestApp.services.PeopleService;
import ru.semen.springcourse.FirstRestApp.util.PersonErrorResponse;
import ru.semen.springcourse.FirstRestApp.util.PersonNotCreatedException;
import ru.semen.springcourse.FirstRestApp.util.PersonNotFoundException;

import javax.validation.Valid;
import java.util.List;

@RestController // @Controller + @ResponseBody над каждым методом
@RequestMapping("/people")
public class PeopleController {

    private final PeopleService peopleService;

    @Autowired
    public PeopleController(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @GetMapping()
    public List<Person> getPeople() {
        return peopleService.findAll(); // Jackson конвертирует эти объекты в JSON
    }

    @GetMapping("/{id}")
    public Person getPerson(@PathVariable("id") int id) {
        return peopleService.findOne(id); // Jackson конвертирует в JSON
    }

    @PostMapping
    public ResponseEntity<HttpStatus> create(@RequestBody @Valid Person person, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error: errors) {
                errorMsg.append(error.getField()).append("-").append(error.getDefaultMessage()).append(";");
            }
            throw new PersonNotCreatedException(errorMsg.toString());
        }
        peopleService.save(person);
        return ResponseEntity.ok(HttpStatus.OK);//отправляем Http ответ с пустым телом и со статусом 200
    }

    @ExceptionHandler
    private ResponseEntity<PersonErrorResponse> handleException(PersonNotFoundException e) {
        PersonErrorResponse response = new PersonErrorResponse("Человек с таким id ненайден",
                System.currentTimeMillis());
        //в HTTP ответе тело ответа(response) и стотус в заголовке NOT_FOUND
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);//статус 404
    }

    @ExceptionHandler
    private ResponseEntity<PersonErrorResponse> handleException(PersonNotCreatedException e) {
        PersonErrorResponse response = new PersonErrorResponse(e.getMessage(),
                System.currentTimeMillis());
        //в HTTP ответе тело ответа(response) и стотус в заголовке NOT_FOUND
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);//статус 404
    }
}
