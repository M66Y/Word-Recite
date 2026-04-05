package com.wordapp;

import com.wordapp.dao.UserDAO;
import com.wordapp.model.User;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * 背诵学习窗口
 * 流程：选择题（根据释义选单词） → 拼写题（根据释义拼写单词）
 * 两阶段全部答对，该单词背诵完成
 */
public class StudyWindow extends JDialog {

    // ==================== 学习状态 ====================
    private final User currentUser;
    private final UserDAO userDAO;
    private final int planId;
    private final boolean isReview;

    /** 今日单词列表 [wordId, word, definition, phonetic] */
    private final List<Object[]> wordList;

    /** 当前单词索引 */
    private int currentIndex = 0;

    /** 当前单词学习阶段：0=选择题，1=拼写题 */
    private int currentPhase = 0;

    /** 错误次数统计 */
    private int errorCount = 0;
    private int totalCorrect = 0;

    // ==================== UI组件 ====================
    private JLabel progressLabel;
    private JLabel phaseLabel;
    private JLabel wordLabel;         // 显示释义（让用户选/拼写单词）
    private JLabel phoneticLabel;     // 音标
    private JPanel questionPanel;     // 动态问题区域
    private JLabel feedbackLabel;     // 答对/错反馈
    private JButton nextButton;
    private JProgressBar progressBar;

    // 选择题专用
    private ButtonGroup choiceGroup;
    private JPanel choicesPanel;

    // 拼写题专用
    private JTextField spellField;
    private JButton submitSpellBtn;

    // ==================== 构造方法 ====================

    public StudyWindow(JFrame parent, User user, UserDAO dao,
                       List<Object[]> words, int planId, boolean isReview) {
        super(parent, isReview ? "复习模式" : "背诵模式", true);
        this.currentUser = user;
        this.userDAO = dao;
        this.wordList = new ArrayList<>(words);
        this.planId = planId;
        this.isReview = isReview;

        Collections.shuffle(wordList); // 随机顺序

        setSize(620, 520);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);

        initComponents();
        loadCurrentWord();
        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // 顶部进度区
        mainPanel.add(createTopPanel(), BorderLayout.NORTH);

        // 中央学习区
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(new Color(245, 247, 250));

        // 单词信息卡
        JPanel wordCard = createWordCard();
        centerPanel.add(wordCard, BorderLayout.NORTH);

        // 题目区（动态切换）
        questionPanel = new JPanel(new CardLayout());
        questionPanel.setBackground(new Color(245, 247, 250));
        questionPanel.add(createChoicePanel(), "CHOICE");
        questionPanel.add(createSpellPanel(), "SPELL");
        centerPanel.add(questionPanel, BorderLayout.CENTER);

