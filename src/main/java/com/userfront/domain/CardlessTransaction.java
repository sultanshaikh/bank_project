package com.userfront.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class CardlessTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	private Date date;
    private String description;
    private String type;
    private Long amount;
    private Boolean KYC;
    private int otp;
    private int primaryAccount;
    private int SecondaryAccount;
    
    
	public int getPrimaryAccount() {
		return primaryAccount;
	}
	public void setPrimaryAccount(int primaryAccount) {
		this.primaryAccount = primaryAccount;
	}
	public int getSecondaryAccount() {
		return SecondaryAccount;
	}
	public void setSecondaryAccount(int secondaryAccount) {
		SecondaryAccount = secondaryAccount;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Boolean getKYC() {
		return KYC;
	}
	public void setKYC(Boolean kYC) {
		KYC = kYC;
	}
	 public int getOtp() {
			return otp;
		}
		public void setOtp(int otp) {
			this.otp = otp;
		}
		public Long getAmount() {
			return amount;
		}
		public void setAmount(Long amount) {
			this.amount = amount;
		}



}
