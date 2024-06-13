/*
 * Created by JFormDesigner on Sun Jun 09 15:54:01 CST 2024
 */

package com.company;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.io.IOException;
import java.util.List;

/**
 * @author tripelZ （向锦弘）
 */
public class mainwindow extends JFrame {

    public mainwindow() {
        this.setVisible(true);          // 显示窗口
        this.setResizable(false);       // 禁止窗口可最大化
        setDefaultCloseOperation(EXIT_ON_CLOSE);  // 设置关闭窗口时的操作
        initComponents();       // 加载窗口界面
        loadGradeData();        // 加载年级、
        loadClassData();        // 班级下拉框
    }
    // 加载年级信息 （下拉列表）
    private void loadGradeData() {
        // 获取年级数据
        List<String> gradeList = DatabaseHandler.getAllGrades();
        // 清空下拉框
        stuGrade.removeAllItems();
        // 将数据库中的数据添加到下拉框中
        for (String grade : gradeList) {
            stuGrade.addItem(grade);
        }
    }
    // 同上方法加载 班级信息
    private void loadClassData() {
        String nowGrade = (String)stuGrade.getSelectedItem();   // 获取下拉列表当前的选中项
        List<String> classList = DatabaseHandler.getAllClass(nowGrade);
        stuClass.removeAllItems();
        for (String classs : classList){
            stuClass.addItem(classs);
        }
    }

    private JFrame loginFr; // 保存登录界面的引用
    private void LoginbuttonActionPerformed(ActionEvent e) {
        if (loginFr == null || !loginFr.isVisible()) {         // 回调函数，启用登录权限
            loginFr = new Loginwindow(new LoginCallback(){
                @Override
                public void onLoginSuccess() {
                    // 启用受限功能按钮  (两个复选框、三个上传按钮)
                    sureexport.setEnabled(true);
                    suredelete.setEnabled(true);
                    scBmb.setEnabled(true);
                    scQdb.setEnabled(true);
                    scQtb.setEnabled(true);
                    Qdsc.setEnabled(true);
                    scHmc.setEnabled(true);
                }
                @Override
                public void onLoginFailure() {
                    // 登录失败，继续禁用
                    sureexport.setEnabled(false);
                    suredelete.setEnabled(false);
                    scBmb.setEnabled(false);
                    scQdb.setEnabled(false);
                    scQtb.setEnabled(false);
                    Qdsc.setEnabled(false);
                    scHmc.setEnabled(false);
                }
            });
        } else {
            loginFr.toFront();    // 显示登录界面
        }
    }

    // 监听 前置导出按钮
    private void sureexportActionPerformed(ActionEvent e) {
        // 如果前置复选框被选中，启用该按钮
        exportbutton.setEnabled(sureexport.isSelected());
    }
    // 监听 前置删除按钮
    private void suredeleteActionPerformed(ActionEvent e) {
        // 如果前置复选框被选中，启用该按钮
        deletebutton.setEnabled(suredelete.isSelected());
    }

