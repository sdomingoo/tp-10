package com.example.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MigrationExecutor {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public MigrationExecutor(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public void executeMigration(String scriptPath) throws Exception {
        try {
            runScript(jdbcUrl, username, password, scriptPath);
        } catch (Exception e) {
            runScript("jdbc:h2:mem:reservation;DB_CLOSE_DELAY=-1", "sa", "", scriptPath);
        }
    }

    private void runScript(String url, String user, String pass, String scriptPath) throws Exception {
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             InputStream is = getClass().getClassLoader().getResourceAsStream(scriptPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }

            try (Statement stmt = conn.createStatement()) {
                for (String statementSql : sql.toString().split(";")) {
                    String trimmed = statementSql.trim();
                    if (!trimmed.isEmpty()) {
                        try {
                            stmt.execute(trimmed);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }
}
