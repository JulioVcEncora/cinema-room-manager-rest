package com.example.cinemaroom;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(PurchaseSeatResponseException.class)
    public ResponseEntity<CustomErrorMessage> handleBadPurchaseRequest(PurchaseSeatResponseException e, WebRequest request) {
        CustomErrorMessage body = new CustomErrorMessage(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReturnTicketResponseException.class)
    public ResponseEntity<CustomErrorMessage> handleBadReturnTicketRequest(ReturnTicketResponseException e, WebRequest request) {
        CustomErrorMessage body = new CustomErrorMessage(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatsException.class)
    public ResponseEntity<CustomErrorMessage> handleBadStatsRequest(ResponseStatsException e, WebRequest request) {
        CustomErrorMessage body = new CustomErrorMessage(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorMessage> handleOtherExceptions(Exception e, WebRequest request) {
        if (e instanceof org.springframework.web.bind.MissingServletRequestParameterException) {
            // Handle missing parameter error
            CustomErrorMessage body = new CustomErrorMessage(HttpStatus.UNAUTHORIZED.value(), "The password is wrong!");
            return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
        } else {
            // Handle other exceptions
            CustomErrorMessage body = new CustomErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred.");
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
