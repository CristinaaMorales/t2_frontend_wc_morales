package pe.edu.cibertec.patitas_frontend_wc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc.viewmodel.LoginModel;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    WebClient webClientAutenticacion;

    @GetMapping("/inicio")
    public String inicio(Model model) {
        LoginModel loginModel = new LoginModel("00", "", "");
        model.addAttribute("loginModel", loginModel);
        return "inicio"; // La vista Thymeleaf para login (inicio.html)
    }

    @PostMapping("/autenticar")
    public String autenticar(@RequestParam("tipoDocumento") String tipoDocumento,
                             @RequestParam("numeroDocumento") String numeroDocumento,
                             @RequestParam("password") String password,
                             Model model) {

        if (tipoDocumento == null || tipoDocumento.trim().isEmpty() ||
                numeroDocumento == null || numeroDocumento.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {

            LoginModel loginModel = new LoginModel("01", "Error: Debe completar correctamente sus credenciales", "");
            model.addAttribute("loginModel", loginModel);
            return "inicio"; // Retorna a la vista de login
        }

        try {
            // Invocar API de validación de usuario usando WebClient
            LoginRequestDTO loginRequestDTO = new LoginRequestDTO(tipoDocumento, numeroDocumento, password);

            Mono<LoginResponseDTO> monoLoginResponseDTO = webClientAutenticacion.post()
                    .uri("/login")
                    .body(Mono.just(loginRequestDTO), LoginRequestDTO.class)
                    .retrieve()
                    .bodyToMono(LoginResponseDTO.class);

            LoginResponseDTO loginResponseDTO = monoLoginResponseDTO.block();

            if ("00".equals(loginResponseDTO.codigo())) {
                LoginModel loginModel = new LoginModel("00", "", loginResponseDTO.nombreUsuario());
                model.addAttribute("loginModel", loginModel);
                return "principal"; // Vista principal después del login exitoso
            } else {
                LoginModel loginModel = new LoginModel("02", "Error: Autenticación fallida", "");
                model.addAttribute("loginModel", loginModel);
                return "inicio"; // Vista de login con error
            }

        } catch (Exception e) {
            LoginModel loginModel = new LoginModel("99", "Error: Ocurrió un problema en la autenticación", "");
            model.addAttribute("loginModel", loginModel);
            System.out.println("Error en autenticar: " + e.getMessage());
            return "inicio";
        }
    }

    @PostMapping("/logout")
    public String logout(@RequestParam("tipoDocumento") String tipoDocumento,
                         @RequestParam("numeroDocumento") String numeroDocumento,
                         Model model) {

        try {
            LogoutRequestDTO logoutRequestDTO = new LogoutRequestDTO(tipoDocumento, numeroDocumento);

            Mono<LogoutResponseDTO> monoLogoutResponseDTO = webClientAutenticacion.post()
                    .uri("/logout")
                    .body(Mono.just(logoutRequestDTO), LogoutRequestDTO.class)
                    .retrieve()
                    .bodyToMono(LogoutResponseDTO.class);

            LogoutResponseDTO logoutResponseDTO = monoLogoutResponseDTO.block();

            if ("00".equals(logoutResponseDTO.codigo())) {
                LoginModel loginModel = new LoginModel("00", "Sesión cerrada exitosamente", "");
                model.addAttribute("loginModel", loginModel);
                return "inicio"; // Redirige a la página de inicio después del logout
            } else {
                LoginModel loginModel = new LoginModel("02", "Error: No se pudo cerrar sesión", "");
                model.addAttribute("loginModel", loginModel);
                return "principal"; // Muestra un error en la vista principal
            }

        } catch (Exception e) {
            LoginModel loginModel = new LoginModel("99", "Error: Ocurrió un problema al cerrar sesión", "");
            model.addAttribute("loginModel", loginModel);
            System.out.println("Error en logout: " + e.getMessage());
            return "principal";
        }
    }
}
