package com.userfront.controller;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.userfront.domain.Appointment;
import com.userfront.domain.PrimaryTransaction;
import com.userfront.domain.User;
import com.userfront.service.AppointmentService;
import com.userfront.service.UserService;

@Controller
@RequestMapping("/appointment")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
	private JavaMailSender emailSender;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/create",method = RequestMethod.GET)
    public String createAppointment(Model model) {
        Appointment appointment = new Appointment();
        model.addAttribute("appointment", appointment);
        model.addAttribute("dateString", "");

        return "appointment";
    }

    @RequestMapping(value = "/create",method = RequestMethod.POST)
    public String createAppointmentPost(@ModelAttribute("appointment") Appointment appointment, @ModelAttribute("dateString") String date, Model model, Principal principal) throws ParseException {

        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Date d1 = format1.parse( date );
        appointment.setDate(d1);

        User user = userService.findByUsername(principal.getName());
        appointment.setUser(user);

        appointmentService.createAppointment(appointment);
        try {
            sendEmailNotification(appointment,user);
            model.addAttribute("updateSuccessful", true);
        } catch (MessagingException e) {
            // Handle email sending failure
        	model.addAttribute("emailError", true);
        }
        

        return "redirect:/userFront";
    }
    
    @RequestMapping(value = "/appointmentDetails",method = RequestMethod.GET)
    public String getAppointment(Model model,Principal principal) {
        Appointment appointment = new Appointment();
        User user = userService.findByUsername(principal.getName());
        appointment.setUser(user);
        List<Appointment> apptdetailsList = appointmentService.findByUserId(user.getUserId());
        
        model.addAttribute("apptdetailsList", apptdetailsList);
        //model.addAttribute("dateString", "");

        return "appointmentDetails";
    }

    private void sendEmailNotification(Appointment appointment, User user) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        //helper.setFrom("ay1996465@gmail.com");
        helper.setTo(appointment.getEmail());
        helper.setSubject("Appointment Communication Notification");

        // Prepare the HTML content using Thymeleaf template
        String htmlContent = "<p><strong>Dear Customer  </strong>, </p>"
        		          +  "<p><strong> Appointment ID :: </strong> " + appointment.getId() + "</p>"
        		           + "<p><strong>Customer Name:</strong> " + user.getName() + "</p>"
                            + "<p><strong>Email:</strong> " + appointment.getEmail() + "</p>"
                            + "<p><strong>for Location Visit:</strong> " + appointment.getLocation() + "</p>"
                            + "<p><strong>for Discription:</strong><br>" + appointment.getDescription() + "</p>"
                           + "<p><strong>Confirmation Status:</strong><br>" + appointment.isConfirmed() + "</p>";

        helper.setText(htmlContent, true);

        emailSender.send(message);
    }


}
