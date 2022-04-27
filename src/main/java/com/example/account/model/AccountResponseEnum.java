package com.example.account.model;

public enum AccountResponseEnum {
    Girokonto("Girokonto"),
    Schülerkonto("Schülerkonto"),
    Tagesgeldkonto("Tagesgeldkonto");


    private String accountType;

    AccountResponseEnum(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountType() {
        return accountType;
    }
}
