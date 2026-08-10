package com.murilo.helpdesk.controller;

import com.murilo.helpdesk.dto.LoginRequest;
import com.murilo.helpdesk.dto.LoginResponse;
import com.murilo.helpdesk.dto.request.AlterarSenhaRequest;
import com.murilo.helpdesk.dto.request.AtualizarPerfilRequest;
import com.murilo.helpdesk.dto.request.RecuperarSenhaRequest;
import com.murilo.helpdesk.dto.request.RedefinirSenhaRequest;
import com.murilo.helpdesk.dto.request.RegisterRequest;
import com.murilo.helpdesk.dto.request.UsuarioRequest;
import com.murilo.helpdesk.dto.response.MensagemResponse;
import com.murilo.helpdesk.dto.response.UsuarioResponse;
import com.murilo.helpdesk.model.Usuario;
import com.murilo.helpdesk.model.enums.Perfil;
import com.murilo.helpdesk.security.JwtService;
import com.murilo.helpdesk.security.UserDetailsServiceImpl;
import com.murilo.helpdesk.service.PasswordResetService;
import com.murilo.helpdesk.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Autenticação", description = "Login, cadastro, perfil e senha")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final UsuarioService usuarioService;
    private final PasswordResetService passwordResetService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;


    @PostMapping("/login")
    @Operation(summary = "Autenticar", description = "Valida as credenciais e devolve o token JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String email = request.email().trim().toLowerCase();


        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.senha()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(userDetails);

        Usuario usuario = usuarioService.findByEmail(email);
        usuarioService.registrarAcesso(email);

        Set<String> perfis = usuario.getPerfis().stream()
                .map(Perfil::getDescricao)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(new LoginResponse(
                usuario.getId(), token, "Bearer", usuario.getNome(), usuario.getEmail(), perfis,
                LocalDateTime.now().plusSeconds(jwtExpiration / 1000)));
    }

    @PostMapping("/register")
    @Operation(summary = "Criar conta", description = "Autocadastro — sempre com perfil CLIENTE")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegisterRequest request) {

        var usuarioRequest = new UsuarioRequest(
                request.nome(), request.email(), request.senha(),
                request.telefone(), null, true,
                Set.of(Perfil.CLIENTE.getCodigo()));

        UsuarioResponse criado = usuarioService.create(usuarioRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PostMapping("/recuperar-senha")
    @Operation(summary = "Solicitar link de redefinição de senha")
    public ResponseEntity<MensagemResponse> recuperarSenha(
            @Valid @RequestBody RecuperarSenhaRequest request) {

        var link = passwordResetService.solicitar(request.email());


        return ResponseEntity.ok(new MensagemResponse(
                "Se este e-mail estiver cadastrado, enviaremos as instruções de redefinição.",
                link.orElse(null)));
    }

    @PostMapping("/redefinir-senha")
    @Operation(summary = "Redefinir a senha usando o token recebido")
    public ResponseEntity<MensagemResponse> redefinirSenha(
            @Valid @RequestBody RedefinirSenhaRequest request) {
        passwordResetService.redefinir(request);
        return ResponseEntity.ok(MensagemResponse.de("Senha redefinida com sucesso."));
    }


    @GetMapping("/me")
    @Operation(summary = "Dados do usuário autenticado")
    public ResponseEntity<UsuarioResponse> me(Authentication auth) {
        Usuario usuario = usuarioService.findByEmail(auth.getName());
        return ResponseEntity.ok(usuarioService.findByIdAsResponse(usuario.getId()));
    }


    @PutMapping("/me")
    @Operation(summary = "Atualizar os próprios dados")
    public ResponseEntity<UsuarioResponse> atualizarMeuPerfil(
            @Valid @RequestBody AtualizarPerfilRequest request,
            Authentication auth) {
        return ResponseEntity.ok(usuarioService.atualizarMeuPerfil(auth.getName(), request));
    }

    @PostMapping("/alterar-senha")
    @Operation(summary = "Alterar a própria senha (exige a senha atual)")
    public ResponseEntity<MensagemResponse> alterarSenha(
            @Valid @RequestBody AlterarSenhaRequest request,
            Authentication auth) {
        usuarioService.alterarSenha(auth.getName(), request);
        return ResponseEntity.ok(MensagemResponse.de("Senha alterada com sucesso."));
    }
}
