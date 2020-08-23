package org.acme.getting.started;

public class DBConnectionInfo {

    private String host;
    private int port;
    private String user;
    private String password;
    private String db;

    public DBConnectionInfo(String host, int port, String user, String password, String db) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.db = db;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }
}
