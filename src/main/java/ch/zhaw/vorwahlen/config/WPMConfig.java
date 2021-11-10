package ch.zhaw.vorwahlen.config;

import ch.zhaw.vorwahlen.authentication.AuthFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;

/**
 * Profile configuration for running the application in development or production mode.
 */
@Configuration
public class WPMConfig {

    /**
     * Run the application with {@link AuthFilter} in development mode.
     * @return the AuthFilter with the dev user.
     */
    @Profile("dev")
    @Qualifier("authFilter")
    @Bean
    public AuthFilter developmentAuthFilter() {
        var userData = Map.of(
                "sessionId", "ABC123",
                "name", "dev",
                "lastName", "dev",
                "affiliation", "student;member",
                "homeOrg", "zhaw.ch",
                "mail", "dev@zhaw.ch",
                "role", "ADMIN"
        );

        var filter = new AuthFilter(false);
        filter.setUserData(userData);
        return filter;
    }

    /**
     * Run the application with the {@link AuthFilter} in production mode
     * @return the AuthFilter
     */
    @Profile("prod")
    @Qualifier("authFilter")
    @Bean
    public AuthFilter productionAuthFilter() {
        return new AuthFilter(true);
    }

}