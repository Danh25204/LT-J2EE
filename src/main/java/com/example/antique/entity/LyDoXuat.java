package com.example.antique.entity;

/**
 * Enum lý do xuất kho.
 * Theo yêu cầu đồ án: Bán, Cho mượn, Điều chuyển, Khác.
 */
public enum LyDoXuat {

    BAN("Bán"),
    CHO_MUON("Cho mượn"),
    DIEU_CHUYEN("Điều chuyển"),
    KHAC("Khác");

    private final String label;

    LyDoXuat(String label) { this.label = label; }

    public String getLabel() { return label; }

}
