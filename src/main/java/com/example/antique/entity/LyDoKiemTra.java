package com.example.antique.entity;

/**
 * Lý do tạo yêu cầu kiểm tra đồ cổ.
 */
public enum LyDoKiemTra {

    TON_KHO_LAU("Tồn kho lâu ngày", "warning", "bi-hourglass-split",
            "Đồ cổ chưa có hoạt động xuất kho trong thời gian dài"),

    SAU_CHO_MUON("Sau khi trả hàng cho mượn", "info", "bi-arrow-return-left",
            "Đồ cổ vừa được trả lại sau khi cho mượn, cần kiểm tra tình trạng");

    private final String moTa;
    private final String badgeColor;
    private final String icon;
    private final String huongDan;

    LyDoKiemTra(String moTa, String badgeColor, String icon, String huongDan) {
        this.moTa = moTa;
        this.badgeColor = badgeColor;
        this.icon = icon;
        this.huongDan = huongDan;
    }

    public String getMoTa()       { return moTa; }
    public String getBadgeColor() { return badgeColor; }
    public String getIcon()       { return icon; }
    public String getHuongDan()   { return huongDan; }
}
