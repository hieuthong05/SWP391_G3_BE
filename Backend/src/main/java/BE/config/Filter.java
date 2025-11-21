package BE.config;

import BE.exception.AuthenticationException;
import BE.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Filter extends OncePerRequestFilter {

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    @Autowired
    TokenService tokenService;

    private final List<String> PULIC_API = List.of(
            "POST:/api/admin/register",
            "POST:/api/customer/register",
            "POST:/api/auth/login",
            "GET: /api/reminders/customer/{customerId}",
            "POST:/api/auth/google",
            "POST:/api/auth/forgot-password",
            "POST:/api/auth/reset-password",

            "GET:/oauth2/**",
            "POST:/oauth2/**",
            "GET:/login/oauth2/**",
            "POST:/login/oauth2/**",
            "GET:/login/**",
            "POST:/login/**",

            "GET:/swagger-ui/**",
            "GET:/v3/api-docs/**",
            "GET:/swagger-resources/**",
            "GET:/webjars/**",
            "GET:/api/reminders/customer/{customerId}"
    );

    public boolean isPublicAPI(String uri, String method ){
        if ("OPTIONS".equals(method)) {
            return true;
        }
        AntPathMatcher matcher = new AntPathMatcher();

        return PULIC_API.stream().anyMatch(pattern ->{
            String[] parts = pattern.split(":", 2);
            if(parts.length != 2) return false;

            String allowedMethod = parts[0];
            String allowedUri = parts[1];

            return matcher.match(allowedUri, uri);
        });
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request
                                    , HttpServletResponse response
                                    , FilterChain filterChain) throws ServletException, IOException {
        System.out.println("filter running....");

        String uri = request.getRequestURI();
        String method = request.getMethod();

        System.out.println("🔍 Filter - URI: " + uri + " | Method: " + method);

        if(isPublicAPI(uri,method)){
            System.out.println("Public API - bypass filter");
            filterChain.doFilter(request, response);
            return;
        }else{

            String token = getToken(request);

            System.out.println("Token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "NULL"));

            if(token ==null) {
                System.err.println("No token found in request!");
                resolver.resolveException(request, response, null, new AuthenticationException("Empty token"));
                return;
            }
            try {
                System.out.println(" Processing token for: " + uri);
                tokenService.extractToken(token);

                //SỬA: Lấy subject (có thể là phone hoặc email)
                String subject = tokenService.extractPhone(token);
                System.out.println("👤 Token subject: " + subject);// subject = phone hoặc email

                //Load user by subject (tự động phân biệt phone/email)
                UserDetails userInfo = tokenService.loadUserBySubject(subject);
                System.out.println("User loaded: " + userInfo.getUsername());

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userInfo, null, userInfo.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                System.out.println("Authentication set successfully");

                filterChain.doFilter(request, response);

            }

            catch (ExpiredJwtException expiredJwtException) {
                resolver.resolveException(request, response, null,
                        new AuthenticationException("Expired token!"));
            } catch (MalformedJwtException malformedJwtException) {
                resolver.resolveException(request, response, null,
                        new AuthenticationException("Invalid token!"));
            } catch (Exception e) {
                resolver.resolveException(request, response, null,
                        new AuthenticationException("Token error: " + e.getMessage()));
            }

        }
    }

    public String getToken(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
