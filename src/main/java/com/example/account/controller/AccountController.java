package com.example.account.controller;

import com.example.account.model.AccountCreateRequest;
import com.example.account.model.AccountResponse;
import com.example.account.service.AccountService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
public class AccountController {

    private final AccountService accountService;


    public AccountController(AccountService accountService) {
        this.accountService = accountService;

    }

    @Autowired
    RestTemplate restTemplate;

    @Operation(summary = "Get all accounts")
    @GetMapping("/accounts")
    public List<AccountResponse> getAllAccounts() {

        return accountService.findAllAccounts();
    }

    @Operation(summary = "Find account by account number")
    @GetMapping("/accounts/{accountNo}")
    public ResponseEntity<Object> getAccountByAccountNo(
            @Parameter(description = "account number of account to be found")
            @PathVariable Integer accountNo
    ) {
        Optional<AccountResponse> account = accountService.findByAccountNo(accountNo);
        if (account.isPresent())
            return ResponseEntity.ok(account.get());
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account with account number " + accountNo + " not found.");
    }

    @Hidden
    @GetMapping("/accounts/numbers")
    public List<Integer> getAllAccountNo(){
        return accountService.getAccountNo();
    }

    @Operation(summary = "Get all accounts of a customer")
    @GetMapping("/accounts/customer-accounts/{customerNo}")
    public Object[] getCustomerAccountByCustomerNo(
            @Parameter(description = "Customer number of customer to find all accounts")
            @PathVariable Integer customerNo
    ) {
        String empty = "Either customer with customer number " + customerNo + " does not exist or the customer has no accounts.";
        List<AccountResponse> account = accountService.findAccountByCustomerNo(customerNo);
        if (account.isEmpty())
            return new String[]{empty};
        else
            return accountService.findAccountByCustomerNo(customerNo).toArray();

    }

    @Hidden
    @GetMapping("/accounts/{accountNo}/balance")
    public ResponseEntity<Double> getBalanceInEuro(
            @PathVariable Integer accountNo
    ) {
        Optional<AccountResponse> account = accountService.findByAccountNo(accountNo);
        if (account.isPresent())
            return ResponseEntity.status(HttpStatus.OK).body(accountService.getBalanceInEuro(accountNo));
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }




    @Operation(summary = "Deposit an amount into an account")
    @PutMapping("/accounts/{accountNo}/deposit/{amount}")
    public ResponseEntity<String> depositAmount(
            @Parameter(description = "Account number of account")
            @PathVariable Integer accountNo,
            @Parameter(description = "Amount for deposit")
            @PathVariable Double amount
    ) {
        Optional<AccountResponse> account = accountService.findByAccountNo(accountNo);
        if (account.isPresent()) {
            accountService.depositAmount(accountNo, amount);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Deposit success.\n\n" +
                    "Previous balance: " + Math.round(account.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n" +
                    "Amount: " + Math.round(amount*100.0)/100.0 + "€ \n" +
                    "Current balance: " + Math.round((account.get().getBalanceInEuro()+amount) * 100.0) / 100.0 + "€");
        } else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Deposit amount failed. Account with account number " + accountNo + " does not exist.");

    }

