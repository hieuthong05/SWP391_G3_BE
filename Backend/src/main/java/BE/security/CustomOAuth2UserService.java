package BE.security;

import BE.entity.AuthProvider;
import BE.entity.User;
import BE.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try
        {
            OAuth2User oauth2User = super.loadUser(userRequest);

            System.out.println("✅ OAuth2User loaded: " + oauth2User.getAttributes());

            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String picture = oauth2User.getAttribute("picture");
            String providerId = oauth2User.getAttribute("sub");

            System.out.println("📧 Email: " + email);
            System.out.println("👤 Name: " + name);

            if (email == null || email.isEmpty())
            {
                throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
            }

            Optional<User> userOptional = userRepository.findByEmail(email);

            User user;
            if (userOptional.isPresent()) {
                user = userOptional.get();
                System.out.println("🔄 Updating existing user: " + user.getUserID());
                // Update thông tin nếu cần
                user.setFullName(name);
                user.setPictureUrl(picture);
            } else {
                System.out.println("✨ Creating new OAuth2 user");
                // Tạo user mới
                user = new User();
                user.setEmail(email);
                user.setFullName(name);
                user.setPictureUrl(picture);
                user.setProvider(AuthProvider.GOOGLE);
                user.setProviderId(providerId);
                user.setRole("customer");
                user.setEnabled(true);
                user.setStatus(true);
                user.setPhone(null);
                user.setPassword(null);

                //SET ref_id và ref_type = null cho OAuth2 users
                user.setRefId(null);
                user.setRefType(null);
            }

            userRepository.save(user);
            System.out.println("💾 User saved successfully with ID: " + user.getUserID());

            return new CustomOAuth2User(oauth2User);
        }
        catch (Exception e)
        {
            System.err.println("❌ Error in CustomOAuth2UserService: " + e.getMessage());
            e.printStackTrace();
            throw new OAuth2AuthenticationException("OAuth2 authentication failed: " + e.getMessage());
        }
    }
}
