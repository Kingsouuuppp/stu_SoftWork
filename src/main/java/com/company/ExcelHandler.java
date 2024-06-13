package com.company;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelHandler {

    // ！！！要求 花名册  第一列 为 姓名  第二列为 学号 ！！！ 必须是这种格式。 这影响数据是否读取成功
    public static List<Student> readClassRoster(File excelFile, String grade, String studentClass) {
        List<Student> students = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;    // 跳过第一行（标题行）
                Cell nameCell = row.getCell(0);     // 读取每行第一个单元格（第一列  姓名）
                Cell numberCell = row.getCell(1);   // 读取每行第二个单元格（第一列  学号） excel表中 学号 必须是作为文本类型输入

                if (nameCell != null && numberCell != null) {
                    String name = nameCell.getStringCellValue().trim();     // 获取并去除学生姓名两端空格
                    String number = numberCell.getStringCellValue().trim(); // 获取并去除学生学号两端空格
                    students.add(new Student(name, number, grade, studentClass, 0, 0, false));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return students;
    }

    // 读取报名表中的学生姓名
    public static List<StudentActionInfo> readRegistrationFile(File excelFile) throws IOException {
        List<StudentActionInfo> students_inform = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0); // 获取第一个工作表
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;    // 跳过第一行（标题行）
                Cell nameCell = row.getCell(0); // 获取第一列单元格（学生姓名）
                Cell numberCell = row.getCell(1); // 获取第二列单元格（学生学号）
                if (nameCell != null && nameCell.getCellType() == CellType.STRING &&
                        numberCell != null && numberCell.getCellType() == CellType.STRING) {
                    String name = nameCell.getStringCellValue().trim();
                    String number = numberCell.getStringCellValue().trim();
                    students_inform.add(new StudentActionInfo(name, number));
                }
            }
        }
        return students_inform;
    }
    // 读取签到表或签退表中的学生姓名和学号
    public static List<StudentActionInfo> readAttendanceFile(File excelFile) throws IOException {
        List<StudentActionInfo> students_inform = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0); // 获取第一个工作表
            for (int i = 6; i <= sheet.getLastRowNum(); i++) { // 从第7行开始读取数据（索引从0开始，所以是6）
                Row row = sheet.getRow(i); // 获取当前行对象
                if (row == null) break; // 如果当前行为空，则停止读取

                Cell nameCell = row.getCell(0); // 获取第一列单元格（学生姓名）
                Cell numberCell = row.getCell(1); // 获取第二列单元格（学生学号）
                Cell nextCell = row.getCell(7); // 获取第八列单元格，检查是否为空

                if (nextCell == null || nextCell.getCellType() == CellType.BLANK) break; // 如果第八列单元格为空，则停止读取
                // 验证学生姓名和学号单元格是否为空，并且类型为字符串
                if (nameCell != null && numberCell != null &&
                        nameCell.getCellType() == CellType.STRING &&
                        numberCell.getCellType() == CellType.STRING) {
                    String name = nameCell.getStringCellValue().trim(); // 获取并去除学生姓名两端空格
                    String number = numberCell.getStringCellValue().trim(); // 获取并去除学生学号两端空格
                    students_inform.add(new StudentActionInfo(name, number)); // 将学生信息添加到列表中
                }
            }
        }
        return students_inform;
    }

    // 查找同时存在于报名表、签到表和签退表中的学生
    public static List<StudentActionInfo> findMatchingStudents(List<StudentActionInfo> registeredStudents,
                                                   List<StudentActionInfo> signInStudents,
                                                   List<StudentActionInfo> signOutStudents) {
        List<StudentActionInfo> matchingStudents = new ArrayList<>();
        for (StudentActionInfo stu : registeredStudents) {
            String name = stu.getName();
            String number = stu.getNumber();

            // 检查学生是否同时存在于第二个和第三个列表中
            boolean isInList2 = listContainsStudent(signInStudents, name, number);
            boolean isInList3 = listContainsStudent(signOutStudents, name, number);

            if (isInList2 && isInList3) {
                // 如果学生在所有三个列表中都存在，则保存下来
                matchingStudents.add(new StudentActionInfo(name, number));
            }
        }
        return matchingStudents;
    }

    // 辅助方法：检查列表中是否包含指定的学生信息
    private static boolean listContainsStudent(List<StudentActionInfo> list, String name, String number) {
        for (StudentActionInfo student : list) {
            if (student.getName().equals(name) && student.getNumber().equals(number)) {
                return true;
            }
        }
        return false;
    }

    // 导出数据至 Excel 文档
    public static void exportStudentsToExcel(List<Student> students, String studentGrade, String studentClass) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Class_AllStudents");
        // 表头
        Row headerRow = sheet.createRow(0);
        Cell headerCell1 = headerRow.createCell(0);
        headerCell1.setCellValue("姓名");
        Cell headerCell2 = headerRow.createCell(1);
        headerCell2.setCellValue("学号");
        Cell headerCell3 = headerRow.createCell(2);
        headerCell3.setCellValue("年级");
        Cell headerCell4 = headerRow.createCell(3);
        headerCell4.setCellValue("班级");
        Cell headerCell5 = headerRow.createCell(4);
        headerCell5.setCellValue("已修学分");
        Cell headerCell6 = headerRow.createCell(5);
        headerCell6.setCellValue("余缺学分");
        Cell headerCell7 = headerRow.createCell(6);
        headerCell7.setCellValue("是否达标");
        // 添加信息
        int rowNum = 1;
        for (Student student : students) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getName());
            row.createCell(1).setCellValue(student.getNumber());
            row.createCell(2).setCellValue(student.getStudentGrade());
            row.createCell(3).setCellValue(student.getStudentClass());
            row.createCell(4).setCellValue(student.getLaborScore());
            row.createCell(5).setCellValue(student.getScoreDiff());
            row.createCell(6).setCellValue(student.isPass()?"是":"否");
        }
        // 导出 Excel 表   // 自定义保存路径
        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
        String fileName = studentGrade+studentClass+"学生劳动学时数据.xlsx";
        String filePath = desktopPath + File.separator + fileName;
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }
}
