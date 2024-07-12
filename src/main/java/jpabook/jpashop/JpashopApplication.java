package jpabook.jpashop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {

		SpringApplication.run(JpashopApplication.class, args);
	}

	@Bean
	Hibernate5JakartaModule hibernate5JakartaModule() {
		Hibernate5JakartaModule hibernate5JakartaModule = new Hibernate5JakartaModule();
		return hibernate5JakartaModule;
	}	
}
