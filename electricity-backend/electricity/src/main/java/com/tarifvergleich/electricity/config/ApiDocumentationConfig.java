package com.tarifvergleich.electricity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class ApiDocumentationConfig {

	@Bean
	public OpenAPI customerOpenAPI() {
		return new OpenAPI()
				.info(new Info().title("Tarifvergleich Electricity API").version("1.0.0")
						.description("Documentation for electricity tariff comparison service requests.")
						.contact(new Contact().name("Developer").email("admin@gmail.com")));
		
//		Uncomment this after implementing jwt
//				.addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
//				.components(new Components().addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
	}

//	Uncomment this after implementing jwt
//	private SecurityScheme createAPIKeyScheme() {
//		return new SecurityScheme().type(SecurityScheme.Type.HTTP).bearerFormat("JWT").scheme("bearer");
//	}
}
