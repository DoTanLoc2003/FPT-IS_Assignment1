package com.assignment1.keycloak.provider;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserProvider;
import org.keycloak.storage.UserStorageProviderFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class UserFederationStorageProviderFactory implements UserStorageProviderFactory<UserFederationStorageProvider>{
    private Connection connection;
    private static final Logger logger = Logger.getLogger(UserFederationStorageProviderFactory.class);
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASS = "Loc2003@";

    @Override
    public UserFederationStorageProvider create(KeycloakSession keycloakSession, ComponentModel componentModel) {
        try {
            UserFederationStorageProvider userProvider = new UserFederationStorageProvider(keycloakSession, componentModel, connection);
            userProvider.setModel(componentModel);
            userProvider.setSession(keycloakSession);
            userProvider.setConnection(connection);

            return userProvider;
        } catch (Exception e) {
            logger.error("Error while creating provider", e);
            throw new RuntimeException("Failed to create provider", e);
        }
    }

private boolean isConnectionValid() {
    try {
        return connection != null && !connection.isClosed() && connection.isValid(2);
    } catch (SQLException e) {
        return false;
    }
}

    private synchronized Connection getConnection() throws SQLException {
        if(connection == null || !isConnectionValid()) {
            int attempts = 0;
            SQLException lasException = null;
            while(attempts <3) {
                try {
                    connection = DriverManager.getConnection(URL, USER, PASS);
                    logger.info("Connected to database at: " + connection.getMetaData().getURL());
                    return connection;
                } catch (SQLException e) {
                    attempts++;
                    lasException = e;
                    logger.error("Failed to connect to database at: " + connection.getMetaData().getURL(), e);
                }
            }
            throw new RuntimeException("Unable to connect to the database after " + attempts + "attempts at: " + connection.getMetaData().getURL());
        }
        return connection;
    }

    @Override
    public String getId() {
        return "custim-user-provider";
    }

    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("Error while closing connection", e);
        }
    }
}
