package com.vm325.inventory_back.services;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public void addHttpOnlyCookie(String name, String value, int maxAge, HttpServletResponse response){
        String cookieValue = String.format("%s=%s; Max-Age=%d; Path=/; Secure; HttpOnly; SameSite=None",
                name, value, maxAge);

        response.addHeader("Set-Cookie", cookieValue);
    }

    public void deleteCookie(String name, HttpServletResponse response){
        String cookieValue = String.format("%s=; Max-Age=0; Path=/; Secure; HttpOnly; SameSite=None", name);
        response.addHeader("Set-Cookie", cookieValue);
    }
}
