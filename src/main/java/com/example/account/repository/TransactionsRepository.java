package com.example.account.repository;

import com.example.account.model.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TransactionsRepository extends JpaRepository<Transactions, Integer> {


    @Query(value = "select * from Transactions t where t.account_No = ?1",nativeQuery = true)
    List<Transactions> findTransactionsByAccountNo(Integer accountNo);
}
