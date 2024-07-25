package com.userfront.controller;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.userfront.domain.Appointment;
import com.userfront.domain.CardlessTransaction;
import com.userfront.domain.PrimaryAccount;
import com.userfront.domain.PrimaryTransaction;
import com.userfront.domain.SavingsAccount;
import com.userfront.domain.SavingsTransaction;
import com.userfront.domain.User;
import com.userfront.service.AccountService;
import com.userfront.service.TransactionService;
import com.userfront.service.UserService;
import com.userfront.dao.CardLessTransactionDao;
import com.userfront.dao.PrimaryAccountDao;

@Controller
@RequestMapping("/account")
public class AccountController {
	
	@Autowired
    private UserService userService;
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private JavaMailSender emailSender;
	
	@Autowired
	private PrimaryAccountDao PrimaryAccountDao;
	
	@Autowired
	private CardLessTransactionDao CardLessTransactionDao;
	
	@RequestMapping("/primaryAccount")
	public String primaryAccount(Model model, Principal principal) {
		List<PrimaryTransaction> primaryTransactionList = transactionService.findPrimaryTransactionList(principal.getName());
		
		User user = userService.findByUsername(principal.getName());
        PrimaryAccount primaryAccount = user.getPrimaryAccount();

        model.addAttribute("primaryAccount", primaryAccount);
        model.addAttribute("primaryTransactionList", primaryTransactionList);
		
		return "primaryAccount";
	}

	@RequestMapping("/savingsAccount")
    public String savingsAccount(Model model, Principal principal) {
		List<SavingsTransaction> savingsTransactionList = transactionService.findSavingsTransactionList(principal.getName());
        User user = userService.findByUsername(principal.getName());
        SavingsAccount savingsAccount = user.getSavingsAccount();

        model.addAttribute("savingsAccount", savingsAccount);
        model.addAttribute("savingsTransactionList", savingsTransactionList);

        return "savingsAccount";
    }
	
	@RequestMapping(value = "/deposit", method = RequestMethod.GET)
    public String deposit(Model model) {
        model.addAttribute("accountType", "");
        model.addAttribute("amount", "");

        return "deposit";
    }

    @RequestMapping(value = "/deposit", method = RequestMethod.POST)
    public String depositPOST(@ModelAttribute("amount") String amount, @ModelAttribute("accountType") String accountType, Principal principal) {
        accountService.deposit(accountType, Double.parseDouble(amount), principal);

        return "redirect:/userFront";
    }
    
    @RequestMapping(value = "/withdraw", method = RequestMethod.GET)
    public String withdraw(Model model) {
        model.addAttribute("accountType", "");
        model.addAttribute("amount", "");

        return "withdraw";
    }

    @RequestMapping(value = "/withdraw", method = RequestMethod.POST)
    public String withdrawPOST(@ModelAttribute("amount") String amount, @ModelAttribute("accountType") String accountType, Principal principal) {
        accountService.withdraw(accountType, Double.parseDouble(amount), principal);

        return "redirect:/userFront";
    }
    
    @RequestMapping(value = "/cardlesswithdraw", method = RequestMethod.GET)
    public String cardless(Principal principal, Model model) {
		if (principal.getName().equalsIgnoreCase("admin@security.com"))
		{
			User user = userService.findByUsername(principal.getName());
			
			model.addAttribute("users", userService.findAllUsers());
		     return "users";
		}
		else {
        User user = userService.findByUsername(principal.getName());
        PrimaryAccount primaryAccount = user.getPrimaryAccount();
        SavingsAccount savingsAccount = user.getSavingsAccount();

        model.addAttribute("primaryAccount", primaryAccount);
        model.addAttribute("savingsAccount", savingsAccount);

        return "cardless";
		}
    }
    
    @RequestMapping(value = "/cardlessbetween", method = RequestMethod.GET)
    public String cardlessbetween(Principal principal, Model model) {
		if (principal.getName().equalsIgnoreCase("admin@security.com"))
		{
			User user = userService.findByUsername(principal.getName());
			
			model.addAttribute("users", userService.findAllUsers());
		     return "users";
		}
		else {
        User user = userService.findByUsername(principal.getName());
        PrimaryAccount primaryAccount = user.getPrimaryAccount();
        SavingsAccount savingsAccount = user.getSavingsAccount();

        model.addAttribute("primaryAccount", primaryAccount);
        model.addAttribute("savingsAccount", savingsAccount);
        model.addAttribute("transferFrom", "");
//        model.addAttribute("transferTo", "");
        model.addAttribute("amount", "");

        return "cardlessbetweenaccounts";
		}
    }
   

