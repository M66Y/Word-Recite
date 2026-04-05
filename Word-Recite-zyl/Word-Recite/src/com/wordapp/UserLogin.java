package com.wordapp;

import com.wordapp.dao.UserDAO;
import com.wordapp.database.DatabaseHelper;
import com.wordapp.model.User;
import com.wordapp.Main;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * 用户登录/注册窗口
 * 程序启动入口界面
 */
public class UserLogin extends JFrame {

    // ==================== UI组件 ====================
    private JTabbedPane tabbedPane;

    // 登录面板组件
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;
    private JButton loginButton;
    private JLabel loginStatusLabel;

    // 注册面板组件
    private JTextField registerUsernameField;
    private JPasswordField registerPasswordField;
    private JPasswordField registerConfirmPasswordField;
    private JTextField registerEmailField;
    private JButton registerButton;
    private JLabel registerStatusLabel;

    // 数据访问
    private final UserDAO userDAO = new UserDAO();

    // ==================== 构造方法 ====================

    public UserLogin() {
        initWindow();
        initComponents();
        setVisible(true);
    }

    private void initWindow() {
        setTitle("Word Recite - 背单词系统");
        setSize(480, 420);
        setLocationRelativeTo(null); // 居中显示
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // 设置图标（如有）
        // setIconImage(...);

        // 窗口关闭时断开数据库连接
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseHelper.closeConnection();
            }
        });
    }

    private void initComponents() {
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));

        // 顶部标题
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 选项卡（登录/注册）
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        tabbedPane.addTab("  登  录  ", createLoginPanel());
        tabbedPane.addTab("  注  册  ", createRegisterPanel());
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    /**
     * 顶部标题面板
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(52, 152, 219));
        panel.setPreferredSize(new Dimension(480, 80));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        JLabel titleLabel = new JLabel("📚 Word Recite");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("智能背单词系统");
        subLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        subLabel.setForeground(new Color(200, 230, 255));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subLabel);

        return panel;
    }

    /**
     * 登录面板
     */
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("微软雅黑", Font.PLAIN, 13);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 13);

        // 用户名
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel userLabel = new JLabel("用户名：");
        userLabel.setFont(labelFont);
        panel.add(userLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        loginUsernameField = new JTextField(20);
        loginUsernameField.setFont(fieldFont);
        styleTextField(loginUsernameField);
        panel.add(loginUsernameField, gbc);

        // 密码
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel pwdLabel = new JLabel("密  码：");
        pwdLabel.setFont(labelFont);
        panel.add(pwdLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        loginPasswordField = new JPasswordField(20);
        loginPasswordField.setFont(fieldFont);
        styleTextField(loginPasswordField);
        panel.add(loginPasswordField, gbc);

        // 登录按钮
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        loginButton = createStyledButton("登  录", new Color(52, 152, 219));
        loginButton.addActionListener(e -> handleLogin());
        panel.add(loginButton, gbc);

        // 回车触发登录
        loginPasswordField.addActionListener(e -> handleLogin());
        loginUsernameField.addActionListener(e -> loginPasswordField.requestFocus());

        // 状态标签
        gbc.gridy = 3; gbc.insets = new Insets(5, 5, 5, 5);
        loginStatusLabel = new JLabel(" ");
        loginStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        loginStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(loginStatusLabel, gbc);

        return panel;
    }

    /**
     * 注册面板
     */
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 30, 10, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("微软雅黑", Font.PLAIN, 13);

        // 用户名
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        JLabel userLabel = new JLabel("用户名：");
        userLabel.setFont(labelFont);
        panel.add(userLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        registerUsernameField = new JTextField(20);
        styleTextField(registerUsernameField);
        panel.add(registerUsernameField, gbc);

        // 密码
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel pwdLabel = new JLabel("密  码：");
        pwdLabel.setFont(labelFont);
        panel.add(pwdLabel, gbc);

        gbc.gridx = 1;
        registerPasswordField = new JPasswordField(20);
        styleTextField(registerPasswordField);
        panel.add(registerPasswordField, gbc);

        // 确认密码
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel confirmLabel = new JLabel("确认密码：");
        confirmLabel.setFont(labelFont);
        panel.add(confirmLabel, gbc);

        gbc.gridx = 1;
        registerConfirmPasswordField = new JPasswordField(20);
        styleTextField(registerConfirmPasswordField);
        panel.add(registerConfirmPasswordField, gbc);

        // 邮箱
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel emailLabel = new JLabel("邮  箱：");
        emailLabel.setFont(labelFont);
        panel.add(emailLabel, gbc);

        gbc.gridx = 1;
        registerEmailField = new JTextField(20);
        styleTextField(registerEmailField);
        panel.add(registerEmailField, gbc);

        // 注册按钮
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 5, 5, 5);
        registerButton = createStyledButton("注  册", new Color(46, 204, 113));
        registerButton.addActionListener(e -> handleRegister());
        panel.add(registerButton, gbc);

        // 状态标签
        gbc.gridy = 5; gbc.insets = new Insets(5, 5, 5, 5);
        registerStatusLabel = new JLabel(" ");
        registerStatusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        registerStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(registerStatusLabel, gbc);

        return panel;
    }

    // ==================== 事件处理 ====================

    /**
     * 处理登录逻辑
     */
    private void handleLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        // 输入验证
        if (username.isEmpty() || password.isEmpty()) {
            showStatus(loginStatusLabel, "请输入用户名和密码", Color.RED);
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("登录中...");

        // 异步执行登录（防止UI卡顿）
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return userDAO.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        showStatus(loginStatusLabel, "登录成功！正在进入...", new Color(46, 204, 113));
                        // 延迟跳转主界面
                        Timer timer = new Timer(800, e -> {
                            dispose();
                            new Main(user);
                        });
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        showStatus(loginStatusLabel, "用户名或密码错误", Color.RED);
                        loginButton.setEnabled(true);
                        loginButton.setText("登  录");
                    }
                } catch (Exception e) {
                    showStatus(loginStatusLabel, "登录失败，请检查网络连接", Color.RED);
                    loginButton.setEnabled(true);
                    loginButton.setText("登  录");
                }
            }
        };
        worker.execute();
    }

    /**
     * 处理注册逻辑
     */
    private void handleRegister() {
        String username = registerUsernameField.getText().trim();
        String password = new String(registerPasswordField.getPassword());
        String confirmPassword = new String(registerConfirmPasswordField.getPassword());
        String email = registerEmailField.getText().trim();

        // 输入验证
        if (username.isEmpty() || password.isEmpty()) {
            showStatus(registerStatusLabel, "用户名和密码不能为空", Color.RED);
            return;
        }
        if (username.length() < 3 || username.length() > 20) {
            showStatus(registerStatusLabel, "用户名长度应为3-20个字符", Color.RED);
            return;
        }
        if (password.length() < 6) {
            showStatus(registerStatusLabel, "密码长度不能少于6位", Color.RED);
            return;
        }
        if (!password.equals(confirmPassword)) {
            showStatus(registerStatusLabel, "两次密码输入不一致", Color.RED);
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("注册中...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                // 先检查用户名
                if (userDAO.isUsernameExists(username)) {
                    return null; // 特殊标记：用户名已存在
                }
                return userDAO.registerUser(username, password, email);
            }

            @Override
            protected void done() {
                try {
                    Boolean result = get();
                    if (result == null) {
                        showStatus(registerStatusLabel, "用户名已被占用，请换一个", Color.RED);
                    } else if (result) {
                        showStatus(registerStatusLabel, "注册成功！请前往登录", new Color(46, 204, 113));
                        clearRegisterFields();
                        // 切换到登录标签
                        Timer timer = new Timer(1000, e -> tabbedPane.setSelectedIndex(0));
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        showStatus(registerStatusLabel, "注册失败，请稍后重试", Color.RED);
                    }
                } catch (Exception e) {
                    showStatus(registerStatusLabel, "注册失败，请检查网络连接", Color.RED);
                } finally {
                    registerButton.setEnabled(true);
                    registerButton.setText("注  册");
                }
            }
        };
        worker.execute();
    }

    // ==================== 工具方法 ====================

    private void showStatus(JLabel label, String message, Color color) {
        label.setText(message);
        label.setForeground(color);
    }

    private void clearRegisterFields() {
        registerUsernameField.setText("");
        registerPasswordField.setText("");
        registerConfirmPasswordField.setText("");
        registerEmailField.setText("");
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        // 悬停效果
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    // ==================== 程序入口 ====================

    public static void main(String[] args) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // 使用默认外观
        }

        // 测试数据库连接
        if (!DatabaseHelper.testConnection()) {
            JOptionPane.showMessageDialog(null,
                    "无法连接到数据库，请检查数据库配置！\n" +
                            "数据库地址: localhost:3306/word_recite_db",
                    "数据库连接失败",
                    JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(UserLogin::new);
    }
}