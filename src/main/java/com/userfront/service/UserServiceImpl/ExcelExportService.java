package com.userfront.service.UserServiceImpl;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.userfront.domain.User;


public class ExcelExportService {

	public ByteArrayInputStream exportToExcel(List<User> users) {
        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Sheet sheet = workbook.createSheet("users_Details");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("User ID");
            headerRow.createCell(1).setCellValue("customer full Name");
            headerRow.createCell(2).setCellValue("Email");
            headerRow.createCell(3).setCellValue("Contact");
            headerRow.createCell(4).setCellValue("User Name");
//            headerRow.createCell(5).setCellValue("Primary account Num.");
//            headerRow.createCell(6).setCellValue("Secondary account Num.");
//            headerRow.createCell(7).setCellValue("Role");
//            headerRow.createCell(8).setCellValue("Experience");
//            headerRow.createCell(9).setCellValue("currentProjectDetails");
           
            // ... Add other headers for each field

            // Add data rows
            int rowNum = 1;
            for (com.userfront.domain.User user : users) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(user.getUserId());
                row.createCell(1).setCellValue(user.getFirstName() +" "+ user.getLastName());
                row.createCell(2).setCellValue(user.getEmail());
                row.createCell(3).setCellValue(user.getPhone());
                row.createCell(4).setCellValue(user.getUsername());
//                row.createCell(5).setCellValue(user.getPrimaryAccount());
//                row.createCell(6).setCellValue(user.getSavingsAccount());
//                row.createCell(7).setCellValue(user.getRole());
//                row.createCell(8).setCellValue(user.getExperience());
//                row.createCell(9).setCellValue(user.getCurrentProjectDetails());
                // ... Add other fields
            }

            workbook.write(out);
            workbook.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}