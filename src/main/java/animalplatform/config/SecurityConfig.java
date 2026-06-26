package animalplatform.config;

import animalplatform.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // Pages et ressources statiques publiques
                        .requestMatchers("/", "/index", "/home").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()

                        // ✅ ENDPOINTS API AUTH (inscription, connexion)
                        .requestMatchers("/api/auth/**").permitAll()

                        // ✅ ENDPOINTS AUTH (pour compatibilité)
                        .requestMatchers("/auth/**").permitAll()

                        // ✅ Pages d'adoption publiques (toutes accessibles sans login)
                        .requestMatchers("/adoption", "/adoption/**").permitAll()

                        // ✅ Pages animaux perdus publiques
                        .requestMatchers("/perdus", "/perdus/**").permitAll()

                        // ✅ Pages de sensibilisation publiques
                        .requestMatchers("/sensibilisation/**").permitAll()

                        // ✅ Forum public (lecture seule sans login)
                        .requestMatchers("/forum", "/forum/**").permitAll()

                        // ✅ Pages d'erreur
                        .requestMatchers("/error/**").permitAll()

                        // ❌ Aucun endpoint favoris car supprimé
                        // Toutes les autres requêtes nécessitent une authentification
                        .anyRequest().authenticated()
                )
                // Désactiver les formulaires par défaut car on utilise des modales
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())

                // Configuration du logout
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )

                // Gestion des erreurs d'accès
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/error/403")
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8080", "http://127.0.0.1:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}