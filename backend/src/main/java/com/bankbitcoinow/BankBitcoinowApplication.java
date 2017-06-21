package com.bankbitcoinow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SpringBootApplication
public class BankBitcoinowApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankBitcoinowApplication.class, args);
	}

	@Configuration
	@EnableWebSecurity
	public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

		@Autowired
		private UserDetailsService userDetailsService;

		@Bean
		public BCryptPasswordEncoder bCryptPasswordEncoder() {
			return new BCryptPasswordEncoder();
		}
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
					.authorizeRequests()
						.antMatchers("/resources/**", "/registration").permitAll()
						.anyRequest().authenticated()
						.and()
					.formLogin()
						.loginPage("/login")
						.permitAll()
						.successHandler(successHandler())
						.failureHandler(failureHandler())
						.and()
					.logout()
						.logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/login")
					.and().cors();
			http.csrf().disable().formLogin();

			http.authorizeRequests()
					.anyRequest().permitAll();
			http.csrf().disable();
		}

		@Autowired
		public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
			//auth.inMemoryAuthentication().withUser("JAJA").password("dddd").roles("ADMIN");
		}
		private AuthenticationSuccessHandler successHandler() {
			return new AuthenticationSuccessHandler() {
				@Override
				public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
					httpServletResponse.getWriter().append("OK");
					httpServletResponse.setStatus(200);
				}
			};
		}

		private AuthenticationFailureHandler failureHandler() {
			return new AuthenticationFailureHandler() {
				@Override
				public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
					httpServletResponse.getWriter().append("Authentication failure");
					httpServletResponse.setStatus(401);
				}
			};
		}
	}

	@Configuration
	public class MvcConfig extends WebMvcConfigurerAdapter {
		@Override
		public void addCorsMappings(CorsRegistry registry) {
			registry.addMapping("/**").allowedOrigins("*");
		}


	}
}
