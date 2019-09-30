package com.document.example.config;

import com.document.example.commons.security.UserPrincipal;
import com.google.common.collect.Sets;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Log4j2
public class SecurityInterceptor extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader("X-userId");
        String rolesHeader = request.getHeader("X-roles");
        if (userId != null && rolesHeader != null) {
            String[] roles = rolesHeader.split(",");
            final UserPrincipal userPrincipal = UserPrincipal.builder()
                    .userId(UUID.fromString(userId))
                    .authorities(Sets.newHashSet(roles).stream()
                            .map(authority -> new StringBuilder("ROLE_").append(authority).toString())
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toSet()))
                    .build();
            SecurityContextHolder.getContext().setAuthentication(userPrincipal);
        }

        filterChain.doFilter(request, response);
    }
}
