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
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");
        String providerId = oauth2User.getAttribute("sub");

        Optional<User> userOptional = userRepository.findByEmail(email);

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update thông tin nếu cần
            user.setFullName(name);
            user.setPictureUrl(picture);
        } else {
            // Tạo user mới
            user = new User();
            user.setEmail(email);
            user.setFullName(name);
            user.setPictureUrl(picture);
            user.setProvider(AuthProvider.GOOGLE);
            user.setProviderId(providerId);
            user.setRole("customer");
            user.setEnabled(true);
        }

        userRepository.save(user);

        return new CustomOAuth2User(oauth2User);
    }
}
