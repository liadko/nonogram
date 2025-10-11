package com.liadkoren.nonogram.service.config;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Configuration
@Slf4j(topic = "h2.config")
public class InMemH2Config {
	@Bean(initMethod = "start", destroyMethod = "stop")
	public Server h2Server() throws SQLException {
		Server s = Server.createTcpServer(
				"-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
		log.info("H2 server started and listening on port {}", s.getPort());
		return s;
	}
}
