package com.example.webapp.domain;
import javax.persistence.Entity;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
@Table(name = "journals")
public class Journal implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "newowner")
    private String newowner;
    @Column(name = "messageid")
    private Long messageid;
    @Column(name = "date", columnDefinition = "TIMESTAMP")
    private java.util.Date date;

    public Journal() {
    }


    public Journal(Long id) {
        this.id = id;
    }

    public Journal(Long id, String newowner, Long messageid) {
        this.id = id;
        this.messageid = messageid;
        this.newowner = newowner;
    }

    public Journal(Date date, Long messageid) {
        this.date = date;
        this.messageid = messageid;
    }

    public Journal(String newowner) {
        this.newowner = newowner;
    }

    public Journal(Long id, String newowner) {
        this.id = id;
        this.newowner = newowner;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNewowner() {
        return newowner;
    }

    public String getNewowner(String newowner) {
        return this.newowner;
    }

    public void setNewowner(String newowner) {
        this.newowner = newowner;
    }

    public Long getMessageid() {
        return messageid;
    }

    public void setMessageid(Long messageid) {
        this.messageid = messageid;
    }
}
