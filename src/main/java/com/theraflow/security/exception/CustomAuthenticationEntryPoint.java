package com.theraflow.security.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorDetails details = getErrorDetails(request, authException);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), details);
    }

    private ErrorDetails getErrorDetails(HttpServletRequest request, AuthenticationException exception) {
        if (exception instanceof AccessTokenException e) {
            return new ErrorDetails(401, "TOKEN_EXPIRED", e.getMessage(), request.getServletPath());
        } else {
            return new ErrorDetails(401, "UNAUTHORIZED", exception.getMessage(), request.getServletPath());
        }
    }

    private record ErrorDetails(
            int status,
            String errorCode,
            String message,
            String path
    ) {
    }
}
