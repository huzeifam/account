package com.example.account;

import com.example.account.model.AccountResponse;
import com.example.account.repository.AccountRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@SpringBootApplication
public class AccountApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = SpringApplication.run(AccountApplication.class, args);

		AccountRepository accountRepository = applicationContext.getBean(AccountRepository.class);

		AccountResponse account1 = new AccountResponse();
		account1.setAccountNo(192837465);
		account1.setAccountType("Girokonto");
		account1.setFirstName("Max");
		account1.setLastName("Mustermann");
		account1.setCustomerNo(987654321);
		account1.setIban("DE50 7382 8438 8383 7438 92");
		account1.setBalanceInEuro(1496.52);
		account1.setStartDate(LocalDate.parse("2005-05-12"));
		account1.setReferenceAccount(null);

		AccountResponse account2 = new AccountResponse();
		account2.setAccountNo(918273645);
		account2.setAccountType("Tagesgeldkonto");
		account2.setFirstName("Max");
		account2.setLastName("Mustermann");
		account2.setCustomerNo(987654321);
		account2.setIban("DE20 8372 6471 8373 0293 11");
		account2.setBalanceInEuro(4530.86);
		account2.setStartDate(LocalDate.parse("2010-01-22"));
		account2.setReferenceAccount(192837465);

		AccountResponse account3 = new AccountResponse();
		account3.setAccountNo(873792213);
		account3.setAccountType("Girokonto");
		account3.setFirstName("Erika");
		account3.setLastName("Mustermann");
		account3.setCustomerNo(123456789);
		account3.setIban("DE20 7483 8392 8372 1114 99");
		account3.setBalanceInEuro(804.19);
		account3.setStartDate(LocalDate.parse("2013-10-02"));
		account3.setReferenceAccount(null);

		AccountResponse account4 = new AccountResponse();
		account4.setAccountNo(362261902);
		account4.setAccountType("Schülerkonto");
		account4.setFirstName("Maria");
		account4.setLastName("Mustermann");
		account4.setCustomerNo(743928374);
		account4.setIban("DE50 8372 2404 0402 8584 11");
		account4.setBalanceInEuro(124.56);
		account4.setStartDate(LocalDate.parse("2021-11-27"));
		account4.setReferenceAccount(null);

		accountRepository.save(account1);
		accountRepository.save(account2);
		accountRepository.save(account3);
		accountRepository.save(account4);
	}


	@Bean
	public RestTemplate restTemplate(){
		return new RestTemplate();
	}
}
