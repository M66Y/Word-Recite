package com.wordapp;

import com.wordapp.dao.UserDAO;
import com.wordapp.database.DatabaseHelper;
import com.wordapp.model.User;
import com.wordapp.UserLogin;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * 主界面
 * 包含：今日学习、背诵计划管理、词书浏览
 */
public class Main extends JFrame {

    // ==================== 组件 ====================
    private User currentUser;
    private final UserDAO userDAO = new UserDAO();

    // 主要面板
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // 侧边栏按钮
    private JButton btnTodayStudy;
    private JButton btnMyPlans;
    private JButton btnWordBook;
    private JButton btnProfile;

    // 计划管理面板组件
    private JTable planTable;
    private DefaultTableModel planTableModel;

    // 当前选中计划
    private int selectedPlanId = -1;
    private int selectedWordBookId = -1;
    private int currentDay = 1;

    // ==================== 构造方法 ====================

    public Main(User user) {
        this.currentUser = user;
        initWindow();
        initComponents();
        loadUserPlans(); // 加载用户计划
        setVisible(true);
    }

    private void initWindow() {
        setTitle("Word Recite - " + currentUser.getUsername());
        setSize(900, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 560));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DatabaseHelper.closeConnection();
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 顶部栏
        add(createTopBar(), BorderLayout.NORTH);

        // 侧边栏
        add(createSidebar(), BorderLayout.WEST);

        // 内容区（CardLayout切换页面）
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.add(createTodayStudyPanel(), "TODAY");
        contentPanel.add(createMyPlansPanel(), "PLANS");
        contentPanel.add(createWordBookPanel(), "WORDBOOK");
        contentPanel.add(createProfilePanel(), "PROFILE");
        add(contentPanel, BorderLayout.CENTER);

