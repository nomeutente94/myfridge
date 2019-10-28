package com.example.robertotarullo.myfridge.bean;

import android.widget.Button;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class Filter {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    // private int position; // TODO Si salva nelle preferenze locali dell'app
    @Ignore
    private Button button;
    @Ignore
    private List<Product> products;
    @Ignore
    private int notificationCount;

    public Filter(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Button getButton() {
        return button;
    }

    public void setButton(Button button) {
        this.button = button;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }
}
