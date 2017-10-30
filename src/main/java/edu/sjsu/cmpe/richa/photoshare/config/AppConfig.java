package edu.sjsu.cmpe.richa.photoshare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages={"edu.sjsu.cmpe.richa.photoshare"})
@PropertySource("classpath:app.properties")
public class AppConfig {
    // Spring will scan the Components and do Dependency Injection by itself for Beans not specified here.
	
	/**
	 * Load the properties in Spring Context. Will also make @Value inject work.
	 * @return
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}
