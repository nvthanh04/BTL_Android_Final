package com.example.btlandroid.HoaDon;

import com.example.btlandroid.Cart.Cart;

import java.io.Serializable;
import java.util.List;

public class HoaDon implements Serializable {
    public String id;
    public String address;
    public float totalMoney;
    public String trangThai;

    // Thông tin người dùng
    public String tenNguoiDung;
    public String soDienThoai;

    public String ngayDat;
    public String ngayGiao;
    public String email;

    public List<Cart> danhSachSanPham;

    public HoaDon() {
    }

    public HoaDon(String address, float totalMoney, String trangThai,
                  String tenNguoiDung, String soDienThoai,String ngayDat, String ngayGiao,String email, List<Cart> danhSachSanPham) {
        this.address = address;
        this.totalMoney = totalMoney;
        this.ngayDat = ngayDat;
        this.ngayGiao = ngayGiao;
        this.trangThai = trangThai;
        this.tenNguoiDung = tenNguoiDung;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.danhSachSanPham = danhSachSanPham;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public float getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(float totalMoney) {
        this.totalMoney = totalMoney;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getTenNguoiDung() {
        return tenNguoiDung;
    }

    public void setTenNguoiDung(String tenNguoiDung) {
        this.tenNguoiDung = tenNguoiDung;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public List<Cart> getDanhSachSanPham() {
        return danhSachSanPham;
    }

    public void setDanhSachSanPham(List<Cart> danhSachSanPham) {
        this.danhSachSanPham = danhSachSanPham;
    }

    public String getNgayDat() {
        return ngayDat;
    }

    public void setNgayDat(String ngayDat) {
        this.ngayDat = ngayDat;
    }

    public String getNgayGiao() {
        return ngayGiao;
    }

    public void setNgayGiao(String ngayGiao) {
        this.ngayGiao = ngayGiao;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}