        // 默认显示今日学习
        showCard("TODAY");
        setButtonActive(btnTodayStudy);
    }

    // ==================== 顶部栏 ====================

    private JPanel createTopBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(44, 62, 80));
        panel.setPreferredSize(new Dimension(900, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel titleLabel = new JLabel("📚 Word Recite");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel userLabel = new JLabel("👤 " + currentUser.getUsername() + "   ");
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        userLabel.setForeground(new Color(189, 195, 199));

        JButton logoutBtn = new JButton("退出登录");
        logoutBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(231, 76, 60));
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> handleLogout());

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 10));
        rightPanel.setBackground(new Color(44, 62, 80));
        rightPanel.add(userLabel);
        rightPanel.add(logoutBtn);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    // ==================== 侧边栏 ====================

    private JPanel createSidebar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(52, 73, 94));
        panel.setPreferredSize(new Dimension(150, 620));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        btnTodayStudy = createSidebarButton("📖 今日学习", "TODAY");
        btnMyPlans    = createSidebarButton("📋 我的计划", "PLANS");
        btnWordBook   = createSidebarButton("📚 词书浏览", "WORDBOOK");
        btnProfile    = createSidebarButton("👤 个人信息", "PROFILE");

        panel.add(Box.createVerticalStrut(10));
        panel.add(btnTodayStudy);
        panel.add(Box.createVerticalStrut(5));
        panel.add(btnMyPlans);
        panel.add(Box.createVerticalStrut(5));
        panel.add(btnWordBook);
        panel.add(Box.createVerticalStrut(5));
        panel.add(btnProfile);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JButton createSidebarButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btn.setForeground(new Color(189, 195, 199));
        btn.setBackground(new Color(52, 73, 94));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(150, 45));
        btn.setPreferredSize(new Dimension(150, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(new Color(41, 128, 185))) {
                    btn.setBackground(new Color(64, 89, 110));
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(new Color(41, 128, 185))) {
                    btn.setBackground(new Color(52, 73, 94));
                }
            }
        });

        btn.addActionListener(e -> {
            showCard(cardName);
            setButtonActive(btn);
            if ("PLANS".equals(cardName)) loadUserPlans();
        });

        return btn;
    }

    private void setButtonActive(JButton activeBtn) {
        JButton[] buttons = {btnTodayStudy, btnMyPlans, btnWordBook, btnProfile};
        for (JButton btn : buttons) {
            btn.setBackground(new Color(52, 73, 94));
            btn.setForeground(new Color(189, 195, 199));
        }
        activeBtn.setBackground(new Color(41, 128, 185));
        activeBtn.setForeground(Color.WHITE);
    }

    private void showCard(String cardName) {
        cardLayout.show(contentPanel, cardName);
    }

    // ==================== 今日学习面板 ====================

    private JPanel createTodayStudyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // 标题
        JLabel title = createSectionTitle("📖 今日学习");
        panel.add(title, BorderLayout.NORTH);

        // 中心内容
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(new Color(245, 247, 250));

        // 提示信息
        JPanel infoCard = createCard();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));

        JLabel infoLabel = new JLabel("<html><b>选择学习模式</b><br><br>" +
                "请先在「我的计划」中选择一个背诵计划，然后点击开始学习。<br>" +
                "每个单词将经历：<b>选择题 → 拼写题</b> 两个阶段。<br>" +
                "两阶段全部答对后，该单词背诵完成！</html>");
        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoCard.add(infoLabel);

        // 操作按钮区
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton startLearnBtn = createActionButton("▶ 开始背诵", new Color(52, 152, 219));
        startLearnBtn.addActionListener(e -> startStudySession(false));

        JButton startReviewBtn = createActionButton("🔄 开始复习", new Color(155, 89, 182));
        startReviewBtn.addActionListener(e -> startStudySession(true));

        btnPanel.add(startLearnBtn);
        btnPanel.add(startReviewBtn);
        infoCard.add(btnPanel);

        centerPanel.add(infoCard, BorderLayout.CENTER);

        // 统计卡片
        JPanel statsPanel = createStatsPanel();
        centerPanel.add(statsPanel, BorderLayout.SOUTH);

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setBackground(new Color(245, 247, 250));

        panel.add(createStatCard("今日计划", "0 词", new Color(52, 152, 219)));
        panel.add(createStatCard("已完成", "0 词", new Color(46, 204, 113)));
        panel.add(createStatCard("正确率", "-- %", new Color(231, 76, 60)));

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(127, 140, 141));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ==================== 我的计划面板 ====================

    private JPanel createMyPlansPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // 标题 + 新建按钮
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));

        JLabel title = createSectionTitle("📋 我的背诵计划");
        headerPanel.add(title, BorderLayout.WEST);

        JButton newPlanBtn = createActionButton("＋ 新建计划", new Color(46, 204, 113));
        newPlanBtn.addActionListener(e -> showCreatePlanDialog());
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrap.setBackground(new Color(245, 247, 250));
        btnWrap.add(newPlanBtn);
        headerPanel.add(btnWrap, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // 计划列表表格
        String[] columns = {"计划ID", "计划名称", "词书", "总天数", "每日单词", "当前进度", "创建时间"};
        planTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        planTable = new JTable(planTableModel);
        planTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        planTable.setRowHeight(32);
        planTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        planTable.setSelectionBackground(new Color(174, 214, 241));
        planTable.setGridColor(new Color(220, 220, 220));

        // 隐藏计划ID列
        planTable.getColumnModel().getColumn(0).setMinWidth(0);
        planTable.getColumnModel().getColumn(0).setMaxWidth(0);
        planTable.getColumnModel().getColumn(0).setWidth(0);

        // 点击行选择计划
        planTable.getSelectionModel().addListSelectionListener(e -> {
            int row = planTable.getSelectedRow();
            if (row >= 0) {
                selectedPlanId = (int) planTableModel.getValueAt(row, 0);
            }
        });

        JScrollPane scrollPane = new JScrollPane(planTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.add(scrollPane, BorderLayout.CENTER);

        // 底部操作按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        bottomPanel.setBackground(new Color(245, 247, 250));

        JButton startBtn = createActionButton("▶ 开始学习", new Color(52, 152, 219));
        startBtn.addActionListener(e -> {
            if (selectedPlanId < 0) {
                JOptionPane.showMessageDialog(this, "请先选择一个计划", "提示", JOptionPane.WARNING_MESSAGE);
            } else {
                showCard("TODAY");
                setButtonActive(btnTodayStudy);
            }
        });

        JButton deleteBtn = createActionButton("🗑 删除计划", new Color(231, 76, 60));
        deleteBtn.addActionListener(e -> handleDeletePlan());

        bottomPanel.add(startBtn);
        bottomPanel.add(deleteBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 加载用户计划列表
     */
    private void loadUserPlans() {
        planTableModel.setRowCount(0);
        List<Object[]> plans = userDAO.getUserPlans(currentUser.getUserId());
        for (Object[] plan : plans) {
            planTableModel.addRow(new Object[]{
                    plan[0],  // plan_id（隐藏）
                    plan[1],  // plan_name
                    plan[3],  // book_name
                    plan[4] + " 天",  // total_days
                    plan[5] + " 词/天",  // words_per_day
                    "第 " + plan[6] + " 天",  // current_day
                    plan[7]   // created_at
            });
        }
    }

    /**
     * 显示新建计划对话框
     */
    private void showCreatePlanDialog() {
        JDialog dialog = new JDialog(this, "新建背诵计划", true);
        dialog.setSize(420, 360);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 5, 7, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font font = new Font("微软雅黑", Font.PLAIN, 13);

        // 计划名称
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("计划名称："), gbc);
        gbc.gridx = 1;
        JTextField planNameField = new JTextField(15);
        planNameField.setFont(font);
        formPanel.add(planNameField, gbc);

        // 选择词书
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("选择词书："), gbc);
        gbc.gridx = 1;
        List<Object[]> books = userDAO.getAllWordBooks();
        String[] bookNames = books.stream()
                .map(b -> b[0] + ": " + b[1] + " (" + b[2] + "词)")
                .toArray(String[]::new);
        JComboBox<String> bookCombo = new JComboBox<>(bookNames.length > 0 ? bookNames : new String[]{"暂无词书"});
        bookCombo.setFont(font);
        formPanel.add(bookCombo, gbc);

        // 分配方式
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("分配方式："), gbc);
        gbc.gridx = 1;
        JRadioButton byDaysBtn = new JRadioButton("按总天数");
        JRadioButton byWordsBtn = new JRadioButton("按每日单词数");
        byDaysBtn.setFont(font);
        byWordsBtn.setFont(font);
        ButtonGroup bg = new ButtonGroup();
        bg.add(byDaysBtn);
        bg.add(byWordsBtn);
        byDaysBtn.setSelected(true);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radioPanel.add(byDaysBtn);
        radioPanel.add(byWordsBtn);
        formPanel.add(radioPanel, gbc);

        // 数量输入
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel numLabel = new JLabel("总天数：");
        formPanel.add(numLabel, gbc);
        gbc.gridx = 1;
        JTextField numField = new JTextField("30", 15);
        numField.setFont(font);
        formPanel.add(numField, gbc);

        // 切换标签
        byDaysBtn.addActionListener(e -> numLabel.setText("总天数："));
        byWordsBtn.addActionListener(e -> numLabel.setText("每日单词："));

        dialog.add(formPanel, BorderLayout.CENTER);

        // 按钮
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton confirmBtn = createActionButton("确认创建", new Color(46, 204, 113));
        JButton cancelBtn = createActionButton("取  消", new Color(149, 165, 166));

        confirmBtn.addActionListener(e -> {
            String planName = planNameField.getText().trim();
            if (planName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入计划名称", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int selectedBook = bookCombo.getSelectedIndex();
            if (selectedBook < 0 || books.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请选择词书", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int bookId = (int) books.get(selectedBook)[0];
            int num;
            try {
                num = Integer.parseInt(numField.getText().trim());
                if (num <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "请输入有效的正整数", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int planId;
            if (byDaysBtn.isSelected()) {
                planId = userDAO.createPlanByDays(currentUser.getUserId(), planName, bookId, num);
            } else {
                planId = userDAO.createPlanByWordsPerDay(currentUser.getUserId(), planName, bookId, num);
            }

            if (planId > 0) {
                JOptionPane.showMessageDialog(dialog, "计划创建成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
                loadUserPlans();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "创建失败，请检查词书是否有数据", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(confirmBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * 删除选中计划
     */
    private void handleDeletePlan() {
        if (selectedPlanId < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的计划", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除该计划吗？此操作不可恢复！",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (userDAO.deletePlan(selectedPlanId, currentUser.getUserId())) {
                JOptionPane.showMessageDialog(this, "计划已删除", "提示", JOptionPane.INFORMATION_MESSAGE);
                selectedPlanId = -1;
                loadUserPlans();
            } else {
                JOptionPane.showMessageDialog(this, "删除失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==================== 词书浏览面板 ====================

    private JPanel createWordBookPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = createSectionTitle("📚 词书浏览");
        panel.add(title, BorderLayout.NORTH);

        // 词书列表
        List<Object[]> books = userDAO.getAllWordBooks();

        // 创建词书卡片
        JPanel booksPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        booksPanel.setBackground(new Color(245, 247, 250));

        if (books.isEmpty()) {
            JLabel emptyLabel = new JLabel("暂无词书数据，请先导入词书");
            emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            booksPanel.add(emptyLabel);
        } else {
            for (Object[] book : books) {
                booksPanel.add(createBookCard(book));
            }
        }

        JScrollPane scrollPane = new JScrollPane(booksPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBookCard(Object[] book) {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(5, 5));

        JLabel nameLabel = new JLabel("📖 " + book[1]);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        nameLabel.setForeground(new Color(44, 62, 80));

        JLabel wordCountLabel = new JLabel("共 " + book[2] + " 个单词");
        wordCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        wordCountLabel.setForeground(new Color(127, 140, 141));

        JLabel descLabel = new JLabel("<html>" + (book[3] != null ? book[3].toString() : "") + "</html>");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        descLabel.setForeground(new Color(100, 100, 100));

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 3));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.add(nameLabel);
        infoPanel.add(wordCountLabel);
        infoPanel.add(descLabel);

        card.add(infoPanel, BorderLayout.CENTER);
        return card;
    }

    // ==================== 个人信息面板 ====================

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = createSectionTitle("👤 个人信息");
        panel.add(title, BorderLayout.NORTH);

        JPanel card = createCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;

        Font keyFont = new Font("微软雅黑", Font.BOLD, 13);
        Font valFont = new Font("微软雅黑", Font.PLAIN, 13);

        // 头像
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("微软雅黑", Font.PLAIN, 60));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(avatarLabel, gbc);

        gbc.gridwidth = 1;

        addProfileRow(card, gbc, 1, "用户名：", currentUser.getUsername(), keyFont, valFont);
        addProfileRow(card, gbc, 2, "邮  箱：",
                currentUser.getEmail() != null ? currentUser.getEmail() : "未设置", keyFont, valFont);
        addProfileRow(card, gbc, 3, "注册时间：",
                currentUser.getCreatedAt() != null ? currentUser.getCreatedAt() : "未知", keyFont, valFont);

        // 计划数量
        int planCount = userDAO.getUserPlans(currentUser.getUserId()).size();
        addProfileRow(card, gbc, 4, "计划数量：", planCount + " 个", keyFont, valFont);

        JPanel cardWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cardWrap.setBackground(new Color(245, 247, 250));
        card.setPreferredSize(new Dimension(400, 320));
        cardWrap.add(card);
        panel.add(cardWrap, BorderLayout.CENTER);

        return panel;
    }

    private void addProfileRow(JPanel panel, GridBagConstraints gbc,
                               int row, String key, String value,
                               Font keyFont, Font valFont) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel keyLabel = new JLabel(key);
        keyLabel.setFont(keyFont);
        keyLabel.setForeground(new Color(127, 140, 141));
        panel.add(keyLabel, gbc);

        gbc.gridx = 1;
        JLabel valLabel = new JLabel(value);
        valLabel.setFont(valFont);
        valLabel.setForeground(new Color(44, 62, 80));
        panel.add(valLabel, gbc);
    }

    // ==================== 背诵学习会话 ====================

    /**
     * 启动学习会话
     *
     * @param isReview 是否为复习模式
     */
    private void startStudySession(boolean isReview) {
        if (selectedPlanId < 0) {
            // 让用户先选择计划
            JOptionPane.showMessageDialog(this,
                    "请先在「我的计划」中选择一个背诵计划！",
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE);
            showCard("PLANS");
            setButtonActive(btnMyPlans);
            return;
        }

        // 获取今日单词
        List<Object[]> todayWords = userDAO.getWordsForDay(selectedWordBookId, selectedPlanId, currentDay);
        if (todayWords.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "今日没有待学习的单词，可能已完成全部学习！",
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 打开学习窗口
        new StudyWindow(this, currentUser, userDAO, todayWords, selectedPlanId, isReview);
    }

    // ==================== 通用工具方法 ====================

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.BOLD, 18));
        label.setForeground(new Color(44, 62, 80));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return label;
    }

    private JPanel createCard() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new DropShadowBorder(),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        return panel;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(130, 36));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { button.setBackground(bgColor.darker()); }
            @Override
            public void mouseExited(MouseEvent e) { button.setBackground(bgColor); }
        });
        return button;
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要退出登录吗？",
                "退出登录",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            DatabaseHelper.closeConnection();
            SwingUtilities.invokeLater(UserLogin::new);
        }
    }

    /**
     * 简单阴影边框（模拟卡片效果）
     */
    static class DropShadowBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(200, 200, 200, 80));
            g2.fillRoundRect(x + 2, y + 2, width - 2, height - 2, 8, 8);
            g2.setColor(new Color(220, 220, 220));
            g2.drawRoundRect(x, y, width - 3, height - 3, 6, 6);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 4, 4);
        }
    }
}