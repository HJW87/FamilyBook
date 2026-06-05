package servlet;

import dao.UserDao;
import entity.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Random;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private UserDao userDao = new UserDao();

    // 生成随机昵称：用户 + 8位随机数字字母组合
    private String generateRandomUsername() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder("用户");
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // 验证手机号格式（11位数字，以1开头）
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return false;
        }
        // 检查是否全是数字
        for (char c : phone.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        // 检查第一位是不是1
        return phone.charAt(0) == '1';
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String phone = req.getParameter("phone");
        String nickname = req.getParameter("nickname");
        String password = req.getParameter("password");
        String email = req.getParameter("email");

        // 1. 验证手机号
        if (phone == null || phone.trim().isEmpty()) {
            req.setAttribute("error", "手机号不能为空！");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
            return;
        }

        if (!isValidPhone(phone)) {
            req.setAttribute("error", "手机号格式不正确！请输入11位数字手机号。");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
            return;
        }

        // 2. 验证密码
        if (password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "密码不能为空！");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
            return;
        }

        if (password.length() < 6) {
            req.setAttribute("error", "密码长度不能少于6位！");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
            return;
        }

        // 3. 检查手机号是否已被注册
        if (userDao.findByPhone(phone) != null) {
            req.setAttribute("error", "该手机号已被注册！");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
            return;
        }

        // 4. 处理昵称：如果用户没填，生成随机昵称
        String finalUsername;
        if (nickname != null && !nickname.trim().isEmpty()) {
            finalUsername = nickname.trim();
        } else {
            finalUsername = generateRandomUsername();
        }

        // 5. 创建用户对象
        User newUser = new User();
        newUser.setPhone(phone);
        newUser.setUsername(finalUsername);
        newUser.setPassword(password);
        newUser.setEmail(email);
        newUser.setRole("member");

        // 6. 注册
        boolean success = userDao.register(newUser);

        if (success) {
            // 注册成功，带提示跳转到登录页
            req.setAttribute("message", "注册成功！欢迎 " + finalUsername + "，请登录。");
            req.getRequestDispatcher("login.jsp").forward(req, resp);
        } else {
            req.setAttribute("error", "注册失败，请稍后重试！");
            req.getRequestDispatcher("register.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("register.jsp").forward(req, resp);
    }
}