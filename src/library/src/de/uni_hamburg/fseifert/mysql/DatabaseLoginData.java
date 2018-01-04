package de.uni_hamburg.fseifert.mysql;

public class DatabaseLoginData {
    /**
     * 
     * @param serverHost server host adress
     * @param serverPort server port
     * @param userName mysql user name
     * @param passWord mysql user password
     */
    public DatabaseLoginData(String serverHost, String serverPort, String userName, String passWord) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.userName = userName;
        this.passWord = passWord;
    }

    /**
     * 
     * @param serverHost server host adress
     * @param serverPort server port
     * @param databaseName mysql database name
     * @param userName mysql user name
     * @param passWord mysql user password
     */
    public DatabaseLoginData(String serverHost, String serverPort, String databaseName, String userName, String passWord) {
        this(serverHost, serverPort, userName, passWord);
        this.databaseName = databaseName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getDatabasePassword() {
        return passWord;
    }
    
    public String getDatabaseServerHost() {
        return serverHost;
    }
    
    public String getDatabaseServerPort() {
        return serverPort;
    }
    
    public String getDatabaseUser() {
        return userName;
    }
    
    private String databaseName;
    private String passWord;
    private String serverHost;
    private String serverPort;
    private String userName;
}
