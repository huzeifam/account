package com.example.account.model;

import org.iban4j.Iban;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class AccountResponse {

    @Id
    private Integer accountNo;
    private Integer customerNo;
    private String iban;
    private Double balanceInEuro;
    private LocalDateTime startDate;





    public AccountResponse(){

    }

    public AccountResponse(Integer customerNo, Integer accountNo, String iban,
                           Double balanceInEuro, LocalDateTime startDate) {
        this.customerNo = customerNo;
        this.accountNo = accountNo;
        this.iban = iban;
        this.balanceInEuro = balanceInEuro;
        this.startDate = startDate;
    }

    public Integer getCustomerNo() {return customerNo; }
    public Integer getAccountNo() { return accountNo; }
    public String getIban() { return iban; }
    public Double getBalanceInEuro() { return Math.round(balanceInEuro*100.0)/100.0; }
    public LocalDateTime getStartDate() { return startDate; }

}
