package com.fein91.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.math.BigInteger;
import java.util.List;

/**
 * Created by olta1014 on 23.05.2016.
 */
@Entity
public class Counterparty {

    @Id
    private BigInteger id;
    private String name;

    @OneToMany(mappedBy="counterPartyFrom")
    private List<Invoice> invoicesFrom;

    @OneToMany(mappedBy="counterPartyTo")
    private List<Invoice> invoicesTo;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Invoice> getInvoicesFrom() {
        return invoicesFrom;
    }

    public void setInvoicesFrom(List<Invoice> invoicesFrom) {
        this.invoicesFrom = invoicesFrom;
    }

    public List<Invoice> getInvoicesTo() {
        return invoicesTo;
    }

    public void setInvoicesTo(List<Invoice> invoicesTo) {
        this.invoicesTo = invoicesTo;
    }
}
