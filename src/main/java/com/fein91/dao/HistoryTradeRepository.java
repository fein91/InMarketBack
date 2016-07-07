package com.fein91.dao;

import com.fein91.model.HistoryTrade;
import org.springframework.data.repository.CrudRepository;

public interface HistoryTradeRepository extends CrudRepository<HistoryTrade, Long> {
}
