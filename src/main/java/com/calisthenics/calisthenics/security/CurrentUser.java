package com.calisthenics.calisthenics.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

/**
 * Datos mínimos del usuario autenticado: su id y si es ADMIN o COACH.
 * Se construye desde el Authentication que deja JwtAuthenticationFilter
 * (nombre = id en texto, autoridad = ROLE_&lt;rol&gt;).
 */
public record CurrentUser(Long id, boolean admin, boolean coach, boolean client) {

    public CurrentUser(Long id, boolean admin, boolean coach) {
        this(id, admin, coach, false);
    }

    public CurrentUser(Long id, boolean admin) {
        this(id, admin, false, false);
    }

    public static CurrentUser from(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Access denied");
        }
        Long id;
        try {
            id = Long.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Access denied");
        }
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean coach = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_COACH".equals(a.getAuthority()));
        boolean client = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CLIENT".equals(a.getAuthority()));
        return new CurrentUser(id, admin, coach, client);
    }

    public boolean isOwner(Long userId) {
        return id.equals(userId);
    }

    /** Lanza AccessDeniedException (403) si no es ADMIN y el registro es de otro usuario. */
    public void requireAccessTo(Long ownerUserId) {
        if (!admin && !isOwner(ownerUserId)) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
