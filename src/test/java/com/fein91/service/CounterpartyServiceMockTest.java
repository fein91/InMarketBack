package com.fein91.service;

import com.fein91.InMarketApplication;
import com.fein91.dao.CounterpartyRepository;
import com.fein91.model.Counterparty;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.easymock.EasyMock.*;

/**
 * Unit test for counterparty service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class CounterpartyServiceMockTest {

    @Test
    public void addCounterParty() {
        CounterpartyRepository repository = createMock(CounterpartyRepository.class);
        CounterPartyService service = new CounterPartyServiceImpl(repository);
        expect(repository.save(anyObject(Counterparty.class))).andReturn(Counterparty.of("cp"));
        replay(repository);

        Counterparty cp = service.addCounterParty("cp");

        Assert.assertEquals("cp", cp.getName());
        verify(repository);
    }
}
