package com.assignment1.keycloak.provider;

import org.jboss.logging.Logger;
import com.assignment1.keycloak.adapter.UserAdapter;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.UserCredentialStore;

import com.assignment1.keycloak.entity.UserEntity;
import org.keycloak.models.*;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;

import org.keycloak.models.credential.PasswordCredentialModel;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.keycloak.common.util.SecretGenerator;
import org.keycloak.models.utils.HmacOTP;
import org.keycloak.models.utils.KeycloakModelUtils;

public class UserFederationStorageProvider implements UserStorageProvider, UserLookupProvider, UserRegistrationProvider, UserQueryProvider, CredentialInputValidator, CredentialInputUpdater {
    private static final Logger logger = Logger.getLogger(UserFederationStorageProvider.class);
    public static final String PASSWORD = "password";
    private KeycloakSession session;
    private ComponentModel model;
    private Connection connection;

    public UserFederationStorageProvider(KeycloakSession session, ComponentModel model, Connection connection) {
        
        this.session = session;
        this.model = model;
        this.connection = connection;
    }

    public String hashedPassword(String rawPassword) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(salt);
            byte[] hashedPassword = messageDigest.digest(rawPassword.getBytes("UTF-8"));

            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(combined, 0, messageDigest, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            logger.error("Error hashing password");
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    public boolean checkPassword(String rawPassword, String storedHash) {
        if(storedHash == null || rawPassword == null) {
            return false;
        }

        try {
            byte[] combined = Base64.getDecoder().decode(storedHash);

            byte[] salt = new byte[16];
            byte[] storedPasswordHash = new byte[combined.length - 16];
            System.arraycopy(combined, 0, salt, 0, 16);
            System.arraycopy(combined, 16, storedPasswordHash, 0, storedPasswordHash.length);

            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(salt);
            byte[] inputPasswordHash = messageDigest.digest(rawPassword.getBytes("UTF-8"));

            return MessageDigest.isEqual(storedPasswordHash, inputPasswordHash);
        } catch (Exception e) {
            logger.error("Error checking password", e);
            return false;
        }
    }

    @Override
    public void close() {
        logger.info("Closing connection");
        try {
            if(connection!=null && !connection.isClosed()) {
                connection.close();
                logger.info("Data connection closed successfully!!");
            }
        } catch(SQLException e) {
            logger.error(e);
        }
    }

    private UserEntity mapRowToUser(ResultSet rs) throws SQLException {
        logger.debug("Mapping ResultSet to UserEntity");
        UserEntity user = new UserEntity();
        user.setId(UUID.fromString(rs.getString("id")));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("firstName"));
        user.setLastName(rs.getString("lastName"));
        user.setPassword(rs.getString("password"));
        logger.debug("Map User: " + user.getUsername());

        return user;
    }
    
    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        logger.info("Fetching user with id: " + id);
        String externalId = StorageId.externalId(id);

