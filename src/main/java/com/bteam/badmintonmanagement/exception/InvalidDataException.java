package com.bteam.badmintonmanagement.exception;

import java.util.InvalidPropertiesFormatException;

public class InvalidDataException extends RuntimeException{
    public InvalidDataException(String massage)
    {
        super(massage);
    }
}
