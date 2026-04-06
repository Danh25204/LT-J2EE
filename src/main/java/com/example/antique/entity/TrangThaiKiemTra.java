package com.example.antique.entity;

/**
 * Trạng thái yêu cầu kiểm tra đồ cổ.
 */
public enum TrangThaiKiemTra {

    CHO_KIEM_TRA("Chờ kiểm tra", "danger"),
    DA_KIEM_TRA("Đã kiểm tra", "success"),
    BO_QUA("Bỏ qua", "secondary");

    private final String moTa;
    private final String badgeColor;

    TrangThaiKiemTra(String moTa, String badgeColor) {
        this.moTa = moTa;
        this.badgeColor = badgeColor;
    }

    public String getMoTa()       { return moTa; }
    public String getBadgeColor() { return badgeColor; }
}