    // 上传 报名表
    private void scBmbActionPerformed(ActionEvent e) {
        handleFileUpload_left("报名表");
    }
    // 上传签到表
    private void scQdbActionPerformed(ActionEvent e) {
        handleFileUpload_left("签到表");
    }
    // 上传签退表
    private void scQtbActionPerformed(ActionEvent e) {
        handleFileUpload_left("签退表");
    }
    // 确定上传
    private void QdscActionPerformed(ActionEvent e) {
        // 验证 hdbeizhu 文本框内容是否为空
        String hdbeizhuText = hdbeizhu.getText().trim();
        if (hdbeizhuText.isEmpty()) {
            JOptionPane.showMessageDialog(null, "活动备注不能为空！");
            return;
        }
        // 验证 ldxs 文本框中输入的内容是否为大于等于0、小于等于10的正数
        String ldxsText = ldxs.getText().trim();
        double ldxsValue;
        try {
            ldxsValue = Double.parseDouble(ldxsText);
            if (ldxsValue <= 0 || ldxsValue > 10) {
                JOptionPane.showMessageDialog(null, "请输入大于0、小于等于10的数字！");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "劳动学时必须为正数，请合法输入！");
            return;
        }

        // 获取当前项目所在路径
        String projectRootPath = System.getProperty("user.dir");
        // 构造路径
        String basePath = projectRootPath + File.separator + "systemData" + File.separator + "excelData" + File.separator + hdbeizhuText;
        // 调用 ExcelHandler 中的方法处理 Excel 表格数据
        try {
            // 查找报名表文件// 查找签到表文件// 查找签退表文件
            File registrationFile = findExcelFile(basePath, "报名表");
            File signInFile = findExcelFile(basePath, "签到表");
            File signOutFile = findExcelFile(basePath, "签退表");
            if (registrationFile == null || signInFile == null || signOutFile == null) {
                JOptionPane.showMessageDialog(null, "找不到所需的Excel文件，请检查文件是否上传！");
                return;
            }
            // 读取报名表
            List<StudentActionInfo> registeredStudents = ExcelHandler.readRegistrationFile(registrationFile);
            // 读取签到表
            List<StudentActionInfo> signInStudents = ExcelHandler.readAttendanceFile(signInFile);
            // 读取签退表
            List<StudentActionInfo> signOutStudents = ExcelHandler.readAttendanceFile(signOutFile);
            // 查找匹配的学生
            List<StudentActionInfo> matchingStudents = ExcelHandler.findMatchingStudents(registeredStudents, signInStudents, signOutStudents);

            // 更新数据库
            if (DatabaseHandler.isEventUploaded(hdbeizhuText)) {
                JOptionPane.showMessageDialog(null, "该活动已上传，请勿重复提交！");
                return;
            } else {
                if (DatabaseHandler.areAllStudentsExist(matchingStudents)) {
                    if (DatabaseHandler.recordUploadedEvent(hdbeizhuText)) {   // 活动上传成功
                        DatabaseHandler.updateLaborScores(matchingStudents, ldxsValue);
                        DatabaseHandler.RecordActions(matchingStudents, hdbeizhuText);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "学生数据不全，请先更新学生花名册！");
                    return;
                }
            }

            JOptionPane.showMessageDialog(null, "劳动学时已成功更新！");
        } catch (IOException ioException) {
            ioException.printStackTrace();
            JOptionPane.showMessageDialog(null, "读取文件失败: " + ioException.getMessage());
        }
    }
    // 查找Excel文件
    private File findExcelFile(String basePath, String fileNamePrefix) {
        File dir = new File(basePath);
        File[] matchingFiles = dir.listFiles((dir1, name) -> name.startsWith(fileNamePrefix) && (name.endsWith(".xlsx") || name.endsWith(".xls")));
        return (matchingFiles != null && matchingFiles.length > 0) ? matchingFiles[0] : null;
    }

    // 上传班级花名册
    private void scHmcActionPerformed(ActionEvent e) {
        handleFileUpload_right();
    }

    // 上传 活动文件至指定文件夹
    private void handleFileUpload_left( String fileName ) {  // 参数 父文件夹名、excel文件名
        // 获取文本框中的子文件夹名称
        String subFolderName = hdbeizhu.getText().trim();
        if (subFolderName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "活动备注不能为空！");
            return;
        }

