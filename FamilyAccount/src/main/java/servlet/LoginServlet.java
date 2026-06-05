package servlet;

import dao.UserDao;
import entity.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String phone = req.getParameter("phone");
        String password = req.getParameter("password");

        if (phone == null || password == null || phone.trim().isEmpty() || password.trim().isEmpty()) {
            req.setAttribute("error", "手机号和密码不能为空！");
            req.getRequestDispatcher("login.jsp").forward(req, resp);
            return;
        }

        User user = userDao.login(phone, password);

        if (user != null) {
            HttpSession session = req.getSession();
            session.setAttribute("loginUser", user);
            resp.sendRedirect("stats.jsp");
        } else {
            req.setAttribute("error", "手机号或密码错误！");
            req.getRequestDispatcher("login.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("login.jsp").forward(req, resp);
    }
}