package com.example.demo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DWDataSourceConfig {

    @Value("${dw.datasource.jdbc-url}")
    private String jdbcUrl;

    @Value("${dw.datasource.username}")
    private String username;

    @Value("${dw.datasource.password}")
    private String password;

    // ── MySQL (PRIMARY pour JPA) ──────────────────────────
    @Primary
    @Bean(name = "mysqlDataSourceProperties")
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties mysqlDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "dataSource")
    public DataSource mysqlDataSource(
            @Qualifier("mysqlDataSourceProperties")
            DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    // ── SQL Server DW (pour JdbcTemplate uniquement) ──────
    @Bean(name = "dwDataSource")
    public DataSource dwDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(
            "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        config.setMaximumPoolSize(5);
        config.setConnectionTimeout(30000);
        return new HikariDataSource(config);
    }

    @Bean(name = "dwJdbcTemplate")
    public JdbcTemplate dwJdbcTemplate(
            @Qualifier("dwDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}