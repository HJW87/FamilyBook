<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>注册 - 家庭记账本</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: 'Microsoft YaHei', Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
        }
        .container {
            background: white;
            width: 420px;
            padding: 40px;
            border-radius: 10px;
            box-shadow: 0 15px 35px rgba(0,0,0,0.2);
        }
        h2 {
            text-align: center;
            color: #333;
            margin-bottom: 10px;
        }
        .subtitle {
            text-align: center;
            color: #666;
            margin-bottom: 30px;
            font-size: 14px;
        }
        .input-group {
            margin-bottom: 20px;
        }
        .input-group input {
            width: 100%;
            padding: 12px 15px;
            border: 1px solid #ddd;
            border-radius: 5px;
            font-size: 16px;
            transition: 0.3s;
        }
        .input-group input:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 5px rgba(102,126,234,0.3);
        }
        button {
            width: 100%;
            padding: 12px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 5px;
            font-size: 16px;
            cursor: pointer;
            transition: 0.3s;
        }
        button:hover {
            opacity: 0.9;
            transform: translateY(-2px);
        }
        .error {
            background: #f8d7da;
            color: #721c24;
            padding: 10px;
            border-radius: 5px;
            margin-bottom: 20px;
            text-align: center;
            font-size: 14px;
        }
        .success {
            background: #d4edda;
            color: #155724;
            padding: 10px;
            border-radius: 5px;
            margin-bottom: 20px;
            text-align: center;
            font-size: 14px;
        }
        .login-link {
            text-align: center;
            margin-top: 20px;
            color: #666;
        }
        .login-link a {
            color: #667eea;
            text-decoration: none;
        }
        .login-link a:hover {
            text-decoration: underline;
        }
        .note {
            font-size: 12px;
            color: #999;
            text-align: center;
            margin-top: 15px;
        }
        .optional {
            color: #999;
            font-size: 12px;
            margin-left: 5px;
        }
    </style>
</head>
<body>
<div class="container">
    <h2>🏠 家庭记账本</h2>
    <div class="subtitle">用户注册</div>

    <% if (request.getAttribute("error") != null) { %>
    <div class="error">❌ <%= request.getAttribute("error") %></div>
    <% } %>

    <% if (request.getAttribute("message") != null) { %>
    <div class="success">✅ <%= request.getAttribute("message") %></div>
    <% } %>

    <form action="register" method="post">
        <div class="input-group">
            <input type="tel" name="phone" placeholder="手机号（11位，必填）" maxlength="11" required>
        </div>
        <div class="input-group">
            <input type="text" name="nickname" placeholder="昵称（选填，不填自动生成）">
        </div>
        <div class="input-group">
            <input type="password" name="password" placeholder="密码（至少6位，必填）" minlength="6" required>
        </div>
        <div class="input-group">
            <input type="email" name="email" placeholder="邮箱（选填）">
        </div>
        <button type="submit">注 册</button>
    </form>
    <div class="login-link">
        已有账号？ <a href="login">去登录</a>
    </div>
    <div class="note">
        🔒 手机号将作为登录账号，请确保填写正确
    </div>
</div>
</body>
</html>