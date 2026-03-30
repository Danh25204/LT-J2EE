package com.example.antique.service;

import com.example.antique.dto.ImportReceiptDTO;
import com.example.antique.dto.ImportReceiptDetailDTO;
import com.example.antique.entity.*;
import com.example.antique.repository.AntiqueRepository;
import com.example.antique.repository.ImportReceiptRepository;
import com.example.antique.repository.InventoryRepository;
import com.example.antique.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service xử lý nghiệp vụ phiếu nhập kho.
 * 
 * Workflow tạo phiếu nhập:
 * 1. Tạo ImportReceipt với mã tự động (PN-YYYYMM-XXX)
 * 2. Thêm từng ImportReceiptDetail
 * 3. Với mỗi detail: cập nhật Inventory.soLuongTon += soLuong
 * 4. Tính lại tổng giá trị phiếu
 * 
 * @Transactional: đảm bảo tất cả hoặc không có gì (all-or-nothing).
 */
@Service
@RequiredArgsConstructor
public class ImportReceiptService {

    private final ImportReceiptRepository importReceiptRepository;
    private final AntiqueRepository antiqueRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    /**
     * Lấy tất cả phiếu nhập, mới nhất trước.
     */
    @Transactional(readOnly = true)
    public List<ImportReceiptDTO> findAll() {
        return importReceiptRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tìm phiếu nhập theo ID.
     */
    @Transactional(readOnly = true)
    public ImportReceiptDTO findById(Long id) {
        ImportReceipt receipt = importReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập ID: " + id));
        return convertToDTO(receipt);
    }

    /**
     * Tạo phiếu nhập mới.
     * 
     * @Transactional: nếu bất kỳ bước nào lỗi → rollback toàn bộ.
     */
    @Transactional
    public ImportReceiptDTO create(ImportReceiptDTO dto) {
        // Kiểm tra trùng đồ cổ trong cùng phiếu
        Set<Long> antiqueIds = new HashSet<>();
        for (ImportReceiptDetailDTO detailDTO : dto.getDetails()) {
            if (!antiqueIds.add(detailDTO.getAntiqueId())) {
                throw new RuntimeException("Trùng đồ cổ ID: " + detailDTO.getAntiqueId() + " trong phiếu nhập!");
            }
        }

        // 1. Tạo entity ImportReceipt
        ImportReceipt receipt = new ImportReceipt();
        
        // Sinh mã phiếu tự động: PN-YYYYMM-XXX
        receipt.setMaPhieuNhap(generateMaPhieu(dto.getNgayNhap()));
        
        receipt.setNgayNhap(dto.getNgayNhap() != null ? dto.getNgayNhap() : LocalDate.now());
        receipt.setNguonGoc(dto.getNguonGoc());
        receipt.setGhiChu(dto.getGhiChu());
        receipt.setTrangThai(TrangThaiPhieuNhap.NHAP_KHO);
        
        // Lấy user từ userId
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user ID: " + dto.getUserId()));
        receipt.setUser(user);
        
        // 2. Thêm từng chi tiết
        for (ImportReceiptDetailDTO detailDTO : dto.getDetails()) {
            ImportReceiptDetail detail = new ImportReceiptDetail();
            
            // Lấy antique
            Antique antique = antiqueRepository.findById(detailDTO.getAntiqueId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đồ cổ ID: " + detailDTO.getAntiqueId()));
            detail.setAntique(antique);
            
            detail.setSoLuong(detailDTO.getSoLuong());
            detail.setDonGia(detailDTO.getDonGia());
            detail.setGhiChu(detailDTO.getGhiChu());
            
            // Tính thành tiền
            detail.tinhThanhTien();
            
            // Link detail với receipt
            detail.setImportReceipt(receipt);
            receipt.getDetails().add(detail);
            
            // 3. CẬP NHẬT TỒN KHO: soLuongTon += soLuong
            Inventory inventory = inventoryRepository.findByAntiqueId(antique.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy inventory cho đồ cổ: " + antique.getTenDocCo()));
            
            inventory.setSoLuongTon(inventory.getSoLuongTon() + detailDTO.getSoLuong());
            inventoryRepository.save(inventory);
        }
        
        // 4. Tính tổng giá trị phiếu
        receipt.tinhLaiTongGiaTri();
        
        // 5. Lưu phiếu (cascade sẽ tự lưu details)
        ImportReceipt saved = importReceiptRepository.save(receipt);
        
        return convertToDTO(saved);
    }

    /**
     * Hủy phiếu nhập — HOÀN LẠI tồn kho (trừ soLuongTon).
     */
    @Transactional
    public void cancel(Long id) {
        ImportReceipt receipt = importReceiptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập ID: " + id));
        
        if (receipt.getTrangThai() == TrangThaiPhieuNhap.HUY) {
            throw new RuntimeException("Phiếu đã bị hủy trước đó!");
        }
        
        // Trừ lại tồn kho cho từng chi tiết
        for (ImportReceiptDetail detail : receipt.getDetails()) {
            Inventory inventory = inventoryRepository.findByAntiqueId(detail.getAntique().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy tồn kho cho: " + detail.getAntique().getTenDocCo()));
            inventory.setSoLuongTon(inventory.getSoLuongTon() - detail.getSoLuong());
            inventoryRepository.save(inventory);
        }
        
        receipt.setTrangThai(TrangThaiPhieuNhap.HUY);
        importReceiptRepository.save(receipt);
    }

    /**
     * Sinh mã phiếu nhập tự động: PN-YYYYMM-XXX
     * Ví dụ: PN-202603-001, PN-202603-002,...
     */
    private String generateMaPhieu(LocalDate ngayNhap) {
        LocalDate date = ngayNhap != null ? ngayNhap : LocalDate.now();
        String yearMonth = date.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String prefix = "PN-" + yearMonth + "-";
        
        // Tìm số thứ tự lớn nhất đã dùng (tránh trùng khi có phiếu bị hủy)
        Optional<Integer> maxSeq = importReceiptRepository.findAll()
                .stream()
                .filter(r -> r.getMaPhieuNhap().startsWith(prefix))
                .map(r -> {
                    String seq = r.getMaPhieuNhap().substring(prefix.length());
                    try { return Integer.parseInt(seq); } catch (NumberFormatException e) { return 0; }
                })
                .max(Integer::compareTo);
        
        int nextSeq = maxSeq.orElse(0) + 1;
        return prefix + String.format("%03d", nextSeq);
    }

    /**
     * Convert Entity → DTO.
     */
    private ImportReceiptDTO convertToDTO(ImportReceipt receipt) {
        ImportReceiptDTO dto = new ImportReceiptDTO();
        dto.setId(receipt.getId());
        dto.setMaPhieuNhap(receipt.getMaPhieuNhap());
        dto.setNgayNhap(receipt.getNgayNhap());
        dto.setNguonGoc(receipt.getNguonGoc());
        dto.setTongGiaTri(receipt.getTongGiaTri());
        dto.setGhiChu(receipt.getGhiChu());
        dto.setTrangThai(receipt.getTrangThai());
        dto.setUserId(receipt.getUser().getId());
        dto.setUserName(receipt.getUser().getUsername());
        dto.setCreatedAt(receipt.getCreatedAt());
        
        // Convert details
        List<ImportReceiptDetailDTO> detailDTOs = receipt.getDetails().stream()
                .map(this::convertDetailToDTO)
                .collect(Collectors.toList());
        dto.setDetails(detailDTOs);
        
        return dto;
    }

    /**
     * Convert Detail Entity → DTO.
     */
    private ImportReceiptDetailDTO convertDetailToDTO(ImportReceiptDetail detail) {
        ImportReceiptDetailDTO dto = new ImportReceiptDetailDTO();
        dto.setId(detail.getId());
        dto.setImportReceiptId(detail.getImportReceipt().getId());
        dto.setAntiqueId(detail.getAntique().getId());
        dto.setAntiqueName(detail.getAntique().getTenDocCo());
        dto.setAntiqueImage(detail.getAntique().getAnhChinh());
        dto.setSoLuong(detail.getSoLuong());
        dto.setDonGia(detail.getDonGia());
        dto.setThanhTien(detail.getThanhTien());
        dto.setGhiChu(detail.getGhiChu());
        return dto;
    }

    /**
     * Lấy tất cả đồ cổ (dùng cho form tạo phiếu nhập).
     */
    @Transactional(readOnly = true)
    public List<Antique> findAllAntiques() {
        return antiqueRepository.findAll();
    }

    /**
     * Tìm user theo username (dùng cho controller).
     */
    @Transactional(readOnly = true)
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));
    }

}
