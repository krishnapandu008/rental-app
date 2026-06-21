package com.rental.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpServletRequest;

// @Controller
public class ReactRoutingForwardController {

    @RequestMapping(value = "/**")
    public String forward(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 1. Skip API calls
        if (path.startsWith("/api/") || path.equals("/api")) {
            return null; // let REST controllers handle
        }

        // 2. Skip actual static files (those with a file extension)
        if (path.contains(".")) {
            return null; // let static resource handling serve the file
        }

        // 3. Skip if already forwarding to index.html (avoid loop)
        if (path.equals("/index.html")) {
            return null;
        }

        // 4. Forward all other requests (React Router paths) to index.html
        return "forward:/index.html";
    }
}