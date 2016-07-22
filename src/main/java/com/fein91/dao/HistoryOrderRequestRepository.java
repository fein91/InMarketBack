package com.fein91.dao;

import com.fein91.model.Counterparty;
import com.fein91.model.HistoryOrderRequest;
import com.fein91.model.Invoice;
import com.fein91.model.OrderType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoryOrderRequestRepository extends CrudRepository<HistoryOrderRequest, Long> {

    List<HistoryOrderRequest> findByCounterparty(Counterparty counterparty);

    @Query("SELECT hor FROM HistoryOrderRequest hor where hor.counterparty.id = :counterPartyId and hor.historyOrderType in :historyOrderTypes")
    List<HistoryOrderRequest> findByCounterpartyIdAndHistoryOrderType(@Param("counterPartyId") Long counterPartyId,
                                                                      @Param("historyOrderTypes") List<String> historyOrderTypes);

    HistoryOrderRequest findByOriginOrderRequestId(Long originOrderRequestId);
}
