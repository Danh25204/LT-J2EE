package com.example.antique.service;

import com.example.antique.dto.AntiqueDTO;
import com.example.antique.entity.Antique;
import com.example.antique.entity.Category;
import com.example.antique.entity.Inventory;
import com.example.antique.entity.TinhTrang;
import com.example.antique.repository.AntiqueRepository;
import com.example.antique.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;

/**
 * Service xử lý nghiệp vụ quản lý đồ cổ.
 * 
 * Chức năng chính:
 * 1. Tự động sinh mã đồ cổ unique: DC-2026-001, DC-2026-002,...
 * 2. CRUD đầy đủ với validation
 * 3. Upload/xóa ảnh tự động
 * 4. Tạo Inventory tương ứng khi tạo đồ cổ mới
 * 5. Tìm kiếm và lọc đa điều kiện
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AntiqueService {

    private final AntiqueRepository antiqueRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    /**
     * Tạo đồ cổ mới.
     * 
     * Quy trình:
     * 1. Validate categoryId tồn tại
     * 2. Sinh mã đồ cổ tự động (DC-YYYY-XXX)
     * 3. Upload ảnh (nếu có)
     * 4. Tạo entity Antique
     * 5. Tạo Inventory tương ứng (số lượng ban đầu = 0)
     * 6. Lưu DB
     * 
     * @param dto DTO từ form
     * @return Antique đã lưu
     */
    public Antique createAntique(AntiqueDTO dto) {
        log.info("→ Tạo đồ cổ mới: {}", dto.getTenDocCo());

        // 1. Validate và load Category
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Không tìm thấy danh mục ID: " + dto.getCategoryId()
                ));

        // 2. Sinh mã đồ cổ tự động
        String maDocCo = generateMaDocCo();

        // 3. Xử lý ảnh:
        // - Ưu tiên anhChinhFilename (đã upload qua AJAX)
        // - Fallback: anhChinhFile (upload qua multipart form)
        String anhChinh = null;
        if (dto.getAnhChinhFilename() != null && !dto.getAnhChinhFilename().trim().isEmpty()) {
            // Đã upload qua AJAX → dùng filename có sẵn
            anhChinh = dto.getAnhChinhFilename();
            log.info("  → Sử dụng ảnh đã upload: {}", anhChinh);
        } else if (dto.getAnhChinhFile() != null && !dto.getAnhChinhFile().isEmpty()) {
            // Upload qua multipart form
            anhChinh = fileStorageService.storeFile(dto.getAnhChinhFile());
            log.info("  → Upload ảnh từ form: {}", anhChinh);
        }

        // 4. Tạo entity Antique
        Antique antique = new Antique();
        antique.setMaDocCo(maDocCo);
        antique.setTenDocCo(dto.getTenDocCo());
        antique.setCategory(category);
        antique.setNamSanXuat(dto.getNamSanXuat());
        antique.setTrieuDai(dto.getTrieuDai());
        antique.setXuatXu(dto.getXuatXu());
        antique.setChatLieu(dto.getChatLieu());
        antique.setKichThuoc(dto.getKichThuoc());
        antique.setTinhTrang(dto.getTinhTrang());
        antique.setMoTa(dto.getMoTa());
        antique.setGiaNhap(dto.getGiaNhap());
        antique.setGiaBanDuKien(dto.getGiaBanDuKien());
        antique.setSoLuongMacDinh(dto.getSoLuongMacDinh());
        antique.setAnhChinh(anhChinh);

        // 5. Tạo Inventory tương ứng (số lượng ban đầu = soLuongMacDinh)
        Inventory inventory = new Inventory();
        inventory.setAntique(antique);
        // Lấy số lượng tồn ban đầu = số lượng mặc định (nếu null thì = 0)
        inventory.setSoLuongTon(dto.getSoLuongMacDinh() != null ? dto.getSoLuongMacDinh() : 0);
        inventory.setViTriLuuTru(null); // Chưa có vị trí
        antique.setInventory(inventory); // Set quan hệ 2 chiều

        // 6. Lưu DB (cascade sẽ tự lưu Inventory)
        Antique saved = antiqueRepository.save(antique);
        log.info("✓ Đã tạo đồ cổ: {} - {}", saved.getMaDocCo(), saved.getTenDocCo());

        return saved;
    }

    /**
     * Cập nhật đồ cổ.
     * 
     * Xử lý đặc biệt:
     * - Upload ảnh mới → xóa ảnh cũ
     * - Không upload ảnh mới → giữ ảnh cũ
     * - KHÔNG cho phép sửa mã đồ cổ (unique, đã sinh tự động)
     * - KHÔNG tự động cập nhật Inventory (cập nhật riêng qua module Tồn kho)
     */
    public Antique updateAntique(Long id, AntiqueDTO dto) {
        log.info("→ Cập nhật đồ cổ ID: {}", id);

        // 1. Load entity hiện tại
        Antique antique = antiqueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Không tìm thấy đồ cổ ID: " + id
                ));

        // 2. Validate và load Category mới (nếu đổi danh mục)
        if (!antique.getCategory().getId().equals(dto.getCategoryId())) {
            Category newCategory = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy danh mục ID: " + dto.getCategoryId()
                    ));
            antique.setCategory(newCategory);
        }

        // 3. Xử lý ảnh
        String oldAnhChinh = antique.getAnhChinh();
        
        // Ưu tiên 1: anhChinhFilename (đã upload qua AJAX)
        if (StringUtils.hasText(dto.getAnhChinhFilename())) {
            antique.setAnhChinh(dto.getAnhChinhFilename());
            // Xóa ảnh cũ (nếu có và khác ảnh mới)
            if (StringUtils.hasText(oldAnhChinh) && !oldAnhChinh.equals(dto.getAnhChinhFilename())) {
                fileStorageService.deleteFile(oldAnhChinh);
            }
        }
        // Ưu tiên 2: anhChinhFile (upload qua multipart form)
        else {
            MultipartFile newFile = dto.getAnhChinhFile();
            if (newFile != null && !newFile.isEmpty()) {
                String newAnhChinh = fileStorageService.storeFile(newFile);
                antique.setAnhChinh(newAnhChinh);
                if (StringUtils.hasText(oldAnhChinh)) {
                    fileStorageService.deleteFile(oldAnhChinh);
                }
            }
        }
        // Else: giữ nguyên ảnh cũ (không upload file mới)

        // 4. Cập nhật các trường khác
        antique.setTenDocCo(dto.getTenDocCo());
        antique.setNamSanXuat(dto.getNamSanXuat());
        antique.setTrieuDai(dto.getTrieuDai());
        antique.setXuatXu(dto.getXuatXu());
        antique.setChatLieu(dto.getChatLieu());
        antique.setKichThuoc(dto.getKichThuoc());
        antique.setTinhTrang(dto.getTinhTrang());
        antique.setMoTa(dto.getMoTa());
        antique.setGiaNhap(dto.getGiaNhap());
        antique.setGiaBanDuKien(dto.getGiaBanDuKien());
        antique.setSoLuongMacDinh(dto.getSoLuongMacDinh());

        // 5. Lưu DB (updatedAt tự động cập nhật)
        Antique updated = antiqueRepository.save(antique);
        log.info("✓ Đã cập nhật đồ cổ: {}", updated.getMaDocCo());

        return updated;
    }

    /**
     * Xóa đồ cổ.
     * 
     * Lưu ý:
     * - Cascade sẽ tự xóa Inventory
     * - Phải kiểm tra đồ cổ có trong phiếu nhập/xuất nào không?
     *   (Nếu có ImportReceiptDetail/ExportReceiptDetail → không cho xóa)
     * - Xóa file ảnh
     */
    public void deleteAntique(Long id) {
        log.info("→ Xóa đồ cổ ID: {}", id);

        Antique antique = antiqueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Không tìm thấy đồ cổ ID: " + id
                ));

        // Kiểm tra tồn kho — không cho xóa nếu còn hàng trong kho
        if (antique.getInventory() != null && antique.getInventory().getSoLuongTon() > 0) {
            throw new IllegalStateException(
                "Không thể xóa đồ cổ '" + antique.getTenDocCo()
                + "' vì còn " + antique.getInventory().getSoLuongTon()
                + " cái trong kho. Vui lòng xuất kho hết trước khi xóa!"
            );
        }

        // Xóa file ảnh (nếu có)
        if (StringUtils.hasText(antique.getAnhChinh())) {
            fileStorageService.deleteFile(antique.getAnhChinh());
        }

        // Xóa entity (cascade sẽ xóa Inventory)
        antiqueRepository.delete(antique);
        log.info("✓ Đã xóa đồ cổ: {}", antique.getMaDocCo());
    }

    /**
     * Tìm đồ cổ theo ID.
     */
    @Transactional(readOnly = true)
    public Antique findById(Long id) {
        return antiqueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Không tìm thấy đồ cổ ID: " + id
                ));
    }

    /**
     * Lấy tất cả đồ cổ, sắp xếp theo ngày tạo mới nhất.
     */
    @Transactional(readOnly = true)
    public List<Antique> findAll() {
        return antiqueRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Tìm kiếm đơn giản theo keyword (tên hoặc mã).
     */
    @Transactional(readOnly = true)
    public List<Antique> search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return findAll();
        }
        return antiqueRepository.searchByKeyword(keyword.trim());
    }

    /**
     * Lọc theo danh mục.
     */
    @Transactional(readOnly = true)
    public List<Antique> findByCategory(Long categoryId) {
        return antiqueRepository.findByCategoryId(categoryId);
    }

    /**
     * Lọc theo tình trạng.
     */
    @Transactional(readOnly = true)
    public List<Antique> findByTinhTrang(TinhTrang tinhTrang) {
        return antiqueRepository.findByTinhTrang(tinhTrang);
    }

    /**
     * Tự động sinh mã đồ cổ unique: DC-YYYY-XXX
     * 
     * Format: DC-<năm hiện tại>-<số thứ tự 3 chữ số>
     * VD: DC-2026-001, DC-2026-002,...
     * 
     * Logic:
     * 1. Lấy năm hiện tại (2026)
     * 2. Đếm số đồ cổ đã tạo trong năm này
     * 3. Số thứ tự = count + 1
     * 4. Format: DC-YYYY-XXX (XXX pad left 0, 3 digits)
     */
    private String generateMaDocCo() {
        int currentYear = Year.now().getValue();
        String prefix = "DC-" + currentYear + "-";

        // Đếm số đồ cổ có mã bắt đầu bằng "DC-2026-"
        long count = antiqueRepository.findAll().stream()
                .filter(a -> a.getMaDocCo().startsWith(prefix))
                .count();

        // Số thứ tự tiếp theo
        int nextNumber = (int) (count + 1);

        // Format: 001, 002, ..., 010, ..., 100,...
        String maDocCo = prefix + String.format("%03d", nextNumber);

        log.debug("→ Sinh mã đồ cổ: {}", maDocCo);
        return maDocCo;
    }

    /**
     * Chuyển đổi entity sang DTO (dùng khi edit).
     */
    public AntiqueDTO toDTO(Antique antique) {
        AntiqueDTO dto = new AntiqueDTO();
        dto.setId(antique.getId());
        dto.setMaDocCo(antique.getMaDocCo());
        dto.setTenDocCo(antique.getTenDocCo());
        dto.setCategoryId(antique.getCategory().getId());
        dto.setNamSanXuat(antique.getNamSanXuat());
        dto.setTrieuDai(antique.getTrieuDai());
        dto.setXuatXu(antique.getXuatXu());
        dto.setChatLieu(antique.getChatLieu());
        dto.setKichThuoc(antique.getKichThuoc());
        dto.setTinhTrang(antique.getTinhTrang());
        dto.setMoTa(antique.getMoTa());
        dto.setGiaNhap(antique.getGiaNhap());
        dto.setGiaBanDuKien(antique.getGiaBanDuKien());
        dto.setSoLuongMacDinh(antique.getSoLuongMacDinh());
        dto.setAnhChinhCurrent(antique.getAnhChinh()); // Tên file ảnh hiện tại
        return dto;
    }
}
