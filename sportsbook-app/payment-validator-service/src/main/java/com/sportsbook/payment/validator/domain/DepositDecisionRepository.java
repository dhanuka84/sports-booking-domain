package com.sportsbook.payment.validator.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositDecisionRepository extends JpaRepository<DepositDecisionEntity, String> {}