        try {
            UUID userId = UUID.fromString(externalId);

            String query = "SELECT * FROM users WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)){
                stmt.setObject(1, userId);
                ResultSet rs = stmt.executeQuery();
                if(rs.next()) {
                    logger.info("user with id: " + id + " found");
                    UserEntity entity = mapRowToUser(rs);

                    return new UserAdapter(session, realm, model, entity, connection);
                } else {
                    logger.info("user with id: " + id + " not found");
                }
            } catch(SQLException e) {
                logger.error("Error in finding user with id: " + id, e);
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format for id: " + id, e);
        }
        return null;
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        logger.debug("Attempting to fetch user by username: " + username);
        String query = "SELECT * FROM users WHERE username = ?";
        try(PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                logger.info("User with username: " + username + " found");
                UserEntity entity = mapRowToUser(rs);
                return new UserAdapter(session, realm, model, entity, connection);
            }
        } catch (SQLException e) {
                logger.error("Error while fetching user by username: " + username, e);
        }
        return null;
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        logger.debug("Searching user with email: " + email);
        String query = "SELECT * FROM users WHERE email = ?";
        try(PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if(rs.next()) {
                logger.info("User with email: " + email + " found");
                UserEntity entity = mapRowToUser(rs);
                return new UserAdapter(session, realm, model, entity, connection);
            }
        } catch (SQLException e) {
            logger.error("No user with email: " + email + " found");
        }
        return null;
    }

    @Override
    public UserModel addUser(RealmModel realm, String username) {
        logger.info("Adding user with username: " + username);

        String email = username + "@example.com";
        String firstName = "FirstName";
        String lastName = "LastName";
        UUID newId = UUID.randomUUID();

        String query = "INSERT INTO users (id, username, email, password, firstName, lastName)" + "VALUES (?, ?, ?, ?, ?, ?)";

        try(PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, newId, java.sql.Types.OTHER);
            stmt.setString(2, username);
            stmt.setString(3, email);
            stmt.setNull(4, Types.VARCHAR);
            stmt.setString(5, firstName);
            stmt.setString(6, lastName);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("User with id " + newId + " added successfully");

                UserEntity entity = new UserEntity();
                entity.setId(newId);
                entity.setUsername(username);
                entity.setEmail(email);
                entity.setPassword(null);
                entity.setFirstName(firstName);;
                entity.setLastName(lastName);

                return new UserAdapter(session, realm, model, entity, connection);
            } else {
                logger.error("Failed to add user: " + username);
            }
        } catch (SQLException e) {
            logger.error("Error while adding user with username: " + username, e);
        }
        return null;
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        logger.info("Removing user with id: " + user.getId());
        String externalId = StorageId.externalId(user.getId());

        try {
            UUID userId = UUID.fromString(externalId);

            String query = "DELETE FROM users WHERE id = ?";
            try(PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setObject(1, userId);
                int rowsAffected = stmt.executeUpdate();
                if(rowsAffected > 0) {
                    logger.info("Delete user with id: " + user.getId() + " successfully");
                    return true;
                } else {
                    logger.info("No user with id: " + user.getId() + " found");
                }
            } catch (SQLException e) {
                logger.error("Error deleting user", e);
            }
        } catch(IllegalArgumentException e) {
            logger.error("Invalid UUID format: " + user.getId(), e);
        }
        return false;
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> map, Integer firstResult,
            Integer maxResult) {
        String searchParam = map.getOrDefault("email", map.getOrDefault("username", ""));
        logger.info("Searching for user with parameter: " + searchParam);

        List<UserEntity> users = new ArrayList<>();
        String query = "SELECT * FROM users WHERE email LIKE ? OR username LIKE ?";
        try(PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + searchParam + "%");
            stmt.setString(2, "%" + searchParam + "%");
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                users.add(mapRowToUser(rs));
            }

            logger.info("Found " + users.size() + " users");
        } catch (SQLException e) {
            logger.error("Error while seaching for user with parameter: " + searchParam, e);
        }
        return users.stream().map(user -> new UserAdapter(session, realm, model, user, connection));
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel model, Integer int1, Integer int2) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String s1, String s2) {
        return Stream.empty();
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        if(realm == null || model == null || credentialInput == null) {
            logger.warn("Null parameter(s) provided to updateCredential");
            return false;
        }

        if(!supportsCredentialType(credentialInput.getType())) {
            logger.warn("Unsupported credential type: " + credentialInput.getType());
            return false;
        }

        if(credentialInput.getType().equals(PasswordCredentialModel.TYPE)) {
            String newPassword = credentialInput.getChallengeResponse();
            if(newPassword == null || newPassword.trim().isEmpty()) {
                logger.warn("Empty password provided for user: " + user.getUsername());
                return false;
            }

            String hashedPassword = hashedPassword(newPassword);

            String query = "UPDATE users SET password = ?  WHERE username = ?";
            try(PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, hashedPassword);
                stmt.setString(2, user.getUsername());
                int rowsAffected = stmt.executeUpdate();
                if(rowsAffected > 0) {
                    logger.info("Password updated successfully for user: " + user.getUsername());
                    return true;
                } else {
                    logger.warn("No username: " + user.getUsername() + " found");
                    return false;
                }
            } catch (SQLException e) {
                logger.error("Error while updating password for user: " + user.getUsername(), e);
                return false;
            }
        }

        logger.warn("Unsupported credential input type for user: " + user.getUsername());
        return false;
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
        if(PasswordCredentialModel.TYPE.equals(credentialType)) {
            logger.info("Disabling credential type for user: " + user.getUsername());
            throw new IllegalArgumentException("Disabling password credentials is not suppurted ");
        }
    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
        return Stream.empty();
    }

    

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        logger.info("Validating user: " + user.getUsername());
        if(!supportsCredentialType(credentialInput.getType())) {
            logger.warn("Invalid credentials for user: " + user.getUsername());
            return false;
        }

        String query = "SELECT password FROM users WHERE username = ?";
        try(PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                String storedPassword = rs.getString("password");
                boolean isValid = checkPassword(credentialInput.getChallengeResponse(), storedPassword);
                logger.info("User: " + user.getUsername() + " password: " + isValid);
                return isValid;
            } else {
                logger.warn("User: " + user.getUsername() + " password does not match");
            }
        } catch (SQLException e) {
            logger.error("Error validating credentials for user", e);
        }
        return false;
    }

    //@Override
    public int getUserCount(RealmModel realm) {
        logger.info("Counting users for realm: " + realm.getName());

        String query = "SELECT COUNT(*) FROM users";

        try(PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if(rs.next()) {
                int count = rs.getInt(1);
                logger.info("Found " + count + " user(s) in users table");
                return count;
            }
        } catch (SQLException e) {
            logger.error("Error counting users", e);
        }
        return 0;
    }

    public void setModel(ComponentModel componentModel) {
        this.model = componentModel;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
    
    public void setSession(KeycloakSession keycloakSession) {
        this.session = keycloakSession;
    }
}