        // 创建文件选择器
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            // 获取上传的文件名 （同时去除后缀）
            String selectedFileName = removeFileExtension(selectedFile.getName());
            if (!selectedFileName.equals(fileName)) {
                JOptionPane.showMessageDialog(null, "请上传对应的Excel表！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // 调用 FileUploader 中的方法处理文件上传
            FileUploader.handleFileUpload("excelData", subFolderName, selectedFile);
        }
    }
    // 上传 花名册到指定文件
    private  void handleFileUpload_right() {
        // 创建文件选择器
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // 去除文件扩展名
            String fileNameWithoutExtension = removeFileExtension(selectedFile.getName());
            // 验证文件名格式
            if (!isValidFileName(fileNameWithoutExtension)) {
                JOptionPane.showMessageDialog(null, "请上传规范命名的班级花名册！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // 获取文件名的前五位作为 年级/子文件夹名
            String grade = fileNameWithoutExtension.length() >= 5 ? fileNameWithoutExtension.substring(0, 5) : fileNameWithoutExtension;
            // 获取文件名除去前五位后的部分作为班级名称
            String className = fileNameWithoutExtension.length() > 5 ? fileNameWithoutExtension.substring(5) : "";

            // 调用 FileUploader 中的方法处理文件上传
            FileUploader.handleFileUpload("stuGradeData", grade, selectedFile);

            // 保存年级和班级到数据库
            DatabaseHandler.saveGradeAndClass(grade, className);

            // 读取excel文件信息
            List<Student> students = ExcelHandler.readClassRoster(selectedFile, grade, className);
            DatabaseHandler.saveStudents(students);

            loadGradeData();    //插入成功之后 重新加载年级、班级数据以更新下拉框
            loadClassData();
        }
    }
    // 去除文件名的 后缀部分
    private String removeFileExtension(String fileName) {
        if (fileName != null && fileName.lastIndexOf('.') > 0) {
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        return fileName;
    }
    // 正则表达式验证 文件名是否为 ： 年级+班级
    private boolean isValidFileName(String fileName) {
        // 假设文件名格式为: YYYY级专业班级号班，例如: 2021级计算机科学与技术1班
        String regex = "^\\d{4}级[\\u4e00-\\u9fa5]+\\d+班$";
        return fileName.matches(regex);
    }

    //  查询按钮
    private void searchButtonActionPerformed(ActionEvent e) {
        // 获取文本框中的内容
        String studentName = stuName.getText();
        String studentNumber = stuNumber.getText();
        // 每次查询，先情况查询输出表格中的数据
        DefaultTableModel tableModel = (DefaultTableModel) table1.getModel();
        tableModel.setRowCount(0);  // 清空表格数据
        // 如果 学生 姓名 、 学号 文本框有为空的，则查询该班级信息
        if (studentName.isEmpty() || studentNumber.isEmpty()) {
            String nowGrade = (String)stuGrade.getSelectedItem();
            String nowClass = (String)stuClass.getSelectedItem();
            List<Student> students = DatabaseHandler.queryStudentsByClass(nowGrade,nowClass);
            for (Student student : students) {
                tableModel.addRow(new Object[]{ // 将查询到的学生信息添加到表格
                        student.getName(),
                        student.getNumber(),
                        student.getStudentGrade(),
                        student.getStudentClass(),
                        student.getLaborScore(),
                        student.getScoreDiff(),
                        student.isPass() ? "是" : "否"
                });
            }
        } else {    // 如果输入了 学生 姓名、学号。则查询该学习
            Student student = DatabaseHandler.queryStudent(studentName, studentNumber);
            if (student != null) {
                tableModel.addRow(new Object[]{ // 将查询到的学生信息添加到表格
                        student.getName(),
                        student.getNumber(),
                        student.getStudentGrade(),
                        student.getStudentClass(),
                        student.getLaborScore(),
                        student.getScoreDiff(),
                        student.isPass() ? "是" : "否"
                });
            } else {
                JOptionPane.showMessageDialog(null, "该学生信息不存在！", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // 删除按钮
    private void deletebuttonActionPerformed(ActionEvent e) {
        String selectedGrade = (String) stuGrade.getSelectedItem();  // 删除年级下拉列表 当前选中项
        if (selectedGrade != null) {
            boolean success = DatabaseHandler.deleteGrade(selectedGrade);
            if (success) {
                JOptionPane.showMessageDialog(this, "年级删除成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
                loadGradeData();    // 重新加载年级、班级数据以更新下拉框  ***这里是关键代码哦***
                loadClassData();
            } else {
                JOptionPane.showMessageDialog(this, "年级删除失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "请选择一个年级进行删除！", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void stuGradeActionPerformed(ActionEvent e) {
        loadClassData();    // 根据 年级 当前索引项，加载对应年级的 班级 信息
    }

    private void exportbuttonActionPerformed(ActionEvent e) {
        String studentGrade = (String)stuGrade.getSelectedItem();
        String studentClass = (String)stuClass.getSelectedItem();
        List<Student> grade_class_student = DatabaseHandler.getStudentsByGradeAndClass(studentGrade,studentClass);
        // 错误处理，不过应该不会出现
        if (grade_class_student.isEmpty()) {
            JOptionPane.showMessageDialog(null, "没有找到符合条件的学生！");
            return;
        }
        // 导出到 Excel 文件
        try {
            ExcelHandler.exportStudentsToExcel(grade_class_student, studentGrade, studentClass);
            JOptionPane.showMessageDialog(null, "学生数据导出成功！");
        } catch (IOException eg) {
            eg.printStackTrace();
            JOptionPane.showMessageDialog(null, "导出文件失败: " + eg.getMessage());
        }

    }

    // 绘制 窗口
    private void initComponents() {

        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        gradetext = new JLabel();
        classtext = new JLabel();
        stuGrade = new JComboBox();
        stuClass = new JComboBox();
        sureexport = new JCheckBox();
        suredelete = new JCheckBox();
        exportbutton = new JButton();
        deletebutton = new JButton();
        Loginbutton = new JButton();
        nametext = new JLabel();
        idtext = new JLabel();
        stuName = new JTextField();
        stuNumber = new JTextField();
        searchButton = new JButton();
        kongbai = new JLabel();
        scrollPane1 = new JScrollPane();
        table1 = new JTable();
        qdqtjpanel = new JPanel();
        scBmb = new JButton();
        scQdb = new JButton();
        scQtb = new JButton();
        bztext = new JLabel();
        hdbeizhu = new JTextField();
        ldxstext = new JLabel();
        ldxs = new JTextField();
        Qdsc = new JButton();
        hmcjpanel = new JPanel();
        scHmc = new JButton();

        //======== this ========
        setTitle("XTU\u8ba1\u7b97\u673a\u5b66\u9662\u5b66\u751f\u52b3\u52a8\u5b66\u65f6\u7ba1\u7406\u7cfb\u7edf");
        var contentPane = getContentPane();
        contentPane.setLayout(null);

        //---- gradetext ----
        gradetext.setText("\u5e74\u7ea7\uff1a");
        gradetext.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 16));
        contentPane.add(gradetext);
        gradetext.setBounds(30, 40, 55, 30);

        //---- classtext ----
        classtext.setText("\u73ed\u7ea7\uff1a");
        classtext.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 16));
        contentPane.add(classtext);
        classtext.setBounds(255, 40, 55, 30);

        //---- stuGrade ----
        stuGrade.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 14));
        stuGrade.addActionListener(e -> stuGradeActionPerformed(e));
        contentPane.add(stuGrade);
        stuGrade.setBounds(85, 40, 130, 30);

        //---- stuClass ----
        stuClass.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 14));
        contentPane.add(stuClass);
        stuClass.setBounds(310, 40, 185, 30);

        //---- sureexport ----
        sureexport.setEnabled(false);
        sureexport.addActionListener(e -> sureexportActionPerformed(e));
        contentPane.add(sureexport);
        sureexport.setBounds(655, 40, 20, 30);

        //---- suredelete ----
        suredelete.setEnabled(false);
        suredelete.addActionListener(e -> suredeleteActionPerformed(e));
        contentPane.add(suredelete);
        suredelete.setBounds(655, 105, 20, 30);

        //---- exportbutton ----
        exportbutton.setText("\u5bfc\u51fa");
        exportbutton.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 16));
        exportbutton.setEnabled(false);
        exportbutton.addActionListener(e -> exportbuttonActionPerformed(e));
        contentPane.add(exportbutton);
        exportbutton.setBounds(680, 40, 70, 29);

        //---- deletebutton ----
        deletebutton.setText("\u5220\u9664");
        deletebutton.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 16));
        deletebutton.setEnabled(false);
        deletebutton.addActionListener(e -> deletebuttonActionPerformed(e));
        contentPane.add(deletebutton);
        deletebutton.setBounds(680, 105, 70, 29);

        //---- Loginbutton ----
        Loginbutton.setText("\u767b\u5f55");
        Loginbutton.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 17));
        Loginbutton.addActionListener(e -> LoginbuttonActionPerformed(e));
        contentPane.add(Loginbutton);
        Loginbutton.setBounds(535, 40, 75, 30);

        //---- nametext ----
        nametext.setText("\u59d3\u540d\uff1a");
        nametext.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 16));
        contentPane.add(nametext);
        nametext.setBounds(30, 105, 55, 30);

        //---- idtext ----
        idtext.setText("\u5b66\u53f7\uff1a");
        idtext.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 16));
        contentPane.add(idtext);
        idtext.setBounds(255, 105, 55, 30);

        //---- stuName ----
        stuName.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 14));
        contentPane.add(stuName);
        stuName.setBounds(85, 105, 130, 30);

        //---- stuNumber ----
        stuNumber.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 14));
        contentPane.add(stuNumber);
        stuNumber.setBounds(310, 105, 185, 30);

        //---- searchButton ----
        searchButton.setText("\u67e5\u8be2");
        searchButton.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 16));
        searchButton.addActionListener(e -> searchButtonActionPerformed(e));
        contentPane.add(searchButton);
        searchButton.setBounds(535, 105, 75, 30);
        contentPane.add(kongbai);
        kongbai.setBounds(755, 60, 15, 60);

        //======== scrollPane1 ========
        {
            scrollPane1.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 14));

            //---- table1 ----
            table1.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 14));
            table1.setModel(new DefaultTableModel(
                new Object[][] {
                },
                new String[] {
                    "\u59d3\u540d", "\u5b66\u53f7", "\u5e74\u7ea7", "\u73ed\u7ea7", "\u5df2\u4fee\u5b66\u5206", "\u4f59\u7f3a\u5b66\u5206", "\u662f\u5426\u8fbe\u6807"
                }
            ) {
                boolean[] columnEditable = new boolean[] {
                    false, false, false, false, false, false, false
                };
                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return columnEditable[columnIndex];
                }
            });
            {
                TableColumnModel cm = table1.getColumnModel();
                cm.getColumn(0).setResizable(false);
                cm.getColumn(0).setMaxWidth(90);
                cm.getColumn(0).setPreferredWidth(90);
                cm.getColumn(1).setResizable(false);
                cm.getColumn(1).setMaxWidth(130);
                cm.getColumn(1).setPreferredWidth(130);
                cm.getColumn(2).setResizable(false);
                cm.getColumn(2).setMinWidth(70);
                cm.getColumn(2).setMaxWidth(90);
                cm.getColumn(2).setPreferredWidth(90);
                cm.getColumn(3).setResizable(false);
                cm.getColumn(4).setResizable(false);
                cm.getColumn(4).setMaxWidth(90);
                cm.getColumn(4).setPreferredWidth(70);
                cm.getColumn(5).setResizable(false);
                cm.getColumn(5).setMaxWidth(90);
                cm.getColumn(5).setPreferredWidth(70);
                cm.getColumn(6).setResizable(false);
                cm.getColumn(6).setMinWidth(25);
                cm.getColumn(6).setMaxWidth(70);
                cm.getColumn(6).setPreferredWidth(70);
            }
            table1.setPreferredScrollableViewportSize(new Dimension(450, 425));
            scrollPane1.setViewportView(table1);
        }
        contentPane.add(scrollPane1);
        scrollPane1.setBounds(30, 145, 720, 205);

        //======== qdqtjpanel ========
        {
            qdqtjpanel.setBorder(new TitledBorder("\u6587\u4ef6\u547d\u540d\u683c\u5f0f\uff1a\u62a5\u540d\u8868/\u7b7e\u5230\u8868/\u7b7e\u9000\u8868"));
            qdqtjpanel.setLayout(null);

            //---- scBmb ----
            scBmb.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 14));
            scBmb.setText("\u62a5\u540d\u8868");
            scBmb.setEnabled(false);
            scBmb.addActionListener(e -> scBmbActionPerformed(e));
            qdqtjpanel.add(scBmb);
            scBmb.setBounds(20, 35, 115, 115);

            //---- scQdb ----
            scQdb.setText("\u7b7e\u5230\u8868");
            scQdb.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 14));
            scQdb.setEnabled(false);
            scQdb.addActionListener(e -> scQdbActionPerformed(e));
            qdqtjpanel.add(scQdb);
            scQdb.setBounds(150, 35, 100, 50);

            //---- scQtb ----
            scQtb.setText("\u7b7e\u9000\u8868");
            scQtb.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 14));
            scQtb.setEnabled(false);
            scQtb.addActionListener(e -> scQtbActionPerformed(e));
            qdqtjpanel.add(scQtb);
            scQtb.setBounds(150, 100, 100, 50);

            //---- bztext ----
            bztext.setText("\u6d3b\u52a8\u5907\u6ce8\uff1a");
            qdqtjpanel.add(bztext);
            bztext.setBounds(265, 20, 60, 25);

            //---- hdbeizhu ----
            hdbeizhu.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 12));
            qdqtjpanel.add(hdbeizhu);
            hdbeizhu.setBounds(265, 50, 170, 30);

            //---- ldxstext ----
            ldxstext.setText("\u52b3\u52a8\u5b66\u65f6\uff1a");
            qdqtjpanel.add(ldxstext);
            ldxstext.setBounds(265, 90, 60, 25);

            //---- ldxs ----
            ldxs.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 12));
            qdqtjpanel.add(ldxs);
            ldxs.setBounds(265, 120, 55, 30);

            //---- Qdsc ----
            Qdsc.setText("\u5f00\u59cb\u4e0a\u4f20");
            Qdsc.setEnabled(false);
            Qdsc.addActionListener(e -> QdscActionPerformed(e));
            qdqtjpanel.add(Qdsc);
            Qdsc.setBounds(340, 95, 95, 55);

            {
                // compute preferred size
                Dimension preferredSize = new Dimension();
                for(int i = 0; i < qdqtjpanel.getComponentCount(); i++) {
                    Rectangle bounds = qdqtjpanel.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = qdqtjpanel.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                qdqtjpanel.setMinimumSize(preferredSize);
                qdqtjpanel.setPreferredSize(preferredSize);
            }
        }
        contentPane.add(qdqtjpanel);
        qdqtjpanel.setBounds(25, 370, 460, 165);

        //======== hmcjpanel ========
        {
            hmcjpanel.setBorder(new TitledBorder("\u6587\u4ef6\u540d\u683c\u5f0f\u5982\uff1a2021\u7ea7\u8ba1\u7b97\u673a\u79d1\u5b66\u4e0e\u6280\u672f1\u73ed"));
            hmcjpanel.setLayout(null);

            //---- scHmc ----
            scHmc.setText("\u4e0a\u4f20\u73ed\u7ea7\u82b1\u540d\u518c");
            scHmc.setFont(new Font("\u5b8b\u4f53", Font.PLAIN, 14));
            scHmc.setEnabled(false);
            scHmc.addActionListener(e -> scHmcActionPerformed(e));
            hmcjpanel.add(scHmc);
            scHmc.setBounds(50, 40, 160, 95);

            {
                // compute preferred size
                Dimension preferredSize = new Dimension();
                for(int i = 0; i < hmcjpanel.getComponentCount(); i++) {
                    Rectangle bounds = hmcjpanel.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = hmcjpanel.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                hmcjpanel.setMinimumSize(preferredSize);
                hmcjpanel.setPreferredSize(preferredSize);
            }
        }
        contentPane.add(hmcjpanel);
        hmcjpanel.setBounds(495, 370, 260, 165);

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
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JLabel gradetext;
    private JLabel classtext;
    private JComboBox stuGrade;
    private JComboBox stuClass;
    private JCheckBox sureexport;
    private JCheckBox suredelete;
    private JButton exportbutton;
    private JButton deletebutton;
    private JButton Loginbutton;
    private JLabel nametext;
    private JLabel idtext;
    private JTextField stuName;
    private JTextField stuNumber;
    private JButton searchButton;
    private JLabel kongbai;
    private JScrollPane scrollPane1;
    private JTable table1;
    private JPanel qdqtjpanel;
    private JButton scBmb;
    private JButton scQdb;
    private JButton scQtb;
    private JLabel bztext;
    private JTextField hdbeizhu;
    private JLabel ldxstext;
    private JTextField ldxs;
    private JButton Qdsc;
    private JPanel hmcjpanel;
    private JButton scHmc;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
