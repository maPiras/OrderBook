package Server;

public class User {
    private String username;
    private String password;

    public User(String user, String pass) {
        this.username = user;
        this.password = pass;
    }

    public void setUser(String us) {
        this.username = us;
    }

    public String getUser() {
        return this.username;
    }

    public void setPassword(String pw) {
        this.password = pw;
    }

    public String getPassword() {
        return this.password;
    }
}