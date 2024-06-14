/*
 * Created by JFormDesigner on Sun Jun 09 21:08:49 CST 2024
 */

package com.company;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * @author 1
 */
public class Loginwindow extends JFrame {
    private LoginCallback callback;     // 登录成功与否 回调接口

    public Loginwindow(LoginCallback callback) {
        this.callback = callback;
        this.setVisible(true);
        this.setResizable(false);
        initComponents();
    }
    // 登录按钮事件监听
    private void logbuttonActionPerformed(ActionEvent e) {
        String username = usertext.getText();
        String password = new String(passwordfield.getPassword());
//        System.out.println("username:" + username + " password:" + password);
        if (DatabaseHandler.verifyLogin(username, password)) {
            callback.onLoginSuccess();  // 调用成功回调   ！！！
            JOptionPane.showMessageDialog(Loginwindow.this, "登录成功");
            this.dispose(); // 关闭登录界面
        } else {
            callback.onLoginFailure();  // 调用失败回调   ！！！
            JOptionPane.showMessageDialog(Loginwindow.this, "账号或密码错误");
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        label1 = new JLabel();
        label2 = new JLabel();
        label3 = new JLabel();
        usertext = new JTextField();
        passwordfield = new JPasswordField();
        logbutton = new JButton();
        label4 = new JLabel();

        //======== this ========
        var contentPane = getContentPane();
        contentPane.setLayout(null);

        //---- label1 ----
        label1.setText("\u7ba1\u7406\u5458\u767b\u5f55");
        label1.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 24));
        contentPane.add(label1);
        label1.setBounds(155, 40, 265, 45);

        //---- label2 ----
        label2.setText("\u7528\u6237\u540d");
        label2.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 14));
        contentPane.add(label2);
        label2.setBounds(100, 110, 55, 25);

        //---- label3 ----
        label3.setText("\u5bc6\u7801");
        label3.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 14));
        contentPane.add(label3);
        label3.setBounds(115, 155, 40, 25);
        contentPane.add(usertext);
        usertext.setBounds(160, 110, 145, 25);
        contentPane.add(passwordfield);
        passwordfield.setBounds(160, 155, 145, 25);

        //---- logbutton ----
        logbutton.setText("\u767b\u5f55");
        logbutton.addActionListener(e -> logbuttonActionPerformed(e));
        contentPane.add(logbutton);
        logbutton.setBounds(175, 210, 80, 35);
        contentPane.add(label4);
        label4.setBounds(170, 255, 70, 40);

        {
            // compute preferred size
            Dimension preferredSize = new Dimension();
            for(int i = 0; i < contentPane.getComponentCount(); i++) {
                Rectangle bounds = contentPane.getComponent(i).getBounds();
                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
            }
            Insets insets = contentPane.getInsets();
            preferredSize.width += insets.right;
            preferredSize.height += insets.bottom;
            contentPane.setMinimumSize(preferredSize);
            contentPane.setPreferredSize(preferredSize);
        }
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel label1;
    private JLabel label2;
    private JLabel label3;
    private JTextField usertext;
    private JPasswordField passwordfield;
    private JButton logbutton;
    private JLabel label4;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
