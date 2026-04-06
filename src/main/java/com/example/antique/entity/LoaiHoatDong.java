package com.example.antique.entity;

/**
 * Phân loại hoạt động kho dùng cho bảng lịch sử.
 */
public enum LoaiHoatDong {

    NHAP_KHO("Nhập kho", "success", "bi-box-arrow-in-down"),
    XUAT_KHO("Xuất kho", "danger", "bi-box-arrow-up"),
    HUY_NHAP("Hủy phiếu nhập", "warning", "bi-x-circle"),
    HUY_XUAT("Hủy phiếu xuất", "warning", "bi-x-circle"),
    TRA_HANG("Nhận lại hàng cho mượn", "success", "bi-arrow-return-left"),
    DIEU_CHINH("Điều chỉnh tồn kho", "info", "bi-pencil-square");

    private final String moTa;
    private final String badgeColor;   // Bootstrap color class
    private final String icon;         // Bootstrap Icon class

    LoaiHoatDong(String moTa, String badgeColor, String icon) {
        this.moTa = moTa;
        this.badgeColor = badgeColor;
        this.icon = icon;
    }

    public String getMoTa()       { return moTa; }
    public String getBadgeColor() { return badgeColor; }
    public String getIcon()       { return icon; }
}
