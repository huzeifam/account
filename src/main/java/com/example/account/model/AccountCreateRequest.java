package com.example.account.model;


import java.time.LocalDateTime;

public class AccountCreateRequest {

    private Integer customerNo;
    private Integer referenceAccount;


    public AccountCreateRequest(Integer customerNo, String iban,
                                Double balanceInEuro, LocalDateTime startDate, Integer referenceAccount) {
        this.customerNo = customerNo;
        this.referenceAccount = referenceAccount;

    }
    public Integer getCustomerNo() { return customerNo; }

    public Integer getReferenceAccount() {
        return referenceAccount;
    }
}
