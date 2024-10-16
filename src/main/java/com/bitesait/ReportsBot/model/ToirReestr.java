package com.bitesait.ReportsBot.model;


import jakarta.persistence.*;

@Entity(name = "ToirReestrTable")
public class ToirReestr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "reestr_id")
    private Reestr reestr;

    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Reestr getReestr() {
        return reestr;
    }

    public void setReestr(Reestr reestr) {
        this.reestr = reestr;
    }
}
