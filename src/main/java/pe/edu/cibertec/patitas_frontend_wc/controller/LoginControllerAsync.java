package pe.edu.cibertec.patitas_frontend_wc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutResponseDTO;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginControllerAsync {

    @Autowired
    WebClient webClientAutenticacion;

    @PostMapping("/autenticar-async")
    public Mono<LoginResponseDTO> autenticar(@RequestBody LoginRequestDTO loginRequestDTO) {
        if (loginRequestDTO.tipoDocumento() == null || loginRequestDTO.tipoDocumento().trim().isEmpty() ||
                loginRequestDTO.numeroDocumento() == null || loginRequestDTO.numeroDocumento().trim().isEmpty() ||
                loginRequestDTO.password() == null || loginRequestDTO.password().trim().isEmpty()) {
            return Mono.just(new LoginResponseDTO("01", "Error: Debe completar correctamente sus credenciales", "", ""));
        }

        return webClientAutenticacion.post()
                .uri("/login")
                .body(Mono.just(loginRequestDTO), LoginRequestDTO.class)
                .retrieve()
                .bodyToMono(LoginResponseDTO.class)
                .flatMap(response -> {
                    if ("00".equals(response.codigo())) {
                        return Mono.just(new LoginResponseDTO("00", "", response.nombreUsuario(), ""));
                    } else {
                        return Mono.just(new LoginResponseDTO("02", "Error: Autenticación fallida", "", ""));
                    }
                })
                .onErrorResume(e -> {
                    System.out.println("Error en autenticar-async: " + e.getMessage());
                    return Mono.just(new LoginResponseDTO("99", "Error: Ocurrió un problema en la autenticación", "", ""));
                });
    }

    @PostMapping("/logout-async")
    public Mono<LogoutResponseDTO> logout(@RequestBody LogoutRequestDTO logoutRequestDTO) {
        if (logoutRequestDTO.tipoDocumento() == null || logoutRequestDTO.tipoDocumento().trim().isEmpty() ||
                logoutRequestDTO.numeroDocumento() == null || logoutRequestDTO.numeroDocumento().trim().isEmpty()) {
            return Mono.just(new LogoutResponseDTO("01", "Error: Debe completar correctamente los datos para cerrar sesión"));
        }

        return webClientAutenticacion.post()
                .uri("/logout")
                .body(Mono.just(logoutRequestDTO), LogoutRequestDTO.class)
                .retrieve()
                .bodyToMono(LogoutResponseDTO.class)
                .flatMap(response -> {
                    if ("00".equals(response.codigo())) {
                        return Mono.just(new LogoutResponseDTO("00", "Logout exitoso"));
                    } else {
                        return Mono.just(new LogoutResponseDTO("02", "Error: Logout fallido"));
                    }
                })
                .onErrorResume(e -> {
                    System.out.println("Error en logout-async: " + e.getMessage());
                    return Mono.just(new LogoutResponseDTO("99", "Error: Ocurrió un problema en el logout"));
                });
    }
}
