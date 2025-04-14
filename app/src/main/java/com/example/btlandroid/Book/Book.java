package com.example.btlandroid.Book;

import java.io.Serializable;

public class Book implements Serializable {
    public int id;
    public String name;
    public String note;
    public double price;
    public String image;
    public String category;
    public int quantity;

    public Book() {
    }

    public Book(int id, String name, String note,double price, String image, String category,int quantity){
        this.id = id;
        this.name = name;
        this.note = note;
        this.price = price;
        this.image = image;
        this.category = category;
        this.quantity = quantity;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String toString() {
        return name;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getNote(){
        return note;
    }

    public void setNote(String note){
        this.note = note;
    }

    public double getPrice(){
        return price;
    }

    public void setPrice(double price){
        this.price = price;
    }

    public String getImage(){
        return image;
    }

    public void setImage(String image){
        this.image = image;
    }

    public String getCategory(){
        return category;
    }

    public void setCategory(String category){
        this.category = category;
    }

    public int getQuantity(){return quantity;}
    public void setQuantity(int quantity){
        this.quantity = quantity;
    }
}
