package com.sportsbook.payment.validator.domain

import org.springframework.data.jpa.repository.JpaRepository

interface DepositDecisionRepository : JpaRepository<DepositDecisionEntity, String>
