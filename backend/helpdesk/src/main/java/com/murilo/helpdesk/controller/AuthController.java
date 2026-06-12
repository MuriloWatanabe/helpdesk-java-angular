package com.murilo.helpdesk.controller;

import com.murilo.helpdesk.dto.LoginRequest;
import com.murilo.helpdesk.dto.LoginResponse;
import com.murilo.helpdesk.dto.request.RegisterRequest;
import com.murilo.helpdesk.dto.request.UsuarioRequest;
import com.murilo.helpdesk.dto.response.UsuarioResponse;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.security.JwtService;
import com.murilo.helpdesk.security.UserDetailsServiceImpl;
import com.murilo.helpdesk.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Autenticação", description = "Login e geração de token JWT")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    @Operation(
        summary = "Autenticar usuário",
        description = "Autentica com email e senha, retorna JWT Bearer token"
    )
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // Valida credenciais — lança AuthenticationException se inválidas
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtService.generateToken(userDetails);

        Usuario usuario = usuarioService.findByEmail(request.email());
        Set<String> perfis = usuario.getPerfis().stream()
                .map(p -> p.getDescricao())
                .collect(Collectors.toSet());

        return ResponseEntity.ok(new LoginResponse(usuario.getId(), token, "Bearer", usuario.getNome(), request.email(), perfis));
    }

    @PostMapping("/register")
    @Operation(
        summary = "Cadastrar novo usuário",
        description = "Registra um novo usuário com perfil CLIENTE"
    )
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegisterRequest request) {
        UsuarioRequest usuarioRequest = new UsuarioRequest(request.nome(), request.email(), request.senha(), Set.of(1));
        UsuarioResponse criado = usuarioService.create(usuarioRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }
}
