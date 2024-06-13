package com.company;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        // write your code here
        Connection connection = null;
        try {
            // 获取数据库连接
            connection = DatabaseHandler.getConnection();
            if (connection != null) {
                System.out.println("数据库连接成功！");
                // 打开图形化界面
                JFrame fr = new mainwindow();
            }
        } catch (SQLException e) {
            // 显示数据库连接失败的消息框
            JOptionPane.showMessageDialog(null, "数据库连接失败！", "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            // 确保连接关闭
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
