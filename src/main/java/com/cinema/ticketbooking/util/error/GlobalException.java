package com.cinema.ticketbooking.util.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.cinema.ticketbooking.domain.response.RestResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(value = { UsernameNotFoundException.class, IdInvalidException.class, NotFoundException.class })
    public ResponseEntity<RestResponse<Object>> handleNotFoundException(Exception ex) {
        RestResponse<Object> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.NOT_FOUND.value());
        response.setError(ex.getMessage());
        response.setMessage("Not found");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<RestResponse<Object>> handleBadCredentials(BadCredentialsException ex) {
        RestResponse<Object> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        response.setError(ex.getMessage());
        response.setMessage("Invalid username or password");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(value = { ResourceAlreadyExistsException.class, UnavailableResourceException.class })
    public ResponseEntity<RestResponse<Object>> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        RestResponse<Object> response = new RestResponse<>();

        response.setStatusCode(HttpStatus.CONFLICT.value()); // 409
        response.setError(ex.getMessage());
        response.setMessage("Resource unavailable");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(value = { BadRequestException.class, NoResourceException.class })
    public ResponseEntity<RestResponse<Object>> handleBadRequest(BadRequestException ex) {
        RestResponse<Object> response = new RestResponse<>();

        response.setStatusCode(HttpStatus.BAD_REQUEST.value()); // 400
        response.setError(ex.getMessage());
        response.setMessage("Bad request");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<RestResponse<Object>> handleApiException(ApiException ex) {
        RestResponse<Object> response = new RestResponse<>();

        response.setStatusCode(ex.getStatus().value());
        response.setError(ex.getMessage());
        response.setMessage("Error occurred");
        response.setData(null);

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(value = { DuplicateEmailException.class })
    public ResponseEntity<RestResponse<Object>> handleEmailDuplicate(Exception ex) {
        RestResponse<Object> restResponse = new RestResponse<Object>();
        restResponse.setStatusCode(HttpStatus.CONFLICT.value());
        restResponse.setError(ex.getMessage());
        restResponse.setMessage("Duplicate email in system");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(restResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        BindingResult result = exception.getBindingResult();
        final List<FieldError> fieldErrors = exception.getFieldErrors();

        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(exception.getBody().getDetail());

        List<String> errors = fieldErrors.stream().map(f -> f.getDefaultMessage()).collect(Collectors.toList());
        res.setMessage(errors.size() > 1 ? errors : errors.get(0));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
}
