package com.example.antique.entity;

/**
 * Enum trạng thái phiếu xuất kho.
 */
public enum TrangThaiPhieuXuat {

    XUAT_KHO("Đã xuất kho"),
    HUY("Đã hủy");

    private final String label;

    TrangThaiPhieuXuat(String label) { this.label = label; }

    public String getLabel() { return label; }

}
