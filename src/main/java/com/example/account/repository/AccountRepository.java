package com.example.account.repository;

import com.example.account.model.AccountResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/*@Service
public class AccountRepository {

    List<AccountResponse> accounts = new ArrayList<>();

    public List<AccountResponse> findAll() {

        return accounts;
    }

    public ResponseEntity<Object> save(AccountCreateRequest aRequest) {
        Iban iban = new Iban.Builder()
                .countryCode(CountryCode.DE)
                        .buildRandom();

       accounts.add(
               new AccountResponse(
                       aRequest.getkNr(),
                       UUID.randomUUID().hashCode() & Integer.MAX_VALUE,
                       (iban.getCountryCode()+ iban.getCheckDigit()+iban.getBban()).replaceAll("(\\w\\w\\w\\w)(\\w\\w\\w\\w)(\\w\\w\\w\\w)(\\w\\w\\w\\w)(\\w\\w\\w\\w)(\\w\\w)","$1 $2 $3 $4 $5 $6"),
                       0.0,
                       LocalDate.now())
                );
            return ResponseEntity.ok().build();
    }

//    private double roundAndFormat(double balanceInEuro, int i, Locale german) {
//        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(german);
//        nf.setMaximumFractionDigits(2);
//        return Math.round(balanceInEuro*100.0)/100.0;
//    }

    public void deleteByaNr(Integer aNr) {
        this.accounts = accounts.stream()
                .filter(p -> !p.getaNr().equals(aNr))
                .collect(Collectors.toList());
    }

    public void deleteAccountByKNr(Integer kNr){
        this.accounts =accounts.stream()
                .filter(a -> !a.getkNr().equals(kNr))
                .collect(Collectors.toList());
    }


    public Optional<AccountResponse> findByANr(Integer aNr) {
        Optional<AccountResponse> account = accounts.stream()
                .filter(c -> c.getaNr().equals(aNr))
                .findFirst();
        return account;
    }
    public List<AccountResponse> FindAccountByKNr(Integer kNr) {
        List<AccountResponse> account = accounts.stream()
                .filter(c -> c.getkNr().equals(kNr))
                .collect(Collectors.toList());
        return account;
    }

    public double findBalanceByANr(Integer aNr) {
        Optional<AccountResponse> account = accounts.stream()
                .filter(c -> c.getaNr().equals(aNr))
                .findFirst();
        double currentBalance = (account.get().getBalanceInEuro());
        return Math.round(currentBalance*100.0)/100.0;
    }

    
    public void saveBalanceByANr(Integer aNr, Double amount) {
        Optional<AccountResponse> account = accounts.stream()
                .filter(c -> c.getaNr().equals(aNr))
                .findFirst();
        double currentBalance = (account.get().getBalanceInEuro());
        double newBalance = currentBalance +Math.round(amount*100.0)/100.0;

        account.get().setBalanceInEuro(Math.round(newBalance*100.0)/100.0);

    }

    public void withdrawAmountByANr(Integer aNr, Double amount) {
        Optional<AccountResponse> account = accounts.stream()
                .filter(c -> c.getaNr().equals(aNr))
                .findFirst();
        double currentBalance = account.get().getBalanceInEuro();
        double newBalance = currentBalance -Math.round(amount*100.0)/100.0;
        if (newBalance >= 0)
            account.get().setBalanceInEuro(Math.round(newBalance*100.0)/100.0);

    }

    public void transferAmountByANr(Integer aNr, Integer newANr, Double amount) {
        Optional<AccountResponse> account1 = accounts.stream()
                .filter(c -> c.getaNr().equals(aNr))
                .findFirst();
        Optional<AccountResponse> account2 = accounts.stream()
                .filter(c -> c.getaNr().equals(newANr))
                .findFirst();
        double currentBalance1 = account1.get().getBalanceInEuro();
        double currentBalance2 = account2.get().getBalanceInEuro();

        double newBalance1 = currentBalance1 - Math.round(amount*100.0)/100.0;
        double newBalance2 = currentBalance2 + Math.round(amount*100.0)/100.0;
        if (account1.get().getaNr() != account2.get().getaNr()) {
            if (newBalance1 >= 0) {
                account1.get().setBalanceInEuro(Math.round(newBalance1 * 100.0) / 100.0);
                account2.get().setBalanceInEuro(Math.round(newBalance2 * 100.0) / 100.0);
            }
        }
    }
}*/

@Repository
public interface AccountRepository extends CrudRepository<AccountResponse, Integer>, JpaRepository<AccountResponse, Integer> {


    @Transactional
    @Modifying
    @Query("DELETE FROM AccountResponse a WHERE a.customerNo = ?1")
    void deleteAccountByCustomerNo(Integer customerNo);



    @Query("select a from AccountResponse a where a.customerNo = ?1")
    List<AccountResponse> findAccountByCustomerNo(Integer customerNo);



    @Query("select balanceInEuro from AccountResponse where accountNo = ?1")
    public double findBalanceByAccountNo(Integer accountNo);


    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update AccountResponse set balanceInEuro = balanceInEuro+?2 where accountNo=?1")
    public void saveBalanceByAccountNo(Integer accountNo, Double amount);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update AccountResponse set balanceInEuro = balanceInEuro-?2 where accountNo=?1")
    public void withdrawAmountByAccountNo(Integer accountNo, Double amount);

    @Query("select accountNo from AccountResponse")
    List<Integer> findAllAccountNo();

    @Query("select accountNo from AccountResponse where customerNo = ?1")
    List<Integer> getAccountNoOfCustomerAccounts(Integer customerNo);
}