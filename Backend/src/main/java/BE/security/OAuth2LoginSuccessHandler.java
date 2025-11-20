package BE.security;

import BE.entity.Customer;
import BE.entity.Employee;
import BE.entity.User;
import BE.repository.CustomerRepository;
import BE.repository.EmployeeRepository;
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
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

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
            System.out.println("🔑 Token generated: " + token.substring(0, 30) + "...");
            System.out.println("📋 Token details:");
            System.out.println("   - Subject: " + tokenService.extractPhone(token));
            System.out.println("   - Email from token: " + tokenService.extractEmail(token));
            System.out.println("   - Expired: " + tokenService.isTokenExpired(token));


            System.out.println("🔑 Token generated successfully");

            String phone = "";
            String address = "";
            String birth = "";
            String gender = "";

            // 2. Lấy dữ liệu từ bảng Customer hoặc Employee dựa trên Role và RefId
            if (user.getRefId() != null) {
                String role = user.getRole().toLowerCase();

                if ("customer".equals(role)) {
                    Customer customer = customerRepository.findById(user.getRefId()).orElse(null);
                    if (customer != null) {
                        phone = customer.getPhone();
                        address = customer.getAddress();
                        gender = customer.getGender();
                        birth = customer.getBirth() != null ? customer.getBirth().toString() : "";
                    }
                }
                else if ("staff".equals(role) || "technician".equals(role)) {
                    Employee employee = employeeRepository.findById(user.getRefId()).orElse(null);
                    if (employee != null) {
                        phone = employee.getPhone();
                        address = employee.getAddress();
                        gender = employee.getGender();
                        birth = employee.getBirth() != null ? employee.getBirth().toString() : "";
                    }
                }
            }

            // Fallback: Nếu trong bảng chi tiết chưa có sđt, lấy tạm từ bảng User
            if (phone == null || phone.isEmpty()) {
                phone = user.getPhone() != null ? user.getPhone() : "";
            }

            // Xử lý null cho các trường string để tránh lỗi
            address = address != null ? address : "";
            gender = gender != null ? gender : "";

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
//            String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
//                    .queryParam("token", token)
//                    .queryParam("email", email)
//                    .queryParam("name", encodedName)
//                    .queryParam("avatar", encodedAvatar)
//                    .queryParam("role", user.getRole())
//                    .build(true)
//                    .toUriString();

            // Encode địa chỉ và giới tính
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String encodedGender = URLEncoder.encode(gender, StandardCharsets.UTF_8);

            // Cập nhật targetUrl với đầy đủ tham số
            String targetUrl = redirectUrl +
                    "?token=" + token +
                    "&email=" + user.getEmail() +
                    "&name=" + encodedName +
                    "&avatar=" + encodedAvatar +
                    "&role=" + user.getRole() +
                    "&userID=" + user.getUserID() +
                    "&refId=" + (user.getRefId() != null ? user.getRefId() : "") +
                    // Gắn thêm các trường vừa lấy được
                    "&phone=" + phone +
                    "&address=" + encodedAddress +
                    "&birth=" + birth +
                    "&gender=" + encodedGender;

            System.out.println("🔄 Redirecting to: " + targetUrl.substring(0, Math.min(100, targetUrl.length())) + "...");
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
