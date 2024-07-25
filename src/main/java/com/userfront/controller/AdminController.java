//AdminController.java
package com.userfront.controller;

import com.userfront.dao.RoleDao;
import com.userfront.domain.User;
import com.userfront.domain.security.UserRole;
import com.userfront.service.UserService;
//import com.userfront.service.UserServiceImpl.ExcelExportService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

 @Autowired
 private UserService userService;
 
 @Autowired
 private RoleDao roleDao;
 
// @Autowired
// private ExcelExportService ExcelExportService;

 @GetMapping("/users")
 public String getAllUsers(Model model) {
     model.addAttribute("users", userService.findAllUsers());
     return "users";
 }

 @GetMapping("/users/edit/{id}")
 public String editUser(@PathVariable Long id, Model model) {
     User user = userService.findById(id);
     model.addAttribute("user", user);
     return "edit_user";
 }

 @PostMapping("/users/update")
 public String updateUser(@ModelAttribute("user") User user) {
     userService.update(user);
     return "redirect:/admin/users";
 }

 @GetMapping("/users/delete/{id}")
 public String deleteUser(@PathVariable Long id,Model model) {
     userService.delete(id);
     model.addAttribute("users", userService.findAllUsers());
     return "redirect:/admin/users";
 }
 
 @RequestMapping(value = "/signup", method = RequestMethod.GET)
 public String signup(Model model) {
     User user = new User();

     model.addAttribute("user", user);

     return "admin_signup";
 }
	
	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public String signupPost(@ModelAttribute("user") User user, Model model) {
         String returnvalue =null;
		if (userService.checkUserExists(user.getUsername(), user.getEmail())) {

			if (userService.checkEmailExists(user.getEmail())) {
				model.addAttribute("emailExists", true);
			}

			if (userService.checkUsernameExists(user.getUsername())) {
				model.addAttribute("usernameExists", true);
			}
			returnvalue="admin_signup";
            //return "admin_signup";
		}

		else {
			Set<UserRole> userRoles = new HashSet<>();
			userRoles.add(new UserRole(user, roleDao.findByName("ROLE_USER")));

			userService.createUser(user, userRoles);
			returnvalue="redirect:/admin/users";
			//return "redirect:/admin/users";
		}
		return returnvalue;
	}
	
	@GetMapping("/export/excel")
	public ResponseEntity<InputStreamResource> exportToExcel() {
		
		List<User> user = userService.findAllUsers();
		ByteArrayInputStream bis = exportToExcel(user);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", "inline; filename=employees_details.xlsx");

		return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new InputStreamResource(bis));
	}
	
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

