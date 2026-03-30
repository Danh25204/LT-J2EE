package com.example.antique.entity;

/**
 * Enum trạng thái phiếu nhập kho.
 */
public enum TrangThaiPhieuNhap {

    NHAP_KHO("Đã nhập kho"),
    HUY("Đã hủy");

    private final String label;

    TrangThaiPhieuNhap(String label) { this.label = label; }

    public String getLabel() { return label; }

}
