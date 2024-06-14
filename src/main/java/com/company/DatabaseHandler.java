package com.company;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// 连接数据库
public class DatabaseHandler {
    private static final int target_score = 32;    // 必须修满劳动学时分
    private static final String URL = "jdbc:mysql://localhost:3306/ldxssystem";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "ldmw0915";
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    // 登录 验证
    public static boolean verifyLogin(String username, String password) {
        String sql_0 = "SELECT * FROM accounts WHERE username = ? AND password = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement( sql_0 )) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next(); // 如果存在匹配的用户名和密码，则返回 true，否则返回 false
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // 发生异常时也返回 false
        }
    }

    // 查询学生信息表
    public static Student queryStudent(String studentName, String studentNumber) {
        String sql_1 = "SELECT student_name, student_number, student_grade, student_class, labor_score FROM students WHERE student_name = ? AND student_number = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement( sql_1 )) {
            statement.setString(1, studentName);
            statement.setString(2, studentNumber);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString("student_name");
                String number = resultSet.getString("student_number");
                String studentGrade = resultSet.getString("student_grade");
                String studentClass = resultSet.getString("student_class");
                int laborScore = resultSet.getInt("labor_score");
                int scoreDiff = laborScore - target_score;
                boolean isPass = scoreDiff >= 0;
                return new Student(name, number, studentGrade, studentClass, laborScore, scoreDiff, isPass);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    // 查询整个班级 学生信息
    public static List<Student> queryStudentsByClass(String gradeName, String className) {
        List<Student> students = new ArrayList<>();
        String sql_2 = "SELECT student_name, student_number, student_grade, student_class, labor_score FROM students WHERE student_grade = ? AND student_class = ? ORDER BY student_number ASC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql_2)) {
            statement.setString(1, gradeName);
            statement.setString(2, className);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String name = resultSet.getString("student_name");
                    String number = resultSet.getString("student_number");
                    String studentGrade = resultSet.getString("student_grade");
                    String studentClass = resultSet.getString("student_class");
                    int laborScore = resultSet.getInt("labor_score");
                    int scoreDiff = laborScore - target_score;
                    boolean isPass = scoreDiff >= 0;
                    students.add(new Student(name, number, studentGrade, studentClass, laborScore, scoreDiff, isPass));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    // 获取年级信息
    public static List<String> getAllGrades() {
        String sql_3 = "SELECT * FROM grades";
        List<String> gradeList = new ArrayList<>();
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery( sql_3 )) {    //查询
            // 将获取到的年级信息都存在 gradeList 中
            while (resultSet.next()) {
                String gradeName = resultSet.getString("grade_name");
                gradeList.add(gradeName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // 可以根据需要处理异常，比如记录日志或者弹出错误消息框
        }
        return gradeList;
    }

    // 获取当前所选年级对应的班级信息
    public  static  List<String> getAllClass(String gradeName) {
        String sql_4 = "SELECT * FROM gradeclasss WHERE grade_name = ?";
        List<String> classList = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement( sql_4 )) {    //查询
            statement.setString(1, gradeName);
            ResultSet resultSet = statement.executeQuery();
            // 将获取到的班级信息都存在 gradeList 中
            while (resultSet.next()) {
                String className = resultSet.getString("class_name");
                classList.add(className);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // 可以根据需要处理异常，比如记录日志或者弹出错误消息框
        }
        return classList;
    }

    // 删除 年级表（班级表在设计时已经添加为 外键约束，会直接被级联删除）
    public static boolean deleteGrade(String gradeName) {
        String sql_5 = "DELETE FROM grades WHERE grade_name = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement( sql_5 )) {
            preparedStatement.setString(1, gradeName);
            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // 数据库 “增” 操作。添加数据到表中
    public static void saveGradeAndClass(String grade, String className) {
        String sql_6 = "INSERT IGNORE INTO grades (grade_name) VALUES (?)";
        String sql_7 = "INSERT IGNORE INTO gradeclasss (grade_name, class_name) VALUES (?, ?)";

        try (Connection connection = getConnection()) {
            // 保存年级到 grades 表
            try (PreparedStatement gradeStatement = connection.prepareStatement( sql_6) ) {
                gradeStatement.setString(1, grade);
                gradeStatement.executeUpdate();
            }

            // 保存年级和班级到 gradeclasss 表
            try (PreparedStatement classStatement = connection.prepareStatement( sql_7) ) {
                classStatement.setString(1, grade);
                classStatement.setString(2, className);
                classStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // 班级花名册中学生信息，导入 students 表中
    public static void saveStudents(List<Student> students) {
        String sql_8 = "INSERT IGNORE INTO students (student_name, student_number, student_grade, student_class) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement( sql_8 )) {
            for (Student student : students) {
                statement.setString(1, student.getName());
                statement.setString(2, student.getNumber());
                statement.setString(3, student.getStudentGrade());
                statement.setString(4, student.getStudentClass());
                statement.addBatch();
                //  将单个 SQL 语句添加到当前 PreparedStatement 对象的批处理中。每次调用 addBatch() 都会将当前的 SQL 命令添加到批处理列表中，但不会立即执行。
            }
            statement.executeBatch(); // 执行批处理中所有的 SQL 语句。它返回一个包含每个命令影响的行数的数组。
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 更新学生的 labor_score
    public static void updateLaborScores(List<StudentActionInfo> matchingStudents, double ldxsValue) {
        String sql_9 = "UPDATE students SET labor_score = labor_score + ? WHERE student_name = ? AND student_number = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql_9)) {
            for (StudentActionInfo studentInfo : matchingStudents) {
                statement.setDouble(1, ldxsValue);
                statement.setString(2, studentInfo.getName());
                statement.setString(3, studentInfo.getNumber());
                statement.addBatch(); // 添加到批处理
            }
            statement.executeBatch(); // 执行批处理
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 检查活动是否已上传
    public static boolean isEventUploaded(String eventName) {
        String sql_10 = "SELECT labor_event FROM events WHERE labor_event = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql_10)) {
            statement.setString(1, eventName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next(); // 如果有结果集，则活动已上传
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 记录上传的活动名
    public static boolean recordUploadedEvent(String eventName) {
        String sql_11 = "INSERT INTO events (labor_event) VALUES (?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql_11)) {
            statement.setString(1, eventName);
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 插入 学生参加活动的记录
    public static void RecordActions(List<StudentActionInfo> matchingStudents, String eventName) {
        String sql_12 = "INSERT INTO studentactions (student_name, student_number, labor_event) VALUES (?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql_12)) {
            for (StudentActionInfo studentInfo : matchingStudents) {
                statement.setString(1, studentInfo.getName());
                statement.setString(2, studentInfo.getNumber());
                statement.setString(3, eventName);
                statement.addBatch(); // 添加到批处理
            }
            statement.executeBatch(); // 执行批处理
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 检查 参加活动的学生是否 在数据库students表中
    public static boolean areAllStudentsExist(List<StudentActionInfo> studentList) {
        String sql_13 = "SELECT COUNT(*) FROM students WHERE student_name = ? AND student_number = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql_13)) {
            for (StudentActionInfo student : studentList) {
                statement.setString(1, student.getName());
                statement.setString(2, student.getNumber());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        if (count == 0) {
                            return false; // 如果任何一个学生不在数据库中，则返回 false
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true; // 如果所有学生都存在于数据库中，则返回 true
    }

    // 导出数据，查询整个班级的 劳动学时完成情况
    public static List<Student> getStudentsByGradeAndClass(String studentGrade, String studentClass) {
        List<Student> students = new ArrayList<>();
        String sql_14 = "SELECT student_name, student_number, labor_score FROM students WHERE student_grade = ? AND student_class = ? ORDER BY student_number ASC";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement( sql_14 )) {
            statement.setString(1, studentGrade);
            statement.setString(2, studentClass);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String name = resultSet.getString("student_name");
                    String studentNumber = resultSet.getString("student_number");
                    int laborScore = resultSet.getInt("labor_score");
                    int scoreDiff = laborScore - target_score;
                    boolean isPass = scoreDiff >= 0;
                    students.add(new Student(name, studentNumber, studentGrade, studentClass, laborScore, scoreDiff, isPass));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    // 导出数据，查询某一学生 参加的活动情况。
    public static List getStudentAttendActions(String studentName, String studentNumber) {
        List<String> studentAttendActions = new ArrayList<>();
        String sql_15 = "SELECT labor_event FROM studentactions WHERE student_name = ? AND student_number = ?";
        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(( sql_15))) {
            statement.setString(1, studentName);
            statement.setString(2, studentNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    studentAttendActions.add(resultSet.getString("labor_event"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return studentAttendActions;
    }
}
