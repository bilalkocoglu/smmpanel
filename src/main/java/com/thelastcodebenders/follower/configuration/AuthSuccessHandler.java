package com.thelastcodebenders.follower.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthSuccessHandler.class);

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        String role = authentication.getAuthorities().toString();
        String targetUrl = "";
        if (role.contains("ADMIN")){
            targetUrl = "/admin/dashboard";
        }else if (role.contains("USER")){
            targetUrl = "/user/dashboard";
        }
        log.info("Success Login Handler ! IP -> " + httpServletRequest.getRemoteAddr());
        log.info("Success Login Handler ! IP -> " + httpServletRequest.getLocalAddr());
        log.info("Success Login Handler ! IP -> " + httpServletRequest.getHeader("request_id"));
        log.info("Success Login Handler ! IP -> " + httpServletRequest.getHeader("fwd"));


        redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse, targetUrl);
    }
}
