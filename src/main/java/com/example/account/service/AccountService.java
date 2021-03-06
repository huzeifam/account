package com.example.account.service;

import com.example.account.model.AccountResponse;
import com.example.account.model.Transactions;
import com.example.account.repository.AccountRepository;
//import com.example.banking.model.CustomerResponse;
//import com.example.banking.repository.CustomerRepository;
import com.example.account.repository.TransactionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    //    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionsRepository transactionsRepository;

    public AccountService(/*CustomerRepository customerRepository,*/ AccountRepository accountRepository, TransactionsRepository transactionsRepository) {
//        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionsRepository = transactionsRepository;
    }

    @Autowired
    RestTemplate restTemplate;


    public List<AccountResponse> findAllAccounts() {
        return accountRepository.findAll();
    }

    public Optional<AccountResponse> findByAccountNo(Integer accountNo) {

        return accountRepository.findById(accountNo);
    }

    public List<AccountResponse> findAccountByCustomerNo(Integer customerNo) {

        return accountRepository.findAccountByCustomerNo(customerNo);
    }


    public AccountResponse createAccount(AccountResponse accountNo) {
//        Optional<CustomerResponse> customer = customerRepository.findById(accountNo.getCustomerNo());

     boolean customers = (restTemplate.getForObject("http://localhost:8080/api/customers/numbers", List.class).contains(accountNo.getCustomerNo()));
//       boolean customers = (restTemplate.getForObject("http://customer:8080/api/customers/numbers", List.class).contains(accountNo.getCustomerNo()));


        if (customers == false) {

            return null;
        } else {
            return accountRepository.save(accountNo);
        }

    }


    public Void deleteAccountByCustomerNo(Integer customerNo) {

        accountRepository.deleteAccountByCustomerNo(customerNo);

        return null;
    }

    public void deleteByAccountNo(Integer accountNo) {

        accountRepository.deleteById(accountNo);
    }

    public double getBalanceInEuro(Integer accountNo) {
        return accountRepository.findBalanceByAccountNo(accountNo);
    }


    public void depositAmount(Integer accountNo, Double amount) {
        accountRepository.saveBalanceByAccountNo(accountNo, amount);
    }

    public void withdrawAmount(Integer accountNo, Double amount) {
        accountRepository.withdrawAmountByAccountNo(accountNo, amount);
    }


    public void transferAmount(Integer accountNo, Integer destAccountNo, Double amount) {
        accountRepository.withdrawAmountByAccountNo(accountNo, amount);
        accountRepository.saveBalanceByAccountNo(destAccountNo, amount);

    }

    public List<Integer> getAccountNo() {
        return accountRepository.findAllAccountNo();
    }

    public List<Integer> getAccountNoOfCustomerAccounts(Integer customerNo) {
        return accountRepository.getAccountNoOfCustomerAccounts(customerNo);

    }

    public List<Transactions> findTransactionsByAccountNo(Integer accountNo) {
        return transactionsRepository.findTransactionsByAccountNo(accountNo);
    }

    public Transactions save (Transactions transactions) {

        return transactionsRepository.save(transactions);
    }
    public List<Double> getBalanceOfCustomerAccounts(Integer customerNo) {
        return accountRepository.getBalanceOfCustomerAccounts(customerNo);}

    public String getAccountType(Integer accountNo) {
        return accountRepository.findAccountType(accountNo);
    }
}
