package com.thelastcodebenders.follower.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthFailureHandler implements AuthenticationFailureHandler {
    private static final Logger log = LoggerFactory.getLogger(AuthFailureHandler.class);

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.info("Auth Failure ! IP -> " + request.getRemoteAddr());
        //User is disabled
        //Bad credentials
        String url = "";
        if (exception.getMessage().equals("User is disabled")){
            url = "/login-failure/1";
        }
        else{
            url = "/login-failure/2";
        }

        redirectStrategy.sendRedirect(request, response, url);
    }
}
