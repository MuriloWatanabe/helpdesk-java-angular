package com.murilo.helpdesk.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Lido pelo JsonAuthenticationEntryPoint para explicar ao cliente por que o token foi recusado.
    public static final String ATRIBUTO_ERRO = JwtAuthenticationFilter.class.getName() + ".erro";

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.regionMatches(true, 0, PREFIXO, 0, PREFIXO.length())) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(PREFIXO.length()).trim();

        if (!jwt.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Long usuarioId = jwtService.extractUserId(jwtService.parseToken(jwt));
                UserDetails userDetails = userDetailsService.loadUserById(usuarioId);

                if (userDetails.isEnabled()) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    request.setAttribute(ATRIBUTO_ERRO, "Usuário inativo. Procure um administrador.");
                }
            } catch (ExpiredJwtException e) {
                request.setAttribute(ATRIBUTO_ERRO, "Sua sessão expirou. Faça login novamente.");
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
                request.setAttribute(ATRIBUTO_ERRO, "Token inválido. Faça login novamente.");
            }
        }

        filterChain.doFilter(request, response);
    }
}
