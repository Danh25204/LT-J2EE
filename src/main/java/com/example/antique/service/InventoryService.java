package com.example.antique.service;

import com.example.antique.dto.InventoryDTO;
import com.example.antique.entity.Antique;
import com.example.antique.entity.Inventory;
import com.example.antique.repository.AntiqueRepository;
import com.example.antique.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service quản lý tồn kho.
 * 
 * Chức năng:
 * - Xem danh sách tồn kho (tất cả đồ cổ + số lượng tồn)
 * - Cập nhật vị trí lưu trữ, ghi chú
 * - KHÔNG cho phép tạo mới Inventory (tự động tạo khi tạo Antique)
 * - KHÔNG cho phép sửa số lượng trực tiếp (chỉ qua Import/Export Receipt)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final AntiqueRepository antiqueRepository;

    /**
     * Lấy tất cả tồn kho (join Antique).
     * Sắp xếp: hết hàng → sắp hết → còn hàng.
     */
    public List<InventoryDTO> getAllInventory() {
        List<Inventory> inventories = inventoryRepository.findAll();
        
        return inventories.stream()
            .map(this::convertToDTO)
            .sorted((a, b) -> {
                // Sắp xếp: HET -> THAP -> TOT
                String statusA = a.getStatusBadge();
                String statusB = b.getStatusBadge();
                if (statusA.equals(statusB)) {
                    return a.getTenDocCo().compareTo(b.getTenDocCo());
                }
                if (statusA.equals("HET")) return -1;
                if (statusB.equals("HET")) return 1;
                if (statusA.equals("THAP")) return -1;
                return 1;
            })
            .collect(Collectors.toList());
    }

    /**
     * Lấy inventory theo antique ID.
     */
    public Optional<InventoryDTO> getByAntiqueId(Long antiqueId) {
        return inventoryRepository.findByAntiqueId(antiqueId)
            .map(this::convertToDTO);
    }

    /**
     * Lấy inventory theo ID.
     */
    public Optional<InventoryDTO> getById(Long id) {
        return inventoryRepository.findById(id)
            .map(this::convertToDTO);
    }

    /**
     * Cập nhật vị trí lưu trữ và ghi chú.
     * KHÔNG cho phép sửa số lượng tồn (chỉ qua Import/Export).
     */
    @Transactional
    public void updateLocationAndNote(Long inventoryId, String viTriLuuTru, String ghiChu) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy inventory ID: " + inventoryId));
        
        inventory.setViTriLuuTru(viTriLuuTru);
        inventory.setGhiChu(ghiChu);
        
        inventoryRepository.save(inventory);
        
        log.info("✓ Cập nhật vị trí/ghi chú inventory ID={}: {} - {}", 
                 inventoryId, viTriLuuTru, ghiChu);
    }

    /**
     * Lấy danh sách sắp hết hàng (soLuongTon < soLuongMacDinh).
     */
    public List<InventoryDTO> getLowStockItems() {
        return getAllInventory().stream()
            .filter(dto -> "THAP".equals(dto.getStatusBadge()) || "HET".equals(dto.getStatusBadge()))
            .collect(Collectors.toList());
    }

    /**
     * Convert entity sang DTO.
     */
    private InventoryDTO convertToDTO(Inventory inventory) {
        Antique antique = inventory.getAntique();
        
        InventoryDTO dto = new InventoryDTO();
        dto.setInventoryId(inventory.getId());
        dto.setSoLuongTon(inventory.getSoLuongTon());
        dto.setViTriLuuTru(inventory.getViTriLuuTru());
        dto.setGhiChu(inventory.getGhiChu());
        dto.setUpdatedAt(inventory.getUpdatedAt());
        
        // Antique info
        dto.setAntiqueId(antique.getId());
        dto.setMaDocCo(antique.getMaDocCo());
        dto.setTenDocCo(antique.getTenDocCo());
        dto.setAnhChinh(antique.getAnhChinh());
        dto.setSoLuongMacDinh(antique.getSoLuongMacDinh());
        
        if (antique.getCategory() != null) {
            dto.setCategoryName(antique.getCategory().getTenLoai());
        }
        
        return dto;
    }
}
