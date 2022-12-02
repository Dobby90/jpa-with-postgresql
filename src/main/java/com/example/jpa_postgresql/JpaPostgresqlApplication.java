package com.example.jpa_postgresql;

import com.example.jpa_postgresql.service.SchemaGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;

@Slf4j
@SpringBootApplication
public class JpaPostgresqlApplication {

	public JpaPostgresqlApplication(SchemaGenerator schemaGenerator) {
		this.schemaGenerator = schemaGenerator;
	}

	public static void main(String[] args) {
		SpringApplication.run(JpaPostgresqlApplication.class, args);
	}

	private final SchemaGenerator schemaGenerator;
	@EventListener(ApplicationReadyEvent.class)
	public void init() throws IOException, ClassNotFoundException {
		log.info("Application Ready!!");

		schemaGenerator.createSchema();
	}
}
