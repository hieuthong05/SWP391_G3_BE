package BE.config;

import BE.exception.AuthenticationException;
import BE.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
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
            "POST:/api/employee/register",
            "GET: /api/reminders/customer/{customerId}",
            "POST:/api/auth/google",


            "GET:/swagger-ui/**",
            "GET:/v3/api-docs/**",
            "GET:/swagger-resources/**",
            "GET:/webjars/**",
            "GET:/api/reminders/customer/{customerId}"
    );

    public boolean isPublicAPI(String uri, String method ){
        AntPathMatcher matcher = new AntPathMatcher();

//        if(method.equals("GET")) return true;
        if(uri.startsWith("/api/employees")) return true;

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

        if(isPublicAPI(uri,method)){
            filterChain.doFilter(request,response);
            return;
        }else{

            String token = getToken(request);
            if(token ==null) {
                resolver.resolveException(request, response, null, new AuthenticationException("Empty token"));
                return;
            }
            try {
                tokenService.extractToken(token);
                String phone = tokenService.extractPhone(token);
                var userInfo = tokenService.loadUserByPhone(phone);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userInfo,null,userInfo.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                filterChain.doFilter(request,response);

            }catch (ExpiredJwtException expiredJwtException){
                resolver.resolveException(request,response,null,new AuthenticationException("Expired token!"));
                return;
            }catch (MalformedJwtException malformedJwtException){
                resolver.resolveException(request,response,null,new AuthenticationException("Invalid token!"));
                return;
            }



        }
    }

    public String getToken(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        return authHeader.substring(7);
    }
}
