package entity;

import java.sql.Timestamp;

public class User {
    private int id;
    private String phone;      // 手机号（唯一标识）
    private String username;   // 昵称（可重复）
    private String password;
    private String email;
    private String role;
    private Timestamp createTime;

    public User() {
    }

    public User(String phone, String username, String password, String email, String role) {
        this.phone = phone;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    // Getter 和 Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", phone='" + phone + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}