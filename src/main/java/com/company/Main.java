package com.company;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {

        Connection connection = null;
        try {
            // 获取SQLite数据库连接
            connection = DatabaseHandler.getConnection();
            System.out.println("成功连接到SQLite数据库！");
            DatabaseHandler.createTables();
            DatabaseHandler.registerAccount("root","root123");
            // 打开图形化界面
            JFrame fr = new mainwindow();

        } catch (SQLException e) {
            // 显示数据库连接失败的消息框
            JOptionPane.showMessageDialog(null, "数据库连接失败！", "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}