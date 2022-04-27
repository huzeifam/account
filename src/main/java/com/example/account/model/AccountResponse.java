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
    private String accountType;
    private String firstName;
    private String lastName;
    private Integer customerNo;
    private String iban;
    private Double balanceInEuro;
    private LocalDate startDate;





    public AccountResponse(){

    }

    public AccountResponse(Integer customerNo, String accountType, String firstName, String lastName, Integer accountNo, String iban,
                           Double balanceInEuro, LocalDate startDate) {
        this.customerNo = customerNo;
        this.accountType = accountType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountNo = accountNo;
        this.iban = iban;
        this.balanceInEuro = balanceInEuro;
        this.startDate = startDate;
    }

    public Integer getCustomerNo() {return customerNo; }

    public String getAccountType() {
        return accountType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Integer getAccountNo() { return accountNo; }
    public String getIban() { return iban; }
    public Double getBalanceInEuro() { return Math.round(balanceInEuro*100.0)/100.0; }
    public LocalDate getStartDate() { return startDate; }

}
