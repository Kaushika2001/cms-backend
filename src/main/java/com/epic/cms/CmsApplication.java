package com.epic.cms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@Slf4j
public class CmsApplication {

	public static void main(String[] args) throws UnknownHostException {
		log.info("========================================");
		log.info("Starting Card Management System (CMS)...");
		log.info("========================================");
		
		ConfigurableApplicationContext context = SpringApplication.run(CmsApplication.class, args);
		
		Environment env = context.getEnvironment();
		String protocol = "http";
		String serverPort = env.getProperty("server.port", "8080");
		String contextPath = env.getProperty("server.servlet.context-path", "/");
		String hostAddress = InetAddress.getLocalHost().getHostAddress();
		
		log.info("\n----------------------------------------------------------\n" +
				"Application '{}' is running! Access URLs:\n" +
				"Local: \t\t{}://localhost:{}{}\n" +
				"External: \t{}://{}:{}{}\n" +
				"Profile(s): \t{}\n" +
				"API Docs: \t{}://localhost:{}/swagger-ui.html\n" +
				"----------------------------------------------------------\n" +
				"AUTHENTICATION: DISABLED - All endpoints are publicly accessible\n" +
				"----------------------------------------------------------",
				env.getProperty("spring.application.name", "CMS"),
				protocol,
				serverPort,
				contextPath,
				protocol,
				hostAddress,
				serverPort,
				contextPath,
				env.getActiveProfiles().length == 0 ? 
					env.getDefaultProfiles() : env.getActiveProfiles(),
				protocol,
				serverPort
		);
	}

}
