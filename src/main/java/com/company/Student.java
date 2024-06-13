package com.company;

public class Student {
    private String name;
    private String number;
    private String studentGrade;
    private String studentClass;
    private int laborScore;
    private int scoreDiff;
    private boolean isPass;

    public Student(String name, String number,String studentGrade, String studentClass, int laborScore, int scoreDiff, boolean isPass) {
        this.name = name;
        this.number = number;
        this.studentGrade = studentGrade;
        this.studentClass = studentClass;
        this.laborScore = laborScore;
        this.scoreDiff = scoreDiff;
        this.isPass = isPass;
    }

    // 添加 getter 方法以便获取学生信息
    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getStudentGrade(){ return studentGrade; }

    public String getStudentClass() {
        return studentClass;
    }

    public int getLaborScore() {
        return laborScore;
    }

    public int getScoreDiff() {
        return scoreDiff;
    }

    public boolean isPass() {
        return isPass;
    }
}
