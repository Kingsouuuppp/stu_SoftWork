package com.company;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUploader {     // 参数            父文件夹名、             子文件夹名、      要上传的文件
    public static void handleFileUpload(String uploadFolder, String subFolderName, File selectedFile) {
        // 获取项目根目录
        String projectRootPath = System.getProperty("user.dir");
        // 指定保存文件的主目录
        String uploadDirPath = projectRootPath + File.separator + "systemData" + File.separator + uploadFolder;
        File destinationDir = new File(uploadDirPath);

        // 检查主目录是否存在
        if (!destinationDir.exists()) {
            if (destinationDir.mkdirs()) {
                System.out.println("systemData 文件夹创建成功: " + uploadDirPath);
            } else {
                System.out.println("systemData 文件夹创建失败: " + uploadDirPath);
                return;
            }
        }

        // 在主目录下创建子文件夹
        String subFolderPath = uploadDirPath + File.separator + subFolderName;
        File subFolder = new File(subFolderPath);
        if (!subFolder.exists()) {
            if (subFolder.mkdirs()) {
                System.out.println("子文件夹创建成功: " + subFolderPath);
            } else {
                System.out.println("子文件夹创建失败: " + subFolderPath);
                return;
            }
        }

        // 构建目标文件路径
        Path destinationPath = Paths.get(subFolder.getAbsolutePath(), selectedFile.getName());

        // 检查目标文件是否存在，如果存在则先删除原文件
        File destinationFile = destinationPath.toFile();
        if (destinationFile.exists()) {
            if (destinationFile.delete()) {
                System.out.println("原文件已删除: " + destinationPath.toString());
            } else {
                System.out.println("无法删除原文件: " + destinationPath.toString());
                return;
            }
        }

        try {
            // 将选中的文件复制到指定子文件夹
            Files.copy(selectedFile.toPath(), destinationPath);
            JOptionPane.showMessageDialog(null, "文件已成功保存到: " + destinationPath.toString());
        } catch (IOException ioException) {
            ioException.printStackTrace();
            JOptionPane.showMessageDialog(null, "文件保存失败: " + ioException.getMessage());
        }
    }
}
