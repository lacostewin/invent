package com.example.webapp.domain;
import com.google.zxing.common.BitMatrix;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "message")
public class Message implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "text")
    private String text;
    @Column(name = "sn")
    private String sn;
    @Column(name = "invid")
    private String invid;
    @Column(name = "owner")
    private String owner;
    @Column(name = "author")
    private String author;
    @Lob
    @Column(name = "qrcode")
    private String qrcode;


    public Message() {
    }

    public Message(String text, String sn, String owner, String author, String invid, String qrcode) {
        this.text = text;
        this.sn = sn;
        this.owner = owner;
        this.author = author;
        this.invid = invid;
        this.qrcode = qrcode;
    }

    public Message(String sn) {
        this.sn = sn;
    }

    public Message(Long id, String sn, String text, String invid) {
        this.id = id;
        this.sn = sn;
        this.text = text;
        this.invid = invid;
    }

    public Message(String owner, Long id, String sn, String text, String author, String invid) {
        this.id = id;
        this.owner = owner;
        this.sn = sn;
        this.text = text;
        this.author = author;
        this.invid = invid;
    }

    public Message(Long id) {
        this.id = id;
    }

    public Message(Long id, String sn, String text, String invid, String qrcode) {
        this.id = id;
        this.sn = sn;
        this.text = text;
        this.invid = invid;
        this.qrcode = qrcode;
    }

    public Message (Long id, String owner, String sn, String text, String author, String invid) {
        this.id = id;
        this.owner = owner;
        this.sn = sn;
        this.text = text;
        this.author = author;
        this.invid = invid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getInvid() {
        return invid;
    }

    public void setInvid(String invid) {
        this.invid = invid;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }
}