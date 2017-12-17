package com.infosys.loginApp.helper;

/**
 * class to hold cassandra db info.
 */
public class DbInfo {
    String keySpace;
    String tableName;
    String userName;
    String password;
    String ip;
    String port;

    /**
     * @param keySpace
     * @param tableName
     * @param userName
     * @param password
     */
    DbInfo(String keySpace, String tableName, String userName, String password, String ip, String port) {
        this.keySpace = keySpace;
        this.tableName = tableName;
        this.userName = userName;
        this.password = password;
        this.ip = ip;
        this.port = port;
    }

    /**
     * No-arg constructor
     */
    DbInfo() {
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof DbInfo){
           DbInfo ob = (DbInfo)obj;
           if(this.ip.equals(ob.getIp()) && this.port.equals(ob.getPort()) &&
                   this.keySpace.equals(ob.getKeySpace())){
               return true;
           }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public String getKeySpace() {
        return keySpace;
    }

    public void setKeySpace(String keySpace) {
        this.keySpace = keySpace;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
