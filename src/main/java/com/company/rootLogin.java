/*
 * Created by JFormDesigner on Sun Jun 16 05:46:56 CST 2024
 */

package com.company;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * @author 1
 */
public class rootLogin extends JFrame {
    private LoginCallback rootcallback;     // 登录成功与否 回调接口

    public rootLogin(LoginCallback rootcallback) {
        this.rootcallback = rootcallback;
        this.setVisible(true);
        this.setResizable(false);
        initComponents();
    }

    private void button1ActionPerformed(ActionEvent e) {
        String username = t1.getText();
        String password = new String(p1.getPassword());

        if (username.equals("root") && password.equals("root123")) {
            rootcallback.onLoginSuccess();  // 调用成功回调   ！！！
            JOptionPane.showMessageDialog(rootLogin.this, "验证成功");
            this.dispose(); // 关闭登录界面
        } else {
            rootcallback.onLoginFailure();  // 调用失败回调   ！！！
            JOptionPane.showMessageDialog(rootLogin.this, "账号或密码错误");
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        label1 = new JLabel();
        label2 = new JLabel();
        label3 = new JLabel();
        t1 = new JTextField();
        p1 = new JPasswordField();
        button1 = new JButton();
        label4 = new JLabel();

        //======== this ========
        var contentPane = getContentPane();
        contentPane.setLayout(null);

        //---- label1 ----
        label1.setText("\u6839\u8d26\u53f7\u9a8c\u8bc1");
        label1.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 24));
        contentPane.add(label1);
        label1.setBounds(145, 30, 260, 70);

        //---- label2 ----
        label2.setText("\u8d26\u53f7");
        label2.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 14));
        contentPane.add(label2);
        label2.setBounds(90, 115, 45, 30);

        //---- label3 ----
        label3.setText("\u5bc6\u7801");
        label3.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 14));
        contentPane.add(label3);
        label3.setBounds(90, 160, 45, 30);
        contentPane.add(t1);
        t1.setBounds(145, 115, 165, 30);
        contentPane.add(p1);
        p1.setBounds(145, 160, 165, 30);

        //---- button1 ----
        button1.setText("\u767b\u5f55");
        button1.addActionListener(e -> button1ActionPerformed(e));
        contentPane.add(button1);
        button1.setBounds(160, 220, 75, 40);
        contentPane.add(label4);
        label4.setBounds(170, 285, 45, 20);

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
    private JTextField t1;
    private JPasswordField p1;
    private JButton button1;
    private JLabel label4;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
