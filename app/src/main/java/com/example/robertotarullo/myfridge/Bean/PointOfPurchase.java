package com.example.robertotarullo.myfridge.Bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.time.LocalTime;
import java.util.Date;

@Entity
public class PointOfPurchase {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private String address;
    private String website;
    private Date lastVisit;
    private int phoneNumber;
    @Ignore
    private LocalTime[][] openingHours;

    public PointOfPurchase() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Date getLastVisit() {
        return lastVisit;
    }

    public void setLastVisit(Date lastVisit) {
        this.lastVisit = lastVisit;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalTime[][] getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(LocalTime[][] openingHours) {
        this.openingHours = openingHours;
    }

    @Override
    public String toString() {
        return getName();
    }
}
