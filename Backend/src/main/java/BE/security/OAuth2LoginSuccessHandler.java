package BE.security;

import BE.entity.User;
import BE.repository.UserRepository;
import BE.service.TokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Value("${app.oauth2.redirect-url:http://localhost:5173/oauth2/redirect}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try
        {
            System.out.println("✅ OAuth2 Login Success Handler triggered");

            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");

            System.out.println("📧 Email: " + email);
            if (email == null || email.isEmpty())
            {
                System.err.println("❌ Email is null");
                response.sendRedirect("http://localhost:5173/login?error=no_email");
                return;
            }

            // Get user info
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            //Generate JWT token bằng TokenService (giống normal login)
            String token = tokenService.generateToken(user);

            System.out.println("🔑 Token generated successfully");

            //URL ENCODE tên và avatar để tránh lỗi Unicode
            String encodedName = URLEncoder.encode(
                    user.getFullName() != null ? user.getFullName() : "",
                    StandardCharsets.UTF_8
            );

            String encodedAvatar = URLEncoder.encode(
                    user.getPictureUrl() != null ? user.getPictureUrl() : "",
                    StandardCharsets.UTF_8
            );


            // Redirect to frontend with token
            String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                    .queryParam("token", token)
                    .queryParam("email", email)
                    .queryParam("name", encodedName)
                    .queryParam("avatar", encodedAvatar)
                    .queryParam("role", user.getRole())
                    .build(true)
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
        catch (Exception e)
        {
            System.err.println("❌ Error in OAuth2LoginSuccessHandler: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("http://localhost:5173/login?error=oauth_failed");
        }
    }
}