    @Operation(summary = "Withdraw an amount from an account")
    @PutMapping("/accounts/{accountNo}/withdraw/{amount}")
    public ResponseEntity<String> withdrawAmount(
            @Parameter(description = "Account number of account")
            @PathVariable Integer accountNo,
            @Parameter(description = "Amount for withdrawal")
            @PathVariable Double amount
    ) {
        Optional<AccountResponse> account = accountService.findByAccountNo(accountNo);
        if (account.isPresent()) {
            if (account.get().getBalanceInEuro() - amount >= 0) {

                accountService.withdrawAmount(accountNo, amount);
                return ResponseEntity.status(HttpStatus.ACCEPTED).body("Withdraw success.\n\n" +
                        "Previous balance: " + Math.round((account.get().getBalanceInEuro()) * 100.0) / 100.0 + "€\n" +
                        "Amount: " + Math.round(amount*100.0)/100.0 + "€ \n" +
                        "Current balance: " + Math.round((account.get().getBalanceInEuro()-amount) * 100.0) / 100.0 + "€");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Could not withdraw.\n\n" +
                        "Requested amount: " + Math.round(amount*100.0)/100.0 + "€\n" +
                        "Current balance: " + Math.round(account.get().getBalanceInEuro() * 100.0) / 100.0 + "€ \n\n" +
                        "Amount is bigger than current balance.");
            }
        } else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Withdraw amount failed. Account with account number " + accountNo + " does not exist.");
    }

    @Operation(summary = "Transfer amount from one account to another account")
    @PutMapping("/accounts/{accountNo}/transfer/{destAccountNo}/{amount}")
    public ResponseEntity<String> transferAmount(
            @Parameter(description = "Account number of transferring account")
            @PathVariable Integer accountNo,
            @Parameter(description = "Account number of receiving account")
            @PathVariable Integer destAccountNo,
            @Parameter(description = "Amount for bank transfer")
            @PathVariable Double amount
    ) {
        Optional<AccountResponse> account1 = accountService.findByAccountNo(accountNo);
        Optional<AccountResponse> account2 = accountService.findByAccountNo(destAccountNo);

        if (account1.isPresent()) {
            if (account2.isPresent()) {
                if (account1.get().getAccountNo() != account2.get().getAccountNo()) {
                    if (account1.get().getBalanceInEuro() - amount >= 0) {
                        accountService.transferAmount(accountNo, destAccountNo, amount);
                        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Transfer success. Transferred amount: " + Math.round(amount*100.0)/100.0 + "€\n\n" +
                                "Transferring account with account number \"" + accountNo + "\":\n" +
                                "Previous balance: " + Math.round(account1.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n" +
                                "Current balance: " + Math.round((account1.get().getBalanceInEuro()-amount) * 100.0) / 100.0 + "€\n\n" +
                                "Receiving account with account number \"" + destAccountNo + "\":\n" +
                                "Previous balance: " + Math.round(account2.get().getBalanceInEuro()  * 100.0) / 100.0 + "€\n" +
                                "Current balance: " + Math.round((account2.get().getBalanceInEuro()+ amount) * 100.0) / 100.0 + "€");
                    } else
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("Transfer failed.\n" +
                                "Requested amount for transfer: " + Math.round(amount*100.0/100.0) + "€\n\n" +
                                "Current balance of transferring account with account number \"" + accountNo + "\": " + Math.round(account1.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n" +
                                "Current balance of receiving account with account number \"" + destAccountNo + "\": " + Math.round(account2.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n\n" +
                                "Amount is bigger than current balance of transferring account.");
                } else
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Transferring account and receiving account are the same!");

            } else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transfer failed. The receiving account with account number " + destAccountNo + " does not exist.");
        } else if (account2.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transfer failed. The transferring account with account number " + accountNo + " does not exist.");
        } else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transfer failed. Accounts with account numbers \"" + accountNo + "\" and \"" + destAccountNo + "\" don't exist.");


    }

    @Operation(summary = "Create an account")
    @PostMapping("/accounts")
    public AccountResponse createAccount(
            @Parameter(description = "Customer number of customer to allocate")
            @RequestBody AccountCreateRequest aRequest
    ) {
        Iban iban = new Iban.Builder()
                .countryCode(CountryCode.DE)
                .buildRandom();


        AccountResponse acct = new AccountResponse(
                aRequest.getCustomerNo(),
                UUID.randomUUID().hashCode() & Integer.MAX_VALUE,
                (iban.getCountryCode()+ iban.getCheckDigit()+iban.getBban()).replaceAll("(\\w\\w\\w\\w)(\\w\\w\\w\\w)(\\w\\w\\w\\w)(\\w\\w\\w\\w)(\\w\\w\\w\\w)(\\w\\w)","$1 $2 $3 $4 $5 $6"),
                0.0,
                LocalDate.now()
        );
        return accountService.createAccount(acct);

    }

    @Operation(summary = "Delete an account")
    @DeleteMapping("/accounts/{accountNo}")
    public ResponseEntity deleteAccount(
            @Parameter(description = "Account number of account to delete")
            @PathVariable Integer accountNo

    ) {
        Optional<AccountResponse> account = accountService.findByAccountNo(accountNo);

        if (account.isPresent()) {
            accountService.deleteByAccountNo(accountNo);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Account with account number " + accountNo + " deleted.");

        } else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Could not delete. Account with account number " + accountNo + " does not exist.");

    }
    @Operation(summary = "Delete all accounts of a customer")
    @DeleteMapping("/accounts/customer-accounts/{customerNo}")
    public Void deleteAccountsofCustomer(
            @Parameter(description = "Customer number of customer to delete all accounts")
            @PathVariable Integer customerNo
    ){
        List<AccountResponse> account = accountService.findAccountByCustomerNo(customerNo);

        
        if (account.isEmpty()){
            return null;
        }
        else {
            List<Integer> accountNo = accountService.getAccountNoOfCustomerAccounts(customerNo);
            for (int i = 0 ; i < accountNo.size(); i++) {
                restTemplate.delete("http://localhost:8090/api//credits/account-credits/{accountNo}", accountNo.get(i));
            }
            return accountService.deleteAccountByCustomerNo(customerNo);
        }
    }

}
