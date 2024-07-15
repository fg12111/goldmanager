package com.my.goldmanager;

import java.util.stream.Stream;

import javax.sql.DataSource;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.support.DatabaseStartupValidator;

import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;

@SpringBootApplication
public class GoldmanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoldmanagerApplication.class, args);
	}

	@Bean
	public DatabaseStartupValidator databaseStartupValidator(DataSource dataSource) {
		DatabaseStartupValidator dsv = new DatabaseStartupValidator();
		dsv.setDataSource(dataSource);
		dsv.setInterval(5);
		dsv.setTimeout(1200);
		return dsv;
	}

	@Bean
	public static BeanFactoryPostProcessor dependsOnPostProcessor() {
	    return bf -> {
	        // Let beans that need the database depend on the DatabaseStartupValidator
	        // like the JPA EntityManagerFactory or Flyway
	        String[] liquibase = bf.getBeanNamesForType(SpringLiquibase.class);
	        Stream.of(liquibase)
	                .map(bf::getBeanDefinition)
	                .forEach(it -> it.setDependsOn("databaseStartupValidator"));

	        String[] jpa = bf.getBeanNamesForType(EntityManagerFactory.class);
	        Stream.of(jpa)
	                .map(bf::getBeanDefinition)
	                .forEach(it -> it.setDependsOn("databaseStartupValidator"));
	    };
	}


}
