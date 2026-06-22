package com.adilsonTI.digital_banking_api.configurations;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErroResposta handleMethodNotValidException(MethodArgumentNotValidException e){

        List<FieldError> errorList = e.getFieldErrors();
        List<ErroCampo> listaErros = errorList.stream()
                .map(fieldError ->
                        new ErroCampo(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ErroResposta(HttpStatus.UNPROCESSABLE_ENTITY.value(),"Erro validação", listaErros);
    };

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErroTransferencia handleIllegalStateException(IllegalStateException e) {
        return new ErroTransferencia(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroTransferencia handleIllegalArgumentException(IllegalArgumentException e) {
        return new ErroTransferencia(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

}
