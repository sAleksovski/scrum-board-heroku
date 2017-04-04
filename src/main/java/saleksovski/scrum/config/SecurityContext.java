package saleksovski.scrum.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.social.security.SpringSocialConfigurer;
import saleksovski.auth.repository.UserRepository;
import saleksovski.auth.service.RepositoryUserDetailsService;
import saleksovski.auth.service.SimpleSocialUserDetailsService;

/**
 * Created by stefan on 1/14/16.
 */
@Configuration
@EnableWebSecurity
public class SecurityContext extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //Configures the logout function
                .logout()
                .deleteCookies("JSESSIONID")
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/")
                //Configures url based authorization
                .and()
                .authorizeRequests()
                //Anyone can access the urls
                .antMatchers(
                        "/",
                        "/dest/**",
                        "/auth/facebook",
                        "/auth/google",
                        "/auth/twitter",
                        "/auth/logout",
                        "/signup/**",
                        "/signin",
                        "/user/register/**",
                        "/api/ws/**"
                ).permitAll()
                //The rest of the our application is protected.
                .antMatchers("/**").hasRole("USER")
                //Adds the SocialAuthenticationFilter to Spring Security's filter chain.
                .and()
                .headers().frameOptions().sameOrigin()
                .and()
                .csrf().disable()
                .apply(getSprintSocialConfigurer());
    }

    private SpringSocialConfigurer getSprintSocialConfigurer() {
        SpringSocialConfigurer ssc = new SpringSocialConfigurer();
        ssc.alwaysUsePostLoginUrl(true);
        ssc.postLoginUrl("/");
        return ssc;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService())
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SocialUserDetailsService socialUserDetailsService() {
        return new SimpleSocialUserDetailsService(userDetailsService());
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new RepositoryUserDetailsService(userRepository);
    }

}
