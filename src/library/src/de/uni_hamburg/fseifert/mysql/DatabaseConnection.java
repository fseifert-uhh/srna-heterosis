package de.uni_hamburg.fseifert.mysql;

import java.sql.*;

public class DatabaseConnection {
    /**
     * 
     * @param databaseLoginData object providing mysql database login data
     * @see DatabaseLoginData
     * @throws SQLException 
     */
    public DatabaseConnection(DatabaseLoginData databaseLoginData) throws SQLException {
        this(databaseLoginData.getDatabaseServerHost(), databaseLoginData.getDatabaseServerPort(), databaseLoginData.getDatabaseName(), databaseLoginData.getDatabaseUser(), databaseLoginData.getDatabasePassword());
    }

    /**
     * 
     * @param serverHost server host adress
     * @param serverPort server port
     * @param userName mysql user name
     * @param passWord mysql user password
     * @throws SQLException 
     */
    public DatabaseConnection(String serverHost, String serverPort, String userName, String passWord) throws SQLException {
        this(serverHost, serverPort, "", userName, passWord);
    }
    
    /**
     * 
     * @param serverHost server host adress
     * @param serverPort server port
     * @param databaseName mysql database name
     * @param userName mysql user name
     * @param passWord mysql user password
     * @throws SQLException 
     */
    public DatabaseConnection(String serverHost, String serverPort, String databaseName, String userName, String passWord) throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch(ClassNotFoundException e) {
            System.out.println(e);
        }

        try {
            if(databaseName.isEmpty()) {
                connection = DriverManager.getConnection("jdbc:mysql://" + serverHost + ":" + serverPort, userName, passWord);
            }
            else {
                connection = DriverManager.getConnection("jdbc:mysql://" + serverHost + ":" + serverPort + "/" + databaseName, userName, passWord);
        
                this.databaseName = databaseName;
            }
            
            connectionActive = true;
        }
        catch(SQLException e) {
            connectionActive = false;
            throw new SQLException(e);
        }
    }

    public void close() throws SQLException {
        if(isActive()) {
            connectionActive = false;
            connection.close();
        }
    }

    /**
     * tests for presence of a specified database for the current database connection
     * 
     * @param databaseName 
     * @return boolean value
     * @throws SQLException 
     */
    public boolean databaseExists(String databaseName) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = \"" + databaseName + "\"");
        resultSet.last();
        int resultCount = resultSet.getRow();
        statement.close();

        if(resultCount > 0) {
            return true;
        }

        return false;
    }
    
    /**
     * tests for presence of a specified database table for the current database connection
     * 
     * @param databaseTableName
     * @return boolean value
     * @throws SQLException 
     */
    public boolean databaseTableExists(String databaseTableName) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT " + databaseTableName + " FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = \"" + databaseName + "\"");
        resultSet.last();
        int resultCount = resultSet.getRow();
        statement.close();

        if(resultCount > 0) {
            return true;
        }

        return false;
    }

    /**
     * executes a SQL query to the current database connection
     * 
     * @param databaseQuery SQL query string
     * @param statement statement fetching query results
     * @return boolean value stating the execution status
     * @see Statement
     * @throws SQLException 
     */
    public boolean executeQuery(String databaseQuery, Statement statement) throws SQLException {
        boolean executionStatus = false;

        executionStatus = statement.execute(databaseQuery);

        return executionStatus;
    }

    /**
     * executes a SQL update query to the current database connection
     * @param databaseQuery SQL update query string
     * @return boolean value stating the execution status
     * @throws SQLException 
     */
    public boolean executeUpdate(String databaseQuery) throws SQLException {
        int executionStatus = 0;

        Statement statement = connection.createStatement();
        executionStatus = statement.executeUpdate(databaseQuery);
        statement.close();

        return (executionStatus != Statement.EXECUTE_FAILED) ? true : false;
    }

    /**
     * 
     * @return Connection object
     * @see Connection
     */
    
    public Connection getConnection() {
        return connection;
    }

    /**
     * 
     * @return boolean value stating the activity of database connection
     */
    public boolean isActive() {
        return connectionActive;
    }
    
    private boolean connectionActive = false;

    private Connection connection;

    private DatabaseLoginData databaseLoginData;
    
    private String databaseName;
}
