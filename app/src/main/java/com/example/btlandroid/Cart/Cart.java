package com.example.btlandroid.Cart;

import java.io.Serializable;

public class Cart implements Serializable {
    private String id;
    private String ten;
    private double gia;
    private int soLuong;
    private String image;

    public Cart() {}

    public Cart(String id, String ten, double gia, int soLuong, String image) {
        this.id = id;
        this.ten = ten;
        this.gia = gia;
        this.soLuong = soLuong;
        this.image = image;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public double getGia() { return gia; }
    public void setGia(double gia) { this.gia = gia; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}