    @RequestMapping(value = "/betweenAccounts", method = RequestMethod.POST)
    public String betweenAccountsPost(
            @ModelAttribute("transferFrom") String transferFrom,
            @ModelAttribute("amount") String amount,
            Principal principal
    ) throws Exception {
        User user = userService.findByUsername(principal.getName());
        PrimaryAccount primaryAccount = user.getPrimaryAccount();
        SavingsAccount savingsAccount = user.getSavingsAccount();
        
        int randomOTP=generateSixDigitInt();
    		
        CardlessTransaction cardless =new CardlessTransaction();
        
        insertIntoCardlessTransaction(cardless,transferFrom,randomOTP,Long.parseLong(amount),primaryAccount.getAccountNumber(),savingsAccount.getAccountNumber());
        sendEmailNotification(user,randomOTP);
 
//        if(cardless.getKYC()) {
//        transactionService.cardlessbetweenAccountsTransfer(transferFrom, amount, primaryAccount, savingsAccount);
//        }

        return "redirect:/account/cardlesswithdraw";
    }
    
    private void insertIntoCardlessTransaction(CardlessTransaction cardless,String transferFrom,int randomOTP,Long amount,int primaryAccount,int secondaryAccount) {
    	
    	cardless.setDate(new Date());
    	cardless.setDescription("cardless withdraw from yours "+transferFrom+" Account");
		cardless.setType(transferFrom);
		cardless.setOtp(randomOTP);
		cardless.setAmount(amount);
		cardless.setPrimaryAccount(primaryAccount);
		cardless.setSecondaryAccount(secondaryAccount);
		cardless.setKYC(false);
    	
		PrimaryAccountDao.save(cardless);
	}

	public int generateSixDigitInt() {
        Random random = new Random();
        return random.nextInt(900000) + 100000;
    }
    
    private void sendEmailNotification( User user,int randomOTP) throws MessagingException {
    	Optional<CardlessTransaction> result1 = CardLessTransactionDao.findByOtp(randomOTP);
    	Long transactionId =null;
		if (result1.isPresent()) {
			transactionId = result1.get().getId();
			System.out.println("Transaction ID for Cardless:::"+ transactionId);
		}
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        //helper.setFrom("ay1996465@gmail.com");
        helper.setTo(user.getEmail());
        helper.setSubject("Card-Less OTP Notification For WithDraw");

        // Prepare the HTML content using Thymeleaf template
     // Prepare the HTML content using Thymeleaf template
        String htmlContent = "<html lang=\"en\">" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<style>" +
                "    body { font-family: Arial, sans-serif; color: #333; }" +
                "    .container { margin: 20px; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9; }" +
                "    .highlight { color: #d9534f; font-weight: bold; }" +
                "    p { margin: 10px 0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<p><strong>Dear Customer,</strong></p>" +
                "<p><strong>CardLess Transaction ID:</strong> " + transactionId + "</p>" +
                "<p><strong>User ID:</strong> " + user.getUserId() + "</p>" +
                "<p><strong>Customer Name:</strong> " + user.getName() + "</p>" +
                "<p><strong>Email:</strong> " + user.getEmail() + "</p>" +
                "<p><strong>OTP For CardLess Transaction:</strong> " + randomOTP + "</p>" +
                "<p class=\"highlight\"><strong>OTP Validity:</strong> OTP will expire within 10 minutes.</p>" +
                "<p><strong>Thanks, Customer,</strong></p>" +
                "</div>" +
                "</body>" +
                "</html>";


        helper.setText(htmlContent, true);

        emailSender.send(message);
    }
    
    @RequestMapping(value = "/ATMUI", method = RequestMethod.GET)
    public String atmInterface() throws Exception {
//        User user = userService.findByUsername(principal.getName());
//        PrimaryAccount primaryAccount = user.getPrimaryAccount();
//        SavingsAccount savingsAccount = user.getSavingsAccount();
//        
//        int randomOTP=generateSixDigitInt();
//    		
//        CardlessTransaction cardless =new CardlessTransaction();
//        
//        insertIntoCardlessTransaction(cardless,transferFrom,randomOTP,Long.parseLong(amount));
//        sendEmailNotification(user,randomOTP);
 
//        if(cardless.getKYC()) {
//        transactionService.cardlessbetweenAccountsTransfer(transferFrom, amount, primaryAccount, savingsAccount);
//        }
    	String returnPage=null;
     Iterable<CardlessTransaction> cardlessOptional = CardLessTransactionDao.findAll();
        
     if (!cardlessOptional.iterator().hasNext()) {
        	returnPage ="errorPage";
        }else {
        	returnPage ="atmUI";
        }

        return returnPage;
    }
    
