package com.userfront.dao;

import com.userfront.domain.CardlessTransaction;
import com.userfront.domain.PrimaryAccount;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface PrimaryAccountDao extends CrudRepository<PrimaryAccount,Long> {

    PrimaryAccount findByAccountNumber (int accountNumber);

	void save(CardlessTransaction cardless);
	
	@Query(value="select * from cardless_transaction where otp = :randomOTP ",nativeQuery=true)
	Optional<CardlessTransaction> findByOtp(@Param(value="randomOTP")String randomOTP);

	@Query(value="select * from cardless_transaction where primary_account = :accountnumber and type =:accountType  and amount =:amount ",nativeQuery=true)
	Optional<CardlessTransaction> findByprimaryNumber(@Param(value="accountnumber")String accountnumber,@Param(value="accountType")String AccountType,@Param(value="amount")String amount);

	@Query(value="select * from cardless_transaction where secondary_account = :accountnumber and type =:accountType  and amount =:amount ",nativeQuery=true)
	Optional<CardlessTransaction> findBysavingNumber(@Param(value="accountnumber")String accountnumber,@Param(value="accountType")String AccountType,@Param(value="amount")String amount);

//	@Query(value="select * from cardless_transaction where id = :tid", nativeQuery=true)
//	Optional<CardlessTransaction> findById(@Param(value="tid") Long tid);

}
