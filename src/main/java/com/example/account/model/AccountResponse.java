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

    private Integer referenceAccount;





    public AccountResponse(){

    }

    public AccountResponse(Integer customerNo, String accountType, String firstName, String lastName, Integer accountNo, String iban,
                           Double balanceInEuro, LocalDate startDate, Integer referenceAccount) {
        this.customerNo = customerNo;
        this.accountType = accountType;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountNo = accountNo;
        this.iban = iban;
        this.balanceInEuro = balanceInEuro;
        this.startDate = startDate;
        this.referenceAccount = referenceAccount;
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

    public Integer getReferenceAccount() {
        return referenceAccount;
    }


    public void setAccountNo(Integer accountNo) {
        this.accountNo = accountNo;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setCustomerNo(Integer customerNo) {
        this.customerNo = customerNo;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public void setBalanceInEuro(Double balanceInEuro) {
        this.balanceInEuro = balanceInEuro;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setReferenceAccount(Integer referenceAccount) {
        this.referenceAccount = referenceAccount;
    }
}
