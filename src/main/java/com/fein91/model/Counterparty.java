package com.fein91.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by olta1014 on 23.05.2016.
 */
@Entity
public class Counterparty {

    @Id
    @GeneratedValue
    private Long id;
    private String name;

    private String loginName;

    public Counterparty() {
        //for JPA
    }

    private Counterparty(String name) {
        this.name = name;
    }

    public static Counterparty of(String name) {
        return new Counterparty(name);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
}
