package com.fein91.dao;

import com.fein91.model.Trade;
import org.springframework.data.repository.CrudRepository;

public interface TradeRepository extends CrudRepository<Trade, Long> {
}
