package com.example.account.model;


import java.time.LocalDateTime;

public class AccountCreateRequest {

    private Integer customerNo;


    public AccountCreateRequest(Integer customerNo, String iban,
                                Double balanceInEuro, LocalDateTime startDate) {
        this.customerNo = customerNo;

    }
    public Integer getCustomerNo() { return customerNo; }


}
