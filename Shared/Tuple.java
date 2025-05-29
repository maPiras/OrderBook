package Shared;

import java.net.InetAddress;

public class Tuple {
    private String password;
    private boolean status;
    private InetAddress address;
    private int port;

    public Tuple(String password, boolean status, InetAddress address, int port) {
        this.password = password;
        this.status = status;
        this.address = address;
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}