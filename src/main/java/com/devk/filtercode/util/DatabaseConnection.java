package com.devk.filtercode.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;
    private static final String DRIVER;

    static{
        try(InputStream inputStream = DatabaseConnection.class.getClassLoader().getResourceAsStream("config.properties")){
            if (inputStream == null){
                throw new RuntimeException("Khong tim thay file config.properties trong resources!");
            }

            Properties properties = new Properties();
            properties.load(inputStream);

            URL = properties.getProperty("db.url");
            USER = properties.getProperty("db.user");
            PASSWORD = properties.getProperty("db.password");
            DRIVER = properties.getProperty("db.driver");

        }catch (Exception e){
            throw new RuntimeException("ERROR while load config.properties!");
        }
    }

    public static Connection getConnection() throws SQLException {
        try{
            Class.forName(DRIVER);
        }catch (ClassNotFoundException e){
            throw new RuntimeException("Khong tim thay diver JDBC: " + DRIVER, e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
