package com.bitesait.ReportsBot.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.List;

@Entity(name = "UserTable")
public class User {

    @Id
    private long chatId;

    private String name;


    @OneToMany(mappedBy = "user")
    private List<Report> report;

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
