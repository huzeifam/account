package com.example.account.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transactions {

    @Id
    @GeneratedValue
    private Integer transactionId;
    private Integer accountNo;
    private Integer customerNo;
    private String firstName;
    private String lastName;
    private String type;
    private Double amountInEuro;
    private Double previousBalanceInEuro;
    private Double finalBalanceInEuro;
    private String purposeOfTransfer;
    private LocalDate bookingDate;


    public Transactions(){

    }
    public Transactions(Integer transactionId, Integer accountNo,Integer customerNo, String firstName, String lastName,  String type, Double amountInEuro, Double previousBalanceInEuro, Double finalBalanceInEuro,String purposeOfTransfer, LocalDate bookingDate) {
        this.transactionId = transactionId;
        this.accountNo = accountNo;
        this.customerNo = customerNo;
        this.firstName = firstName;
        this.lastName = lastName;
        this.type = type;
        this.amountInEuro = amountInEuro;
        this.previousBalanceInEuro = previousBalanceInEuro;
        this.finalBalanceInEuro = finalBalanceInEuro;
        this.purposeOfTransfer = purposeOfTransfer;
        this.bookingDate = bookingDate;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public Integer getAccountNo() {
        return accountNo;
    }

    public Integer getCustomerNo() {
        return customerNo;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getType() {
        return type;
    }

    public Double getAmountInEuro() {
        return amountInEuro;
    }

    public Double getPreviousBalanceInEuro() {
        return previousBalanceInEuro;
    }

    public Double getFinalBalanceInEuro() {
        return finalBalanceInEuro;
    }

    public String getPurposeOfTransfer() {
        return purposeOfTransfer;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }
}
