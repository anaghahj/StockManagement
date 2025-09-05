package com.ofss.util;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
 
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        // Why skip auth endpoints?
        // Register and login should be accessible without token
        if (path.startsWith("/api/auth")) {
            chain.doFilter(request, response);
            return;
        }

        
        // check header where frontend/client sends token
        String authHeader = req.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Why validate?
            // Make sure token is not expired or tampered
            if (jwtUtil.validateToken(token)) {
                chain.doFilter(request, response); //Continue
                return;
            }
        }

        //If no valid token
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.getWriter().write("Unauthorized or invalid token");
    }
}
