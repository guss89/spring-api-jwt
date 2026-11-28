package com.calisthenics.calisthenics.exception;

import org.springframework.security.access.AccessDeniedException;

/**
 * Regla de estilo: los errores de permiso lanzan ForbiddenOperationException o
 * AccessDeniedException, y ambos se responden con 403 por un unico handler
 * (GlobalExceptionHandler.handleAccessDenied).
 */
public class ForbiddenOperationException extends AccessDeniedException {
    public ForbiddenOperationException(String msg) {
        super(msg);
    }
}
