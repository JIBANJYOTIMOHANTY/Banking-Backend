package com.learning.Project.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.learning.Project.model.CustomerAccount;

@Repository
public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Long> {
    Optional<CustomerAccount> findByAccountNumber(String accountNumber);
    Optional<CustomerAccount> findFirstByOrderByIdDesc();

    @Query("SELECT c FROM CustomerAccount c WHERE " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.accountNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.mobileNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.govtId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.pan) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.state) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.country) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.pincode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.nomineeName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.address) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<CustomerAccount> searchAccounts(@Param("query") String query);
}
