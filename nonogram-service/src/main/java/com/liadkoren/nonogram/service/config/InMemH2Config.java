package com.liadkoren.nonogram.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Configuration
public class InMemH2Config {
	@Bean(initMethod = "start", destroyMethod = "stop")
	public org.h2.tools.Server h2Server() throws SQLException {
		return org.h2.tools.Server.createTcpServer(
				"-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
	}
}
