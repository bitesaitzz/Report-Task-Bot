package com.bitesait.ReportsBot.model;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "ReportTable")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String type;

    @Column(name = "date", columnDefinition = "DATE")
    private LocalDate date;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "report", fetch = FetchType.EAGER)
    private List<Photo> photos = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "reestr_id")
    private Reestr reestr;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String description;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Reestr getReestr() {
        return reestr;
    }

    public void setReestr(Reestr reestr) {
        this.reestr = reestr;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }



    public void addPhoto(Photo photo) {

       photo.setReport(this);
       photos.add(photo);
    }
}
