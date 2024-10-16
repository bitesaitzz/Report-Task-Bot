package com.bitesait.ReportsBot.model;


import jakarta.persistence.*;

@Entity(name = "PhotoTable")
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @Lob
    private byte[] photoData;

    private String fileId;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;


    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte[] getPhotoData() {
        return photoData;
    }

    public void setPhotoData(byte[] photoData) {
        this.photoData = photoData;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
