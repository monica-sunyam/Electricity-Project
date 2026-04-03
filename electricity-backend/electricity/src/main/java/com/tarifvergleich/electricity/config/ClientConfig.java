package com.tarifvergleich.electricity.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;


@Configuration
public class ClientConfig {
	
	@Bean
	public RestClient.Builder restClientBuilder(){
		return RestClient.builder();
	}
	
	@Bean
	public RestClient energyApiClient(RestClient.Builder builder, @Value("${api.energy.url}")String url, @Value("${api.energy.apikey}") String token) {
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
		factory.setReadTimeout(Duration.ofSeconds(15));
		
		return builder
				.baseUrl(url)
				.requestFactory(factory)
				.defaultHeader("Accept", "application/json")
				.defaultHeader("Authorization", "Bearer "+token)
				.build();
	}

}
