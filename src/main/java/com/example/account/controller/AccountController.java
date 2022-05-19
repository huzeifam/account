package com.example.account.controller;

import com.example.account.model.AccountCreateRequest;
import com.example.account.model.AccountResponse;
import com.example.account.model.AccountResponseEnum;
import com.example.account.model.Transactions;
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

    @Operation(summary = "View all transactions of account")
    @GetMapping("/transactions/{accountNo}")
    public Object[] getTransactionsOfAccount(
            @Parameter(description = "account number of account to view transactions")
            @PathVariable Integer accountNo
    ) {
        Optional<AccountResponse> account = accountService.findByAccountNo(accountNo);
        List<Transactions> transaction = accountService.findTransactionsByAccountNo(accountNo);
        String notFound = "Account (account number: " + accountNo + ") does not exist and no transactions found.";

        if (account.isPresent()) {
            if (!transaction.isEmpty()) {
                return accountService.findTransactionsByAccountNo(accountNo).toArray();
            } else {
                String nottrans = "Account (account number: " + accountNo + ", name: " + account.get().getFirstName() + " " + account.get().getLastName() + ") has no transactions.";
                return new String[]{nottrans};
            }
        } else if (!transaction.isEmpty()) {
            return accountService.findTransactionsByAccountNo(accountNo).toArray();
        } else {
            return new String[]{notFound};

            }
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account (account number: " + accountNo + ") not found.");
    }

    @Hidden
    @GetMapping("/accounts/numbers")
    public List<Integer> getAllAccountNo() {
        return accountService.getAccountNo();
    }

    @Hidden
    @Operation(summary = "Get all accounts of a customer")
    @GetMapping("/accounts/customer-accounts/{customerNo}")
    public Object[] getCustomerAccountByCustomerNo(
            @Parameter(description = "Customer number of customer to find all accounts")
            @PathVariable Integer customerNo
    ) {
        String empty = "Either customer with customer number " + customerNo + " does not exist or the customer has no accounts.";
        List<AccountResponse> account = accountService.findAccountByCustomerNo(customerNo);
        if (account.isEmpty())
//            return new String[]{empty};
            return null;
        else
            return accountService.findAccountByCustomerNo(customerNo).toArray();

    }

    @Hidden
    @GetMapping("/accounts/{accountNo}/accountType")
    public String getAccountType(
            @PathVariable Integer accountNo
    ){
        return accountService.getAccountType(accountNo);
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

    @Hidden
    @GetMapping("/accounts/{customerNo}/totalbalance")
    public Double getCustomerBalance(
            @PathVariable Integer customerNo
    ) {
        List<AccountResponse> account = accountService.findAccountByCustomerNo(customerNo);
        Double totalBalance = 0.0;
        if (account.isEmpty())
            return null;
        else {
            List<Double> allBalance = accountService.getBalanceOfCustomerAccounts(customerNo);
            for (int i = 0; i < allBalance.size(); i++)
                totalBalance += allBalance.get(i);
        }
        return totalBalance;
    }

    public Transactions addTransaction(Transactions transactions) {
        return accountService.save(transactions);

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
        if (amount >= 0) {
            if (account.isPresent()) {
                if (account.get().getAccountType().equals("Tagesgeldkonto")) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("No deposits allowed for account type \"" + account.get().getAccountType() + "\". \n" +
                            "For deposits into this account: Deposit requested amount into reference account (account number: " + account.get().getReferenceAccount() + ", name: "+account.get().getFirstName()+" "+account.get().getLastName()+") and then transfer in this account");
                } else {
                    accountService.depositAmount(accountNo, amount);
                    Transactions transactions = new Transactions(UUID.randomUUID().hashCode() & Integer.MAX_VALUE, account.get().getAccountNo(), account.get().getCustomerNo(), account.get().getFirstName(), account.get().getLastName(), "Deposit", Math.round(amount * 100.0) / 100.0, Math.round(account.get().getBalanceInEuro() * 100.0) / 100.0, Math.round((account.get().getBalanceInEuro() + amount) * 100.0) / 100.0, "-", LocalDate.now());
                    addTransaction(transactions);
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body("Deposit success.\n\n" +
                            "Previous balance: " + Math.round(account.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n" +
                            "Amount: " + Math.round(amount * 100.0) / 100.0 + "€ \n" +
                            "Current balance: " + Math.round((account.get().getBalanceInEuro() + amount) * 100.0) / 100.0 + "€");
                }
            } else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Deposit amount failed. Account (account number: " + accountNo + ") does not exist.");
        }else
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Deposit failed. Amount should be positive!");
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
        if (amount >= 0) {
            if (account.isPresent()) {
                if (account.get().getAccountType().equals("Tagesgeldkonto")) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("No withdrawals allowed for account type \"" + account.get().getAccountType() + "\". \n" +
                            "For withdrawal from this account: Transfer requested amount to reference account (account number: " + account.get().getReferenceAccount() + ", name: "+account.get().getFirstName()+" "+account.get().getLastName()+") and then withdraw from that account.");
                } else if (account.get().getBalanceInEuro() - amount >= 0) {
                    Transactions transactions = new Transactions(UUID.randomUUID().hashCode() & Integer.MAX_VALUE, account.get().getAccountNo(), account.get().getCustomerNo(), account.get().getFirstName(), account.get().getLastName(), "Withdraw", Math.round(amount * 100.0) / 100.0, Math.round((account.get().getBalanceInEuro()) * 100.0) / 100.0, Math.round((account.get().getBalanceInEuro() - amount) * 100.0) / 100.0, "-", LocalDate.now());
                    addTransaction(transactions);
                    accountService.withdrawAmount(accountNo, amount);
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body("Withdraw success.\n\n" +
                            "Previous balance: " + Math.round((account.get().getBalanceInEuro()) * 100.0) / 100.0 + "€\n" +
                            "Amount: " + Math.round(amount * 100.0) / 100.0 + "€ \n" +
                            "Current balance: " + Math.round((account.get().getBalanceInEuro() - amount) * 100.0) / 100.0 + "€");
                } else {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Could not withdraw.\n\n" +
                            "Requested amount: " + Math.round(amount * 100.0) / 100.0 + "€\n" +
                            "Current balance: " + Math.round(account.get().getBalanceInEuro() * 100.0) / 100.0 + "€ \n\n" +
                            "Amount is bigger than current balance.");
                }
            } else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Withdraw amount failed. Account (account number: " + accountNo + ") does not exist.");
        }else
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Withdrawal failed. Amount should be positive!");
    }

    @Operation(summary = "Transfer amount from one account to another account")
    @PutMapping("/accounts/{accountNo}/transfer/{destAccountNo}/{amount}/{purposeOfTransfer}")
    public ResponseEntity<String> transferAmount(
            @Parameter(description = "Account number of transferring account")
            @PathVariable Integer accountNo,
            @Parameter(description = "Account number of receiving account")
            @PathVariable Integer destAccountNo,
            @Parameter(description = "Amount for bank transfer")
            @PathVariable Double amount,
            @Parameter(description = "Purpose of Transfer")
            @PathVariable String purposeOfTransfer
    ) {
        Optional<AccountResponse> account1 = accountService.findByAccountNo(accountNo);
        Optional<AccountResponse> account2 = accountService.findByAccountNo(destAccountNo);
        if (amount >= 0){
            if (account1.isPresent()) {
                if (account2.isPresent()) {
                    if (account1.get().getAccountNo() != account2.get().getAccountNo()) {
                        if (account1.get().getAccountType().equals("Tagesgeldkonto") && account2.get().getAccountType().equals("Tagesgeldkonto")) {
                            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Account type of both accounts: Tagesgeldkonto\n" +
                                    "Transfer is not possible.");
                        } else if (account1.get().getAccountType().equals("Tagesgeldkonto") || account2.get().getAccountType().equals("Tagesgeldkonto")) {
                            if (account1.get().getAccountType().equals("Tagesgeldkonto")) {
                                if (account1.get().getReferenceAccount().equals(account2.get().getAccountNo())) {
                                    if (account1.get().getBalanceInEuro() - amount >= 0) {
                                        Transactions transaction1 = new Transactions(UUID.randomUUID().hashCode() & Integer.MAX_VALUE, account1.get().getAccountNo(), account1.get().getCustomerNo(), account1.get().getFirstName(), account1.get().getLastName(), "Transfer", Math.round(amount * 100.0) / 100.0, Math.round(account1.get().getBalanceInEuro() * 100.0) / 100.0, Math.round((account1.get().getBalanceInEuro() - amount) * 100.0) / 100.0, purposeOfTransfer, LocalDate.now());
                                        Transactions transaction2 = new Transactions(UUID.randomUUID().hashCode() & Integer.MAX_VALUE, account2.get().getAccountNo(), account2.get().getCustomerNo(), account2.get().getFirstName(), account2.get().getLastName(), "Receive", Math.round(amount * 100.0) / 100.0, Math.round(account2.get().getBalanceInEuro() * 100.0) / 100.0, Math.round((account2.get().getBalanceInEuro() + amount) * 100.0) / 100.0, purposeOfTransfer, LocalDate.now());
                                        addTransaction(transaction1);
                                        addTransaction(transaction2);
                                        accountService.transferAmount(accountNo, destAccountNo, amount);
                                        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Transfer success. Receiving account is reference account of transferring account. Transferred amount: " + Math.round(amount * 100.0) / 100.0 + "€\n\n" +
                                                "Transferring account (account number: " + accountNo + "):\n" +
                                                "Previous balance: " + Math.round(account1.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n" +
                                                "Current balance: " + Math.round((account1.get().getBalanceInEuro() - amount) * 100.0) / 100.0 + "€\n\n" +
                                                "Receiving account (reference account, account number: " + destAccountNo + "):\n" +
                                                "Previous balance: " + Math.round(account2.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n" +
                                                "Current balance: " + Math.round((account2.get().getBalanceInEuro() + amount) * 100.0) / 100.0 + "€");
                                    } else
                                        return ResponseEntity.status(HttpStatus.CONFLICT).body("Transfer failed.\n" +
                                                "Requested amount for transfer: " + Math.round(amount * 100.0 / 100.0) + "€\n\n" +
                                                "Current balance of transferring account (account number: " + accountNo + ", name: "+account1.get().getFirstName()+" "+account1.get().getLastName()+"): " + Math.round(account1.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n" +
                                                "Current balance of receiving account (reference account, account number: " + destAccountNo + ", name: "+account2.get().getFirstName()+" "+account2.get().getLastName()+"): " + Math.round(account2.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n\n" +
                                                "Amount is bigger than current balance of transferring account.");
                                } else
                                    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Transfer failed. Account type of transferring account: Tagesgeldkonto \n" +
                                            "Transfer is only allowed with reference account: " + account1.get().getReferenceAccount() + "\n" +
                                            "Receiving account is not reference account of transferring account.");
                            }
                            if (account2.get().getAccountType().equals("Tagesgeldkonto")) {
                                if (account2.get().getReferenceAccount().equals(account1.get().getAccountNo())) {
                                    if (account1.get().getBalanceInEuro() - amount >= 0) {
                                        Transactions transaction1 = new Transactions(UUID.randomUUID().hashCode() & Integer.MAX_VALUE, account1.get().getAccountNo(), account1.get().getCustomerNo(), account1.get().getFirstName(), account1.get().getLastName(), "Transfer", Math.round(amount * 100.0) / 100.0, Math.round(account1.get().getBalanceInEuro() * 100.0) / 100.0, Math.round((account1.get().getBalanceInEuro() - amount) * 100.0) / 100.0, purposeOfTransfer, LocalDate.now());
                                        Transactions transaction2 = new Transactions(UUID.randomUUID().hashCode() & Integer.MAX_VALUE, account2.get().getAccountNo(), account2.get().getCustomerNo(), account2.get().getFirstName(), account2.get().getLastName(), "Receive", Math.round(amount * 100.0) / 100.0, Math.round(account2.get().getBalanceInEuro() * 100.0) / 100.0, Math.round((account2.get().getBalanceInEuro() + amount) * 100.0) / 100.0, purposeOfTransfer, LocalDate.now());
                                        addTransaction(transaction1);
                                        addTransaction(transaction2);
                                        accountService.transferAmount(accountNo, destAccountNo, amount);
                                        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Transfer success. Transferring account is reference account of receiving account. Transferred amount: " + Math.round(amount * 100.0) / 100.0 + "€\n\n" +
                                                "Transferring account (reference account, account number: " + accountNo + "):\n" +
                                                "Previous balance: " + Math.round(account1.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n" +
                                                "Current balance: " + Math.round((account1.get().getBalanceInEuro() - amount) * 100.0) / 100.0 + "€\n\n" +
                                                "Receiving account (account number: " + destAccountNo + "):\n" +
                                                "Previous balance: " + Math.round(account2.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n" +
                                                "Current balance: " + Math.round((account2.get().getBalanceInEuro() + amount) * 100.0) / 100.0 + "€");
                                    } else
                                        return ResponseEntity.status(HttpStatus.CONFLICT).body("Transfer failed.\n" +
                                                "Requested amount for transfer: " + Math.round(amount * 100.0 / 100.0) + "€\n\n" +
                                                "Current balance of transferring account (reference account, account number: " + accountNo + ", name: "+account1.get().getFirstName()+" "+account1.get().getLastName()+"): " + Math.round(account1.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n" +
                                                "Current balance of receiving account (account number: " + destAccountNo + ",name: "+account2.get().getFirstName()+" "+account2.get().getLastName()+"): " + Math.round(account2.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n\n" +
                                                "Amount is bigger than current balance of transferring account.");
                                } else
                                    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Transfer failed. Account type of receiving account: Tagesgeldkonto \n" +
                                            "Transfer is only allowed with reference account: " + account2.get().getReferenceAccount() + "\n" +
                                            "Transferring account is not reference account of receiving account.");
                            }
                        } else if (!account1.get().getAccountType().equals("Tagesgeldkonto") && !account2.get().getAccountType().equals("Tagesgeldkonto")) {
                            if (account1.get().getBalanceInEuro() - amount >= 0) {
                                Transactions transaction1 = new Transactions(UUID.randomUUID().hashCode() & Integer.MAX_VALUE, account1.get().getAccountNo(), account1.get().getCustomerNo(), account1.get().getFirstName(), account1.get().getLastName(), "Transfer", Math.round(amount * 100.0) / 100.0, Math.round(account1.get().getBalanceInEuro() * 100.0) / 100.0, Math.round((account1.get().getBalanceInEuro() - amount) * 100.0) / 100.0, purposeOfTransfer, LocalDate.now());
                                Transactions transaction2 = new Transactions(UUID.randomUUID().hashCode() & Integer.MAX_VALUE, account2.get().getAccountNo(), account2.get().getCustomerNo(), account2.get().getFirstName(), account2.get().getLastName(), "Receive", Math.round(amount * 100.0) / 100.0, Math.round(account2.get().getBalanceInEuro() * 100.0) / 100.0, Math.round((account2.get().getBalanceInEuro() + amount) * 100.0) / 100.0, purposeOfTransfer, LocalDate.now());
                                addTransaction(transaction1);
                                addTransaction(transaction2);
                                accountService.transferAmount(accountNo, destAccountNo, amount);
                                return ResponseEntity.status(HttpStatus.ACCEPTED).body("Transfer success. Transferred amount: " + Math.round(amount * 100.0) / 100.0 + "€\n\n" +
                                        "Transferring account  (account number: " + accountNo + "):\n" +
                                        "Previous balance: " + Math.round(account1.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n" +
                                        "Current balance: " + Math.round((account1.get().getBalanceInEuro() - amount) * 100.0) / 100.0 + "€\n\n" +
                                        "Receiving account (account number " + destAccountNo + "):\n" +
                                        "Previous balance: " + Math.round(account2.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n" +
                                        "Current balance: " + Math.round((account2.get().getBalanceInEuro() + amount) * 100.0) / 100.0 + "€");
                            } else
                                return ResponseEntity.status(HttpStatus.CONFLICT).body("Transfer failed.\n" +
                                        "Requested amount for transfer: " + Math.round(amount * 100.0 / 100.0) + "€\n\n" +
                                        "Current balance of transferring account (account number: " + accountNo + "): " + Math.round(account1.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n" +
                                        "Current balance of receiving account (account number: " + destAccountNo + "): " + Math.round(account2.get().getBalanceInEuro() * 100.0) / 100.0 + "€\n\n" +
                                        "Amount is bigger than current balance of transferring account.");
                        }

                    } else
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("Transferring account and receiving account are the same!");

                }
                if (account1.get().getAccountType().equals("Tagesgeldkonto")) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Transfer failed.\n" +
                            "The receiving account (account number: " + destAccountNo + ") does not exist.\n" +
                            "The transferring account (account number: " + accountNo + ") is not allowed to transfer or receive money (only allowed with reference account (account number: " + account1.get().getReferenceAccount() + ").\n Account type: \"" + account1.get().getAccountType() + "\".");
                } else
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transfer failed. The receiving account (account number: " + destAccountNo + ") does not exist.");
            } else if (account2.isPresent()) {
                if (account2.get().getAccountType().equals("Tagesgeldkonto")) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Transfer failed.\n" +
                            "The transferring account (account number: " + accountNo + ") does not exist.\n" +
                            "The receiving account (account number: " + destAccountNo + ") is not allowed to transfer or receive money (only allowed with reference account (account number: " + account2.get().getReferenceAccount() + ").\n Account type: \"" + account2.get().getAccountType() + "\".");
                } else
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transfer failed. The transferring account (account number: " + accountNo + ") does not exist.");
            } else
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transfer failed. Accounts with account numbers \"" + accountNo + "\" and \"" + destAccountNo + "\" don't exist.");

    }else
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Transfer failed. Amount should be positive!");
    }

    @Operation(summary = "Create an account")
    @PostMapping("/accounts")
    public ResponseEntity<String> createAccount(
            @Parameter(description = "Customer number of customer to allocate")
            @RequestBody AccountCreateRequest aRequest,
            @Parameter(description = "Type of account")
            @RequestParam AccountResponseEnum enumRequest
    ) {
        Iban iban = new Iban.Builder()
                .countryCode(CountryCode.DE)
                .buildRandom();

//        String firstName = restTemplate.getForObject("http://customer:8080/api/customers/" + aRequest.getCustomerNo() + "/first-name", String.class);
        String firstName = restTemplate.getForObject("http://localhost:8080/api/customers/" + aRequest.getCustomerNo() + "/first-name", String.class);
        String lastName = restTemplate.getForObject("http://localhost:8080/api/customers/" + aRequest.getCustomerNo() + "/last-name", String.class);
//        String lastName = restTemplate.getForObject("http://customer:8080/api/customers/" + aRequest.getCustomerNo() + "/last-name", String.class);

        Integer age = restTemplate.getForObject("http://localhost:8080/api/customers/" + aRequest.getCustomerNo() + "/age", Integer.class);
//        Integer age = restTemplate.getForObject("http://customer:8080/api/customers/"+aRequest.getCustomerNo()+"/age", Integer.class);


        List<AccountResponse> account = accountService.findAccountByCustomerNo(aRequest.getCustomerNo());

        Integer accountNo = UUID.randomUUID().hashCode() & Integer.MAX_VALUE;
        AccountResponse acct = new AccountResponse(
                aRequest.getCustomerNo(),
                enumRequest.getAccountType(),
                firstName,
                lastName,
                accountNo,
                (iban.getCountryCode() + iban.getCheckDigit() + iban.getBban()).replaceAll("(\\w\\w\\w\\w)(\\w\\w\\w\\w)(\\w\\w\\w\\w)(\\w\\w\\w\\w)(\\w\\w\\w\\w)(\\w\\w)", "$1 $2 $3 $4 $5 $6"),
                0.0,
                LocalDate.now(),
                aRequest.getReferenceAccount()
        );
        boolean customers = (restTemplate.getForObject("http://localhost:8080/api/customers/numbers", List.class).contains(aRequest.getCustomerNo()));
//        boolean customers = (restTemplate.getForObject("http://customer:8080/api/customers/numbers", List.class).contains(aRequest.getCustomerNo()));
        if (customers == true) {
            if (aRequest.getReferenceAccount() == 0) {
                acct.setReferenceAccount(null);
            }

//        Referenzkonto für Girokonto oder Schülerkonto nicht erlaubt
            if (enumRequest.getAccountType() == "Girokonto" && aRequest.getReferenceAccount() != 0 || enumRequest.getAccountType() == "Schülerkonto" && aRequest.getReferenceAccount() != 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reference account for account type \"" + enumRequest.getAccountType() + "\" is not allowed.");
            }

//        Erstelle Girokonto wenn Kunde mindestens 18 ist
            else if (enumRequest.getAccountType() == "Girokonto" && age >= 18) {
                accountService.createAccount(acct);
                return ResponseEntity.status(HttpStatus.OK).body("Account for customer (customer number: "+aRequest.getCustomerNo()+", name: " + firstName +" "+lastName+ ", age: "+age+") created.\n" +
                        "Account number: "+accountNo+"\n" +
                        "Account type: "+enumRequest.getAccountType());
            }


//        Erstelle kein Girokonto wenn Kunde unter 18 ist
            else if (enumRequest.getAccountType() == "Girokonto" && age < 18) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Account type \"" + enumRequest.getAccountType() + "\" available for 18 years and older.\n" +
                        "Age of customer: " + age);
            }
