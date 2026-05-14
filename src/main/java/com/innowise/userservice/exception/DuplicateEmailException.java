package com.innowise.userservice.exception;

public class DuplicateEmailException extends RuntimeException{
    public DuplicateEmailException(String massage){
        super(massage);
    }
}