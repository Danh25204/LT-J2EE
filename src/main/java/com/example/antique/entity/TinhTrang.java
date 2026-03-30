package com.example.antique.entity;

/**
 * Enum tình trạng hiện vật đồ cổ.
 * Hibernate lưu dưới dạng STRING (tên enum) thay vì số để dễ đọc SQL.
 *
 * Cách dùng trong Entity: @Enumerated(EnumType.STRING)
 */
public enum TinhTrang {

    TOT("Tốt"),
    KHA("Khá"),
    TRUNG_BINH("Trung bình"),
    HU_HONG("Hư hỏng");

    // Nhãn hiển thị tiếng Việt trên UI
    private final String label;

    TinhTrang(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