//        Erstelle Schülerkonto wenn Kunde mindestens 7 und höchstens 17 Jahre ist
            else if (enumRequest.getAccountType() == "Schülerkonto" && age < 18 && age >= 7) {
                accountService.createAccount(acct);
                return ResponseEntity.status(HttpStatus.OK).body("Account for customer (customer number: "+aRequest.getCustomerNo()+", name: " + firstName +" "+lastName+ ", age: "+age+") created.\n" +
                        "Account number: "+accountNo+"\n" +
                        "Account type: "+enumRequest.getAccountType());
            }
//        Erstelle kein Schülerkonto wenn Kunde unter 7 oder mindestens 18 Jahre ist
            else if (enumRequest.getAccountType() == "Schülerkonto" && age >= 18 || enumRequest.getAccountType() == "Schülerkonto" && age < 7) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Account type \"" + enumRequest.getAccountType() + "\" available for 7-17 year olds. \n" +
                        "Age of customer: " + age);
            }
//        Fehler, wenn kein Referenzkonto (bei Tagesgeldkonto) angegeben ist
            else if (enumRequest.getAccountType() == "Tagesgeldkonto" && aRequest.getReferenceAccount() == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reference account missing!");
            } else if (enumRequest.getAccountType() == "Tagesgeldkonto" && aRequest.getReferenceAccount() != 0) {


                for (int i = 0; i < account.size(); i++) {

                    if (account.get(i).getAccountNo().equals(aRequest.getReferenceAccount()) && account.get(i).getAccountType().equals("Girokonto")) {
                        accountService.createAccount(acct);
                        return ResponseEntity.status(HttpStatus.OK).body("Account (" + enumRequest.getAccountType() + ") for customer (customer number: "+aRequest.getCustomerNo()+", name: " + firstName +" "+lastName+ ") created. Reference account is account with account number: " + account.get(i).getAccountNo());
                    } else if ((i + 1) == account.size()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("Could not create account \"" + enumRequest.getAccountType() + "\". Possible reasons: \n\n" +
                                "- Reference account (" + aRequest.getReferenceAccount() + ") does not exist\n" +
                                "- Reference account (" + aRequest.getReferenceAccount() + ") is not a \"Girokonto\"\n" +
                                "- Customer (" + aRequest.getCustomerNo() + ") does not own reference account (" + aRequest.getReferenceAccount() + ")");
                    }
                }
            }
        } else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer (customer number: " + aRequest.getCustomerNo() + ") does not exist.");
        return null;
    }

    @Operation(summary = "Delete an account")
    @DeleteMapping("/accounts/{accountNo}")
    public ResponseEntity deleteAccount(
            @Parameter(description = "Account number of account to delete")
            @PathVariable Integer accountNo

    ) {
        Optional<AccountResponse> account = accountService.findByAccountNo(accountNo);

        if (account.isPresent()) {
            List credit = restTemplate.getForObject("http://localhost:8090/api/credits/account-credit/" + accountNo, List.class);
//            List credit = restTemplate.getForObject("http://credit:8090/api/credits/account-credit/" + accountNo, List.class);
            if (credit == null) {
                if (account.get().getBalanceInEuro() == 0.0) {
                    accountService.deleteByAccountNo(accountNo);
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body("Account (account number: " + accountNo + ", name: "+account.get().getFirstName()+" "+account.get().getLastName()+") deleted.");
                } else
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Could not delete. Balance of account (account number: " + accountNo + ") is not zero.");
            } else
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Could not delete. Account (account number: " + accountNo + ",name: "+account.get().getFirstName()+" "+account.get().getLastName()+") still has ongoing credits.");

        } else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Could not delete. Account (account number: " + accountNo + ") does not exist.");

    }

    @Hidden
    @Operation(summary = "Delete all accounts of a customer")
    @DeleteMapping("/accounts/customer-accounts/{customerNo}")
    public ResponseEntity deleteAccountsofCustomer(
            @Parameter(description = "Customer number of customer to delete all accounts")
            @PathVariable Integer customerNo
    ) {
        List<AccountResponse> account = accountService.findAccountByCustomerNo(customerNo);


        if (account.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer (customer number: " + customerNo + ") has no accounts.");
        } else {
            List<Integer> accountNo = accountService.getAccountNoOfCustomerAccounts(customerNo);
            for (int i = 0; i < accountNo.size(); i++) {
                List credit = restTemplate.getForObject("http://localhost:8090/api/credits/account-credit/" + accountNo.get(i), List.class);
//                List credit = restTemplate.getForObject("http://credit:8090/api/credits/account-credit/" + accountNo.get(i), List.class);

                if (credit == null) {
                    if (account.get(i).getBalanceInEuro() == 0.0) {
                        accountService.deleteByAccountNo(accountNo.get(i));
                    }

                }
//                restTemplate.delete("http://localhost:8090/api/credits/account-credits/{accountNo}", accountNo.get(i));
//                restTemplate.delete("http://credit:8090/api/credits/account-credits/{accountNo}", accountNo.get(i));
            }
            if (account.isEmpty()) {
                return null;
            }
            return ResponseEntity.status(HttpStatus.OK).body("All accounts of customer (customer number: " + customerNo + ") with zero balance and zero ongoing credits deleted.");
        }
    }

}