        // 反馈区
        feedbackLabel = new JLabel(" ");
        feedbackLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        feedbackLabel.setHorizontalAlignment(SwingConstants.CENTER);
        centerPanel.add(feedbackLabel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 底部按钮
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(new Color(245, 247, 250));

        progressLabel = new JLabel("进度：1 / " + wordList.size());
        progressLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        progressLabel.setForeground(new Color(44, 62, 80));

        phaseLabel = new JLabel("阶段：选择题");
        phaseLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        phaseLabel.setForeground(new Color(52, 152, 219));
        phaseLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        progressBar = new JProgressBar(0, wordList.size());
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("0%");
        progressBar.setBackground(new Color(220, 220, 220));
        progressBar.setForeground(new Color(52, 152, 219));
        progressBar.setPreferredSize(new Dimension(0, 14));

        panel.add(progressLabel, BorderLayout.WEST);
        panel.add(phaseLabel, BorderLayout.EAST);
        panel.add(progressBar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createWordCard() {
        JPanel card = new JPanel(new GridLayout(2, 1, 0, 5));
        card.setBackground(new Color(52, 73, 94));
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        card.setPreferredSize(new Dimension(0, 100));

        wordLabel = new JLabel("释义加载中...");
        wordLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        wordLabel.setForeground(Color.WHITE);
        wordLabel.setHorizontalAlignment(SwingConstants.CENTER);

        phoneticLabel = new JLabel(" ");
        phoneticLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        phoneticLabel.setForeground(new Color(174, 214, 241));
        phoneticLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(wordLabel);
        card.add(phoneticLabel);
        return card;
    }

    private JPanel createChoicePanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 8));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        choicesPanel = panel;
        return panel;
    }

    private JPanel createSpellPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel hint = new JLabel("请根据上方释义拼写单词：");
        hint.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        hint.setForeground(new Color(100, 100, 100));

        spellField = new JTextField();
        spellField.setFont(new Font("微软雅黑", Font.BOLD, 20));
        spellField.setHorizontalAlignment(JTextField.CENTER);
        spellField.setPreferredSize(new Dimension(0, 50));
        spellField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        spellField.addActionListener(e -> handleSpellSubmit());

        submitSpellBtn = new JButton("提交答案");
        submitSpellBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        submitSpellBtn.setBackground(new Color(52, 152, 219));
        submitSpellBtn.setForeground(Color.WHITE);
        submitSpellBtn.setFocusPainted(false);
        submitSpellBtn.setBorderPainted(false);
        submitSpellBtn.setPreferredSize(new Dimension(0, 40));
        submitSpellBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitSpellBtn.addActionListener(e -> handleSpellSubmit());

        panel.add(hint, BorderLayout.NORTH);
        panel.add(spellField, BorderLayout.CENTER);
        panel.add(submitSpellBtn, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));

        JLabel statsLabel = new JLabel("正确: 0  错误: 0");
        statsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statsLabel.setForeground(new Color(127, 140, 141));

        nextButton = new JButton("下一题 ▶");
        nextButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        nextButton.setBackground(new Color(46, 204, 113));
        nextButton.setForeground(Color.WHITE);
        nextButton.setFocusPainted(false);
        nextButton.setBorderPainted(false);
        nextButton.setPreferredSize(new Dimension(130, 40));
        nextButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nextButton.setVisible(false);
        nextButton.addActionListener(e -> moveToNext());

        panel.add(statsLabel, BorderLayout.WEST);
        panel.add(nextButton, BorderLayout.EAST);
        return panel;
    }

    // ==================== 学习逻辑 ====================

    /**
     * 加载当前单词的题目
     */
    private void loadCurrentWord() {
        if (currentIndex >= wordList.size()) {
            showCompletionDialog();
            return;
        }

        Object[] currentWord = wordList.get(currentIndex);
        String word = (String) currentWord[1];
        String definition = (String) currentWord[2];
        String phonetic = currentWord[3] != null ? (String) currentWord[3] : "";

        // 更新进度
        progressLabel.setText("进度：" + (currentIndex + 1) + " / " + wordList.size());
        int progress = (int) (((double) currentIndex / wordList.size()) * 100);
        progressBar.setValue(currentIndex);
        progressBar.setString(progress + "%");

        // 显示释义（题目）
        wordLabel.setText("<html><center>" + definition + "</center></html>");
        phoneticLabel.setText(phonetic.isEmpty() ? " " : "[ " + phonetic + " ]");

        feedbackLabel.setText(" ");
        nextButton.setVisible(false);

        if (currentPhase == 0) {
            // 选择题：显示4个选项
            phaseLabel.setText("阶段：选择题（根据释义选单词）");
            phaseLabel.setForeground(new Color(52, 152, 219));
            loadChoiceQuestion(word);
            showPhase("CHOICE");
        } else {
            // 拼写题
            phaseLabel.setText("阶段：拼写题（根据释义拼写单词）");
            phaseLabel.setForeground(new Color(155, 89, 182));
            spellField.setText("");
            spellField.setEnabled(true);
            submitSpellBtn.setEnabled(true);
            showPhase("SPELL");
            spellField.requestFocus();
        }
    }

    /**
     * 加载选择题选项
     */
    private void loadChoiceQuestion(String correctWord) {
        choicesPanel.removeAll();
        choiceGroup = new ButtonGroup();

        // 收集干扰项
        List<String> options = new ArrayList<>();
        options.add(correctWord);

        Random random = new Random();
        List<Object[]> others = new ArrayList<>(wordList);
        others.remove(wordList.get(currentIndex));
        Collections.shuffle(others);

        for (int i = 0; i < Math.min(3, others.size()); i++) {
            options.add((String) others.get(i)[1]);
        }

        // 不足4个时补充假选项
        while (options.size() < 4) {
            options.add("option" + options.size());
        }

        Collections.shuffle(options);

        for (String option : options) {
            JRadioButton rb = new JRadioButton(option);
            rb.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            rb.setBackground(Color.WHITE);
            rb.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
            ));
            rb.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // 选择后立即检查
            rb.addActionListener(e -> handleChoiceSelected(option, correctWord));
            choiceGroup.add(rb);
            choicesPanel.add(rb);
        }

        choicesPanel.revalidate();
        choicesPanel.repaint();
    }

    /**
     * 处理选择题选择
     */
    private void handleChoiceSelected(String selected, String correct) {
        // 禁用所有选项
        for (Component c : choicesPanel.getComponents()) {
            c.setEnabled(false);
        }

        if (selected.equals(correct)) {
            feedbackLabel.setText("✅ 正确！");
            feedbackLabel.setForeground(new Color(39, 174, 96));
            nextButton.setVisible(true);
        } else {
            errorCount++;
            feedbackLabel.setText("❌ 错误！正确答案是：" + correct);
            feedbackLabel.setForeground(new Color(192, 57, 43));

            // 修复：使用完整路径 javax.swing.Timer 解决冲突
            javax.swing.Timer timer = new javax.swing.Timer(1500, e -> {
                feedbackLabel.setText("重新选择...");
                feedbackLabel.setForeground(new Color(230, 126, 34));
                loadChoiceQuestion(correct);
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    /**
     * 处理拼写题提交
     */
    private void handleSpellSubmit() {
        String userInput = spellField.getText().trim().toLowerCase();
        Object[] currentWord = wordList.get(currentIndex);
        String correct = ((String) currentWord[1]).toLowerCase();

        if (userInput.isEmpty()) return;

        spellField.setEnabled(false);
        submitSpellBtn.setEnabled(false);

        if (userInput.equals(correct)) {
            feedbackLabel.setText("✅ 拼写正确！");
            feedbackLabel.setForeground(new Color(39, 174, 96));
            spellField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(39, 174, 96), 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            // 记录结果
            totalCorrect++;
            userDAO.recordWordResult(currentUser.getUserId(), planId, (int) currentWord[0], true);
            nextButton.setVisible(true);
        } else {
            errorCount++;
            feedbackLabel.setText("❌ 拼写错误！正确答案：" + currentWord[1]);
            feedbackLabel.setForeground(new Color(192, 57, 43));
            spellField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(192, 57, 43), 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));

            // 修复：使用完整路径 javax.swing.Timer 解决冲突
            javax.swing.Timer timer = new javax.swing.Timer(2000, e -> {
                spellField.setText("");
                spellField.setEnabled(true);
                spellField.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
                submitSpellBtn.setEnabled(true);
                feedbackLabel.setText("请重新拼写...");
                feedbackLabel.setForeground(new Color(230, 126, 34));
                spellField.requestFocus();
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    /**
     * 进入下一题
     * 选择题答对 → 拼写题
     * 拼写题答对 → 下一个单词（或完成）
     */
    private void moveToNext() {
        nextButton.setVisible(false);

        if (currentPhase == 0) {
            // 切换到拼写阶段
            currentPhase = 1;
            loadCurrentWord();
        } else {
            // 完成当前单词，进入下一个
            currentPhase = 0;
            currentIndex++;
            if (currentIndex < wordList.size()) {
                loadCurrentWord();
            } else {
                showCompletionDialog();
            }
        }
    }

    /**
     * 切换题目类型面板
     */
    private void showPhase(String phase) {
        CardLayout cl = (CardLayout) questionPanel.getLayout();
        cl.show(questionPanel, phase);
    }

    /**
     * 显示学习完成对话框
     */
    private void showCompletionDialog() {
        int total = wordList.size();
        double accuracy = total > 0 ? (double) totalCorrect / total * 100 : 0;

        String message = String.format(
                "<html><center>" +
                        "<h2>🎉 %s完成！</h2>" +
                        "<p>本次学习单词数：<b>%d</b> 个</p>" +
                        "<p>答题正确率：<b>%.1f%%</b></p>" +
                        "<p>累计错误：<b>%d</b> 次</p>" +
                        "<br><p>继续加油！</p>" +
                        "</center></html>",
                isReview ? "复习" : "背诵",
                total, accuracy, errorCount
        );

        JOptionPane.showMessageDialog(this,
                message,
                "学习完成",
                JOptionPane.INFORMATION_MESSAGE);

        dispose();
    }
}