    @RequestMapping(value = "/ATMUI", method = RequestMethod.POST)
    public String atmValidateOTP(
            @ModelAttribute("tid") Long tid,
            @ModelAttribute("transferFrom") String transferFrom,
            @ModelAttribute("accountnumber") String accountnumber,
            @ModelAttribute("otp1") String otp,
            @ModelAttribute("amount") String amount,
            Principal principal,
            Model model
    ) throws Exception {
        User user = userService.findByUsername(principal.getName());
        PrimaryAccount primaryAccount = user.getPrimaryAccount();
        SavingsAccount savingsAccount = user.getSavingsAccount();
        String returnpage=null;
        CardlessTransaction cardless=null;
        Optional<CardlessTransaction> cardlessOptional = CardLessTransactionDao.findById(tid);
        
        if(cardlessOptional.isPresent()) {
            cardless = cardlessOptional.get();
            cardless.setKYC(true);
            CardLessTransactionDao.save(cardless);
        }
        
        if (accountnumber==null || accountnumber=="") {
            model.addAttribute("accountnumber", true);
        }else{
        	model.addAttribute("accountnumber", false);
        }
        if (transferFrom==null || transferFrom=="") {
            model.addAttribute("transferFrom", true);
        }
        else{
        	model.addAttribute("transferFrom", false);
        }
        if (tid==null || tid == 0) {
            model.addAttribute("tid", true);
        }
        else{
        	model.addAttribute("tid", false);
        }
        if (otp==null || otp == "") {
            model.addAttribute("otp", true);
        }else{
        	 model.addAttribute("otp", false);
        }
        if (amount== null || amount == "") {
            model.addAttribute("amount", true);
        }else{
        	model.addAttribute("amount", false);
        }
        
		if ((accountnumber != null && accountnumber != "") && (transferFrom != null && transferFrom != "")
				&& (amount != null && amount != "") && (otp != null && otp != "") && (tid != null && tid != 0)) {
			if (checkAccountValidation(accountnumber, transferFrom, amount)) {
				model.addAttribute("accountCheck", true);
			} else {
				model.addAttribute("accountCheck1", true);
			}
			if (checkOtp(otp)) {
				model.addAttribute("otpcheck", true);
			} else {
				model.addAttribute("otpcheck1", true);
			}
		}
		if ((accountnumber != null && accountnumber != "") && (transferFrom != null && transferFrom != "")
				&& (amount != null && amount != "") && (otp != null && otp != "") && (tid != null && tid != 0)) {
			if (checkAccountValidation(accountnumber, transferFrom, amount) && checkOtp(otp) && (cardless.getKYC())) {
				transactionService.cardlessbetweenAccountsTransfer(transferFrom, amount, primaryAccount,
						savingsAccount);
				CardLessTransactionDao.deleteById(tid);
				model.addAttribute("TransactionStatus", true);
				returnpage= "redirect:/account/cardlesswithdraw";
			} else {
				model.addAttribute("TransactionStatus1", true);
				returnpage= "atmUI";
			}
		}

        return returnpage;
    }

    public boolean checkAccountValidation(String accountnumber, String transferFrom, String amount) {
        Optional<CardlessTransaction> result;
        if (transferFrom.equalsIgnoreCase("Primary")) {
            result = CardLessTransactionDao.findByprimaryNumber(accountnumber, transferFrom, amount);
        } else {
            result = CardLessTransactionDao.findBysavingNumber(accountnumber, transferFrom, amount);
        }
        return result.isPresent();
    }

    public boolean checkOtp(String otp) {
        Optional<CardlessTransaction> result1 = CardLessTransactionDao.findByOtp(otp);
        return result1.isPresent();
    }
    
//    @Scheduled(fixedRate = 300000) // 300000 milliseconds = 5 minutes
//    public void deleteAllCardlessTransactions() {
//    	CardLessTransactionDao.deleteAll();
//    }
//    
// // This method will run every 2 minutes
//    @Scheduled(fixedRate = 120000) // 120000 milliseconds = 2 minutes
//    public void deleteAllCardlessTransactions1() {
//    	CardLessTransactionDao.deleteAll();
//    }
    
   
    
//private String insertKYCIntoCardlessTransaction(CardlessTransaction cardless,String transferFrom,int randomOTP,Long amount) {
//    	
//	    boolean validate =PrimaryAccountDao.findByOtp(randomOTP);
//		cardless.setKYC(true);
//		PrimaryAccountDao.save(cardless);
//		
//		if(validate) {
//			if(cardless.getKYC()) {
//	        transactionService.cardlessbetweenAccountsTransfer(transferFrom, amount, primaryAccount, savingsAccount);
//	        }
//			return "OTP Succesfully Validated!!!";
//		}
//	}
    
    
}
