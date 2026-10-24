
package com.mms;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    // CHECK WHETHER CURRENT USER IS ADMIN
    @ModelAttribute("isAdmin")
    public boolean isAdmin() {

        // GET CURRENT USER AUTHENTICATION
        Authentication authentication =
            SecurityContextHolder.getContext()
                .getAuthentication();

        // CHECK WHETHER USER IS LOGGED IN AS ADMIN
        return authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken)
            && authentication.getAuthorities().stream()
                .anyMatch(role ->
                    role.getAuthority().equals("ROLE_ADMIN")
                );
    }
}
