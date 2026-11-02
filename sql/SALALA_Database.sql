/* =========================================================
   DATABASE: SALALA
   HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG / PHÒNG KHÁM THÚ Y
   (Cập nhật: Xóa bỏ bảng LuongChucVu, gán lương cơ bản trực tiếp)
   ========================================================= */

USE master;
GO

IF DB_ID('SALALA') IS NOT NULL
BEGIN
    ALTER DATABASE SALALA SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE SALALA;
END
GO

CREATE DATABASE SALALA;
GO

USE SALALA;
GO

/* =========================================================
   PHẦN 1: TẠO BẢNG
   ========================================================= */

-- 1. TÀI KHOẢN 
CREATE TABLE TaiKhoan
(
    maTK         VARCHAR(20)  NOT NULL,
    tenDangNhap  VARCHAR(50)  NOT NULL,
    matKhau      VARCHAR(255) NOT NULL,
    vaiTro       NVARCHAR(50) NOT NULL,
    trangThai    BIT          NOT NULL DEFAULT 1,
    maQuanLy     VARCHAR(20)  NULL, 

    CONSTRAINT PK_TaiKhoan PRIMARY KEY (maTK),
    CONSTRAINT UQ_TaiKhoan_TenDangNhap UNIQUE (tenDangNhap)
);
GO

-- 2. QUẢN LÝ CỬA HÀNG
CREATE TABLE QuanLyCuaHang
(
    maQuanLy     VARCHAR(20)   NOT NULL,
    maTK         VARCHAR(20)   NOT NULL,
    hoTenQuanLy  NVARCHAR(100) NOT NULL,
    sdt          VARCHAR(20)   NOT NULL,
    diaChi       NVARCHAR(255),
    ngaySinh     DATE,
    gioiTinh     NVARCHAR(20),
    luongCoBan   DECIMAL(18,2) NOT NULL, -- Lương tháng cố định

    CONSTRAINT PK_QuanLyCuaHang PRIMARY KEY (maQuanLy),
    CONSTRAINT UQ_QuanLyCuaHang_TaiKhoan UNIQUE (maTK),
    CONSTRAINT FK_QuanLyCuaHang_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK),
    CONSTRAINT CK_QuanLyCuaHang_Luong CHECK (luongCoBan >= 0)
);
GO

ALTER TABLE TaiKhoan
ADD CONSTRAINT FK_TaiKhoan_QuanLy FOREIGN KEY (maQuanLy) REFERENCES QuanLyCuaHang(maQuanLy);
GO

-- 3. NHÂN VIÊN
CREATE TABLE NhanVien
(
    maNhanVien   VARCHAR(20)   NOT NULL,
    maTK         VARCHAR(20)   NULL,
    hoTen        NVARCHAR(100) NOT NULL,
    chucVu       NVARCHAR(50)  NOT NULL,
    sdt          VARCHAR(20)   NOT NULL,
    diaChi       NVARCHAR(255),
    ngaySinh     DATE,
    gioiTinh     NVARCHAR(20),
    luongCoBan   DECIMAL(18,2) NOT NULL, -- Lương theo giờ (vài chục ngàn) hoặc lương tháng (mười mấy triệu)

    CONSTRAINT PK_NhanVien PRIMARY KEY (maNhanVien),
    CONSTRAINT FK_NhanVien_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK),
    CONSTRAINT CK_NhanVien_Luong CHECK (luongCoBan >= 0)
);
GO
CREATE UNIQUE INDEX UQ_NhanVien_TaiKhoan ON NhanVien(maTK) WHERE maTK IS NOT NULL;
GO

-- CHUYÊN NGÀNH BÁC SĨ 
CREATE TABLE ChuyenNganh
(
    maChuyenNganh  VARCHAR(20)   NOT NULL,
    tenChuyenNganh NVARCHAR(100) NOT NULL,

    CONSTRAINT PK_ChuyenNganh PRIMARY KEY (maChuyenNganh)
);
GO

-- 4. BÁC SĨ THÚ Y
CREATE TABLE BacSyThuY
(
    maBacSi          VARCHAR(20)   NOT NULL,
    maTK             VARCHAR(20)   NULL,
    maChuyenNganh    VARCHAR(20)   NOT NULL, 
    hoTen            NVARCHAR(100) NOT NULL,
    gioiTinh         NVARCHAR(20),
    sdt              VARCHAR(20)   NOT NULL,
    email            VARCHAR(100)  NOT NULL,
    ngaySinh         DATE,
    luongThang       DECIMAL(18,2) NOT NULL CONSTRAINT DF_BacSyThuY_LuongThang DEFAULT 0, -- Lương tháng cố định

    CONSTRAINT PK_BacSyThuY PRIMARY KEY (maBacSi),
    CONSTRAINT FK_BacSyThuY_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK),
    CONSTRAINT FK_BacSyThuY_ChuyenNganh FOREIGN KEY (maChuyenNganh) REFERENCES ChuyenNganh(maChuyenNganh),
    CONSTRAINT CK_BacSyThuY_Luong CHECK (luongThang >= 0)
);
GO
CREATE UNIQUE INDEX UQ_BacSyThuY_TaiKhoan ON BacSyThuY(maTK) WHERE maTK IS NOT NULL;
GO

-- 5. KHÁCH HÀNG
CREATE TABLE KhachHang
(
    maKH    VARCHAR(20)   NOT NULL,
    hoTen   NVARCHAR(100) NOT NULL,
    sdt     VARCHAR(20)   NOT NULL,
    email   VARCHAR(100)  NOT NULL,
    diaChi  NVARCHAR(255),

    CONSTRAINT PK_KhachHang PRIMARY KEY (maKH)
);
GO

-- 6. THÚ CƯNG
CREATE TABLE ThuCung
(
    maTC              VARCHAR(20)   NOT NULL,
    maKH              VARCHAR(20)   NOT NULL,
    tenTC             NVARCHAR(100) NOT NULL,
    loai              NVARCHAR(50)  NOT NULL,
    gioiTinh          NVARCHAR(20),
    ngaySinh          DATE,
    chuHo             NVARCHAR(100) NOT NULL,
    dacDiemNhanDien   NVARCHAR(255),

    CONSTRAINT PK_ThuCung PRIMARY KEY (maTC),
    CONSTRAINT FK_ThuCung_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
);
GO

-- 7. LỊCH LÀM VIỆC
CREATE TABLE LichLamViec
(
    maLLV        VARCHAR(20) NOT NULL,
    maNhanVien   VARCHAR(20) NOT NULL,
    ngay         DATE,
    gioBatDau    TIME,
    gioKetThuc   TIME,
    trangThai    NVARCHAR(50),

    CONSTRAINT PK_LichLamViec PRIMARY KEY (maLLV),
    CONSTRAINT FK_LichLamViec_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT CK_LichLamViec_Gio CHECK (gioKetThuc IS NULL OR gioBatDau IS NULL OR gioKetThuc > gioBatDau)
);
GO

-- 8. HỒ SƠ SỨC KHỎE
CREATE TABLE HoSoSucKhoe
(
    maHSSK      VARCHAR(20)   NOT NULL,
    maTC        VARCHAR(20)   NOT NULL,
    maBacSi     VARCHAR(20)   NOT NULL,
    ngayKham    DATETIME2,
    trieuChung  NVARCHAR(500),
    chuanDoan   NVARCHAR(500),
    canNang     DECIMAL(10,2),

    CONSTRAINT PK_HoSoSucKhoe PRIMARY KEY (maHSSK),
    CONSTRAINT FK_HoSoSucKhoe_ThuCung FOREIGN KEY (maTC) REFERENCES ThuCung(maTC),
    CONSTRAINT FK_HoSoSucKhoe_BacSy FOREIGN KEY (maBacSi) REFERENCES BacSyThuY(maBacSi),
    CONSTRAINT CK_HoSoSucKhoe_CanNang CHECK (canNang IS NULL OR canNang >= 0)
);
GO

-- 9. THUỐC
CREATE TABLE Thuoc
(
    maThuoc         VARCHAR(20)   NOT NULL,
    tenThuoc        NVARCHAR(100) NOT NULL,
    donViTinh       NVARCHAR(30)  NOT NULL,
    huongDanSuDung  NVARCHAR(500),

    CONSTRAINT PK_Thuoc PRIMARY KEY (maThuoc)
);
GO

-- 10. ĐƠN THUỐC
CREATE TABLE DonThuoc
(
    maDonThuoc  VARCHAR(20)   NOT NULL,
    maTC        VARCHAR(20)   NOT NULL,
    maBacSi     VARCHAR(20)   NOT NULL,
    maHSSK      VARCHAR(20)   NOT NULL,
    ngayKeDon   DATETIME2     NOT NULL,
    ghiChu      NVARCHAR(500),
    trangThai   NVARCHAR(50)  NOT NULL,

    CONSTRAINT PK_DonThuoc PRIMARY KEY (maDonThuoc),
    CONSTRAINT FK_DonThuoc_ThuCung FOREIGN KEY (maTC) REFERENCES ThuCung(maTC),
    CONSTRAINT FK_DonThuoc_BacSy FOREIGN KEY (maBacSi) REFERENCES BacSyThuY(maBacSi),
    CONSTRAINT FK_DonThuoc_HoSoSucKhoe FOREIGN KEY (maHSSK) REFERENCES HoSoSucKhoe(maHSSK)
);
GO

-- 11. CHI TIẾT ĐƠN THUỐC
CREATE TABLE ChiTietDonThuoc
(
    maCTDT      VARCHAR(20)   NOT NULL,
    maDonThuoc  VARCHAR(20)   NOT NULL,
    maThuoc     VARCHAR(20)   NOT NULL,
    soLuong     DECIMAL(18,2) NOT NULL,
    lieuLuong   DECIMAL(18,2) NOT NULL,

    CONSTRAINT PK_ChiTietDonThuoc PRIMARY KEY (maCTDT),
    CONSTRAINT FK_ChiTietDonThuoc_DonThuoc FOREIGN KEY (maDonThuoc) REFERENCES DonThuoc(maDonThuoc),
    CONSTRAINT FK_ChiTietDonThuoc_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc),
    CONSTRAINT CK_ChiTietDonThuoc_SoLuong CHECK (soLuong > 0),
    CONSTRAINT CK_ChiTietDonThuoc_LieuLuong CHECK (lieuLuong > 0)
);
GO

-- 12. DỊCH VỤ
CREATE TABLE DichVu
(
    maDV       VARCHAR(20)   NOT NULL,
    tenDV      NVARCHAR(100) NOT NULL,
    moTa       NVARCHAR(500),
    giaDV      DECIMAL(18,2) NOT NULL,
    thoiGian   TIME          NOT NULL,
    trangThai  NVARCHAR(50)  NOT NULL,

    CONSTRAINT PK_DichVu PRIMARY KEY (maDV),
    CONSTRAINT CK_DichVu_Gia CHECK (giaDV >= 0)
);
GO

-- 13. LỊCH HẸN
CREATE TABLE LichHen
(
    maLichHen   VARCHAR(20)   NOT NULL,
    maTC        VARCHAR(20)   NOT NULL,
    maKH        VARCHAR(20)   NOT NULL,
    maNhanVien  VARCHAR(20)   NOT NULL,
    maDV        VARCHAR(20)   NULL,
    ngayGio     DATETIME2     NOT NULL,
    lyDo        NVARCHAR(255) NOT NULL,
    trangThai   NVARCHAR(50)  NOT NULL,
    ghiChu      NVARCHAR(500),
    loaiHen     NVARCHAR(100) NOT NULL,

    CONSTRAINT PK_LichHen PRIMARY KEY (maLichHen),
    CONSTRAINT FK_LichHen_ThuCung FOREIGN KEY (maTC) REFERENCES ThuCung(maTC),
    CONSTRAINT FK_LichHen_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_LichHen_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_LichHen_DichVu FOREIGN KEY (maDV) REFERENCES DichVu(maDV)
);
GO

-- 14. KHUYẾN MÃI
CREATE TABLE KhuyenMai
(
    maKM         VARCHAR(20)   NOT NULL,
    tenKM        NVARCHAR(100) NOT NULL,
    mucKM        DECIMAL(18,2) NOT NULL,
    ngayBatDau   DATETIME2     NOT NULL,
    ngayKetThuc  DATETIME2     NOT NULL,
    dieuKien     NVARCHAR(500),

    CONSTRAINT PK_KhuyenMai PRIMARY KEY (maKM),
    CONSTRAINT CK_KhuyenMai_Muc CHECK (mucKM > 0 AND mucKM <= 100),
    CONSTRAINT CK_KhuyenMai_Ngay CHECK (ngayKetThuc >= ngayBatDau)
);
GO

-- 15. THUẾ
CREATE TABLE Thue
(
    maThue   VARCHAR(20)   NOT NULL,
    tenThue  NVARCHAR(100) NOT NULL,
    mucThue  DECIMAL(18,2) NOT NULL,

    CONSTRAINT PK_Thue PRIMARY KEY (maThue),
    CONSTRAINT CK_Thue_Muc CHECK (mucThue >= 0)
);
GO

-- 16. THANH TOÁN
CREATE TABLE ThanhToan
(
    maThanhToan  VARCHAR(20)   NOT NULL,
    soTien       DECIMAL(18,2) NOT NULL,
    thoiGian     DATETIME2,
    phuongThuc   NVARCHAR(50)  NOT NULL,
    trangThai    NVARCHAR(50)  NOT NULL,

    CONSTRAINT PK_ThanhToan PRIMARY KEY (maThanhToan),
    CONSTRAINT CK_ThanhToan_SoTien CHECK (soTien >= 0)
);
GO

-- 17. HÓA ĐƠN
CREATE TABLE HoaDon
(
    maHD         VARCHAR(20)  NOT NULL,
    ngayLap      DATETIME2    NOT NULL,
    trangThai    NVARCHAR(50) NOT NULL,
    maKH         VARCHAR(20)  NOT NULL,
    maNhanVien   VARCHAR(20)  NOT NULL,
    maKM         VARCHAR(20)  NULL,
    maThue       VARCHAR(20)  NULL,
    maThanhToan  VARCHAR(20)  NULL,

    CONSTRAINT PK_HoaDon PRIMARY KEY (maHD),
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_HoaDon_KhuyenMai FOREIGN KEY (maKM) REFERENCES KhuyenMai(maKM),
    CONSTRAINT FK_HoaDon_Thue FOREIGN KEY (maThue) REFERENCES Thue(maThue),
    CONSTRAINT FK_HoaDon_ThanhToan FOREIGN KEY (maThanhToan) REFERENCES ThanhToan(maThanhToan)
);
GO

-- 18. NHÀ CUNG CẤP
CREATE TABLE NhaCungCap
(
    maNCC   VARCHAR(20)   NOT NULL,
    tenNCC  NVARCHAR(100) NOT NULL,
    diaChi  NVARCHAR(255) NOT NULL,
    sdt     VARCHAR(20)   NOT NULL,
    email   VARCHAR(100)  NOT NULL,

    CONSTRAINT PK_NhaCungCap PRIMARY KEY (maNCC)
);
GO

-- 19. VẬT TƯ
CREATE TABLE VatTu
(
    maVT         VARCHAR(20)   NOT NULL,
    maNCC        VARCHAR(20)   NOT NULL,
    tenVT        NVARCHAR(100) NOT NULL,
    soLuongTon   DECIMAL(18,2) NOT NULL,
    trangThai    NVARCHAR(50)  NOT NULL,
    giaBanCoBan  DECIMAL(18,2) NOT NULL,
    giaNhap      DECIMAL(18,2) NOT NULL,

    CONSTRAINT PK_VatTu PRIMARY KEY (maVT),
    CONSTRAINT FK_VatTu_NhaCungCap FOREIGN KEY (maNCC) REFERENCES NhaCungCap(maNCC),
    CONSTRAINT CK_VatTu_SoLuong CHECK (soLuongTon >= 0),
    CONSTRAINT CK_VatTu_GiaBan CHECK (giaBanCoBan >= 0),
    CONSTRAINT CK_VatTu_GiaNhap CHECK (giaNhap >= 0)
);
GO

-- 20. CHI TIẾT HÓA ĐƠN
CREATE TABLE ChiTietHoaDon
(
    maCTHD     VARCHAR(20)   NOT NULL,
    maHD       VARCHAR(20)   NOT NULL,
    maDV       VARCHAR(20)   NULL,
    maVT       VARCHAR(20)   NULL,
    maNhanVien VARCHAR(20)   NULL,
    maBacSi    VARCHAR(20)   NULL,
    soLuong    DECIMAL(18,2) NOT NULL,
    donGia     DECIMAL(18,2) NOT NULL,

    CONSTRAINT PK_ChiTietHoaDon PRIMARY KEY (maCTHD),
    CONSTRAINT FK_ChiTietHoaDon_HoaDon FOREIGN KEY (maHD) REFERENCES HoaDon(maHD),
    CONSTRAINT FK_ChiTietHoaDon_DichVu FOREIGN KEY (maDV) REFERENCES DichVu(maDV),
    CONSTRAINT FK_ChiTietHoaDon_VatTu FOREIGN KEY (maVT) REFERENCES VatTu(maVT),
    CONSTRAINT FK_ChiTietHoaDon_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_ChiTietHoaDon_BacSy FOREIGN KEY (maBacSi) REFERENCES BacSyThuY(maBacSi),
    CONSTRAINT CK_ChiTietHoaDon_SoLuong CHECK (soLuong > 0),
    CONSTRAINT CK_ChiTietHoaDon_DonGia CHECK (donGia >= 0),
    CONSTRAINT CK_ChiTietHoaDon_DV_hoac_VT
        CHECK ((CASE WHEN maDV IS NULL THEN 0 ELSE 1 END) + (CASE WHEN maVT IS NULL THEN 0 ELSE 1 END) = 1)
);
GO

-- 21. PHIẾU XUẤT KHO
CREATE TABLE PhieuXuatKho
(
    maPXK       VARCHAR(20)  NOT NULL,
    ngayXuat    DATETIME2    NOT NULL,
    ghiChu      NVARCHAR(500),
    trangThai   NVARCHAR(50) NOT NULL,
    maNhanVien  VARCHAR(20)  NOT NULL,

    CONSTRAINT PK_PhieuXuatKho PRIMARY KEY (maPXK),
    CONSTRAINT FK_PhieuXuatKho_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);
GO

-- 22. CHI TIẾT PHIẾU XUẤT KHO
CREATE TABLE ChiTietPhieuXuatKho
(
    maCTPXK  VARCHAR(20)   NOT NULL,
    maPXK    VARCHAR(20)   NOT NULL,
    maVT     VARCHAR(20)   NOT NULL,
    soLuong  DECIMAL(18,2) NOT NULL,

    CONSTRAINT PK_ChiTietPhieuXuatKho PRIMARY KEY (maCTPXK),
    CONSTRAINT FK_ChiTietPXK_PhieuXuatKho FOREIGN KEY (maPXK) REFERENCES PhieuXuatKho(maPXK),
    CONSTRAINT FK_ChiTietPXK_VatTu FOREIGN KEY (maVT) REFERENCES VatTu(maVT),
    CONSTRAINT CK_ChiTietPXK_SoLuong CHECK (soLuong > 0)
);
GO

-- 23. PHIẾU NHẬP KHO
CREATE TABLE PhieuNhapKho
(
    maPNK       VARCHAR(20)  NOT NULL,
    ngayNhap    DATETIME2    NOT NULL,
    ghiChu      NVARCHAR(500),
    trangThai   NVARCHAR(50) NOT NULL,
    maNhanVien  VARCHAR(20)  NOT NULL,
    maNCC       VARCHAR(20)  NOT NULL,

    CONSTRAINT PK_PhieuNhapKho PRIMARY KEY (maPNK),
    CONSTRAINT FK_PhieuNhapKho_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_PhieuNhapKho_NhaCungCap FOREIGN KEY (maNCC) REFERENCES NhaCungCap(maNCC)
);
GO

-- 24. CHI TIẾT PHIẾU NHẬP KHO
CREATE TABLE ChiTietPhieuNhapKho
(
    maCTPNK  VARCHAR(20)   NOT NULL,
    maPNK    VARCHAR(20)   NOT NULL,
    maVT     VARCHAR(20)   NOT NULL,
    donGia   DECIMAL(18,2) NOT NULL,
    soLuong  DECIMAL(18,2) NOT NULL,

    CONSTRAINT PK_ChiTietPhieuNhapKho PRIMARY KEY (maCTPNK),
    CONSTRAINT FK_ChiTietPNK_PhieuNhapKho FOREIGN KEY (maPNK) REFERENCES PhieuNhapKho(maPNK),
    CONSTRAINT FK_ChiTietPNK_VatTu FOREIGN KEY (maVT) REFERENCES VatTu(maVT),
    CONSTRAINT CK_ChiTietPNK_DonGia CHECK (donGia >= 0),
    CONSTRAINT CK_ChiTietPNK_SoLuong CHECK (soLuong > 0)
);
GO

-- 25. QUẢN LÝ HẠNG THÀNH VIÊN
CREATE TABLE QuanLyHangThanhVien
(
    maHang            VARCHAR(20)   NOT NULL,
    maKH              VARCHAR(20)   NOT NULL,
    tenHang           NVARCHAR(50)  NOT NULL,
    chiTieuToiThieu   DECIMAL(18,2) NOT NULL,
    chiTieuToiDa      DECIMAL(18,2) NULL,
    mucGiamGia        DECIMAL(5,2)  NOT NULL,
    diemTichLuy1Van   DECIMAL(4,1)  NOT NULL,
    quyenLoi          NVARCHAR(500) NOT NULL,
    tongChiTieu       DECIMAL(18,2) NOT NULL DEFAULT 0,
    diemTichLuy       INT           NOT NULL DEFAULT 0,
    ngayCapNhatHang   DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
    trangThai         NVARCHAR(50)  NOT NULL DEFAULT N'Đang áp dụng',

    CONSTRAINT PK_QuanLyHangThanhVien PRIMARY KEY (maHang),
    CONSTRAINT UQ_QuanLyHangThanhVien_KhachHang UNIQUE (maKH),
    CONSTRAINT FK_QuanLyHangThanhVien_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT CK_QuanLyHangThanhVien_TenHang CHECK (tenHang IN (N'Đồng', N'Bạc', N'Vàng', N'Kim cương')),
    CONSTRAINT CK_QuanLyHangThanhVien_ChiTieu CHECK (chiTieuToiThieu >= 0 AND (chiTieuToiDa IS NULL OR chiTieuToiDa > chiTieuToiThieu)),
    CONSTRAINT CK_QuanLyHangThanhVien_GiamGia CHECK (mucGiamGia BETWEEN 0 AND 100),
    CONSTRAINT CK_QuanLyHangThanhVien_Diem CHECK (diemTichLuy1Van >= 0 AND diemTichLuy >= 0),
    CONSTRAINT CK_QuanLyHangThanhVien_TongChiTieu CHECK (tongChiTieu >= 0)
);
GO


/* =========================================================
   PHẦN 2: VIEW TỔNG TIỀN HÓA ĐƠN
   ========================================================= */
CREATE VIEW vw_TongTienHoaDon AS
SELECT  h.maHD,
        SUM(c.soLuong * c.donGia)                       AS tongTienHang,
        ISNULL(km.mucKM, 0)                             AS mucKM,
        ISNULL(t.mucThue, 0)                            AS mucThue,
        CAST(SUM(c.soLuong * c.donGia)
             * (1 - ISNULL(km.mucKM, 0) / 100.0)
             * (1 + ISNULL(t.mucThue, 0) / 100.0) AS DECIMAL(18,2)) AS tongThanhToan
FROM HoaDon h
JOIN ChiTietHoaDon c   ON c.maHD = h.maHD
LEFT JOIN KhuyenMai km ON km.maKM = h.maKM
LEFT JOIN Thue t       ON t.maThue = h.maThue
GROUP BY h.maHD, km.mucKM, t.mucThue;
GO


/* =========================================================
   PHẦN 3: DỮ LIỆU MẪU ĐƯỢC CẬP NHẬT 
   ========================================================= */

CREATE FUNCTION dbo.fn_C3(@n INT) RETURNS VARCHAR(3)
AS BEGIN RETURN RIGHT('000' + CAST(@n AS VARCHAR(3)), 3); END
GO

-- Bảng đếm tạo Hóa đơn
CREATE TABLE #N (n INT PRIMARY KEY);
WITH x AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM x WHERE n < 200)
INSERT INTO #N SELECT n FROM x OPTION (MAXRECURSION 0);
GO

-- 1. TÀI KHOẢN (20 Tài khoản chuẩn cũ)
INSERT INTO TaiKhoan (maTK, tenDangNhap, matKhau, vaiTro, trangThai, maQuanLy)
SELECT 'TK' + dbo.fn_C3(n),
       CASE 
            WHEN n = 1 THEN 'admin'
            WHEN n <= 3 THEN 'quanly' + RIGHT('0' + CAST(n - 1 AS VARCHAR(2)), 2)       
            WHEN n <= 15 THEN 'nhanvien' + RIGHT('0' + CAST(n - 3 AS VARCHAR(2)), 2)    
            WHEN n <= 20 THEN 'bacsi' + RIGHT('0' + CAST(n - 15 AS VARCHAR(2)), 2)      
       END,
       CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', '123456'), 2),
       CASE 
            WHEN n <= 3 THEN N'Quản lý'
            WHEN n <= 8 THEN N'Nhân viên bán hàng'
            WHEN n <= 13 THEN N'Nhân viên dịch vụ'
            WHEN n <= 15 THEN N'Nhân viên quản lý kho'
            WHEN n <= 20 THEN N'Bác sĩ' 
       END,
       1,
       NULL 
FROM #N WHERE n <= 20;
GO

-- 2. QUẢN LÝ CỬA HÀNG 
INSERT INTO QuanLyCuaHang (maQuanLy, maTK, hoTenQuanLy, sdt, diaChi, ngaySinh, gioiTinh, luongCoBan)
SELECT 'QL' + dbo.fn_C3(n), 'TK' + dbo.fn_C3(n),
       CHOOSE(n, N'Lê Văn Thành (Admin)', N'Nguyễn Văn An', N'Trần Thị Bình'),
       '0901000' + dbo.fn_C3(n),
       N'Quận Gò Vấp, TP.HCM',
       DATEADD(DAY, n * 150, '19850510'),
       CASE WHEN n % 2 = 1 THEN N'Nam' ELSE N'Nữ' END,
       18000000 -- Lương tháng cho quản lý
FROM #N WHERE n <= 3;
GO

UPDATE TaiKhoan
SET maQuanLy = 'QL002'
WHERE vaiTro <> N'Quản lý' OR tenDangNhap = 'admin';
GO

-- 3. NHÂN VIÊN (Gán trực tiếp lương theo giờ hoặc lương tháng dựa vào chức vụ)
WITH NhanVien_Temp AS (
    SELECT n, 
           CASE 
                WHEN n <= 5 THEN N'Nhân viên bán hàng'
                WHEN n <= 10 THEN N'Nhân viên dịch vụ'
                ELSE N'Nhân viên quản lý kho' 
           END AS chucVu
    FROM #N WHERE n <= 12
)
INSERT INTO NhanVien (maNhanVien, maTK, hoTen, chucVu, sdt, diaChi, ngaySinh, gioiTinh, luongCoBan)
SELECT 'NV' + dbo.fn_C3(nvt.n), 
       'TK' + dbo.fn_C3(nvt.n + 3),
       CHOOSE(nvt.n, N'Trần Quốc Bảo', N'Nguyễn Thị Cẩm', N'Lê Văn Dũng', N'Phạm Hồng Em', N'Võ Văn Phúc',
                     N'Đặng Thị Giang', N'Bùi Minh Hải', N'Hồ Thị Hạnh', N'Ngô Văn Khoa', N'Dương Thị Linh',
                     N'Lý Quang Minh', N'Phan Thị Ngân'),
       nvt.chucVu,
       '0902000' + dbo.fn_C3(nvt.n),
       N'Quận Gò Vấp, TP.HCM',
       DATEADD(DAY, nvt.n * 263, '19950115'),
       CASE WHEN nvt.n % 2 = 1 THEN N'Nam' ELSE N'Nữ' END,
       CASE nvt.chucVu
            WHEN N'Nhân viên bán hàng' THEN 45000       -- Lương theo giờ
            WHEN N'Nhân viên dịch vụ' THEN 60000        -- Lương theo giờ
            WHEN N'Nhân viên quản lý kho' THEN 13500000 -- Lương theo tháng cố định
       END
FROM NhanVien_Temp nvt;
GO

-- THÊM DỮ LIỆU CHUYÊN NGÀNH BÁC SĨ
INSERT INTO ChuyenNganh (maChuyenNganh, tenChuyenNganh)
VALUES 
('CN001', N'Nội khoa'),
('CN002', N'Ngoại khoa'),
('CN003', N'Da liễu thú y'),
('CN004', N'Răng hàm mặt'),
('CN005', N'Sản khoa');
GO

-- 4. BÁC SĨ THÚ Y 
INSERT INTO BacSyThuY (maBacSi, maTK, maChuyenNganh, hoTen, gioiTinh, sdt, email, ngaySinh, luongThang)
SELECT 'BS' + dbo.fn_C3(n), 
       'TK' + dbo.fn_C3(n + 15),
       'CN' + dbo.fn_C3(n), 
       CHOOSE(n, N'Nguyễn Hữu Tài', N'Trần Thị Uyên', N'Lê Minh Vinh', N'Phạm Thị Xuân', N'Võ Thanh Tùng'),
       CASE WHEN n % 2 = 1 THEN N'Nam' ELSE N'Nữ' END,
       '0903000' + dbo.fn_C3(n),
       'bacsi' + dbo.fn_C3(n) + '@salala.vn',
       DATEADD(DAY, n * 301, '19900301'),
       16000000 + n * 1000000 -- Lương tháng cố định (Mười mấy triệu)
FROM #N WHERE n <= 5;
GO

-- 5. Khách hàng 
INSERT INTO KhachHang (maKH, hoTen, sdt, email, diaChi)
SELECT 'KH' + dbo.fn_C3(n),
       CHOOSE(n, N'Nguyễn Thị Hoa', N'Trần Văn Hùng', N'Lê Thị Kim', N'Phạm Văn Lâm', N'Võ Thị My',
                 N'Đặng Văn Nghĩa', N'Bùi Thị Oanh', N'Hồ Văn Phong', N'Ngô Thị Quyên', N'Dương Văn Sĩ'),
       '0904000' + dbo.fn_C3(n),
       'khachhang' + dbo.fn_C3(n) + '@gmail.com',
       N'Đường Nguyễn Oanh, Gò Vấp, TP.HCM'
FROM #N WHERE n <= 10;
GO

-- 6. Thú cưng 
INSERT INTO ThuCung (maTC, maKH, tenTC, loai, gioiTinh, ngaySinh, chuHo, dacDiemNhanDien)
SELECT 'TC' + dbo.fn_C3(n), k.maKH,
       CHOOSE(n, N'Mimi', N'Lucky', N'Bông', N'Milo', N'Kitty', N'Boss', N'Susu', N'Nâu', N'Tom', N'Mực'),
       CHOOSE(n % 4 + 1, N'Chó', N'Mèo', N'Thỏ', N'Hamster'),
       CASE WHEN n % 2 = 1 THEN N'Đực' ELSE N'Cái' END,
       DATEADD(MONTH, -n * 5, '20251001'),
       k.hoTen,
       N'Lông khỏe mạnh, lanh lợi'
FROM #N
JOIN KhachHang k ON k.maKH = 'KH' + dbo.fn_C3(((n - 1) % 10) + 1)
WHERE n <= 10;
GO

-- 7. LỊCH LÀM VIỆC NHÂN VIÊN TRONG THÁNG 10/2026 
INSERT INTO LichLamViec (maLLV, maNhanVien, ngay, gioBatDau, gioKetThuc, trangThai)
SELECT 'LLV' + RIGHT('000' + CAST(ROW_NUMBER() OVER (ORDER BY nv.maNhanVien, d.n) AS VARCHAR(4)), 4),
       nv.maNhanVien,
       DATEADD(DAY, d.n - 1, '2026-10-01'),
       CAST('08:00' AS TIME),
       CAST('17:00' AS TIME),
       N'Hoàn thành'
FROM NhanVien nv
CROSS JOIN (SELECT n FROM #N WHERE n <= 30) d;
GO

-- 8. Dịch vụ 
INSERT INTO DichVu (maDV, tenDV, moTa, giaDV, thoiGian, trangThai)
SELECT 'DV' + dbo.fn_C3(v.n), v.ten, N'Dịch vụ chuyên nghiệp: ' + v.ten,
       v.gia, CAST('00:30:00' AS TIME), N'Đang áp dụng'
FROM (VALUES
 (1, N'Khám tổng quát', 150000), 
 (2, N'Tiêm phòng dại', 120000), 
 (3, N'Tắm vệ sinh thú cưng', 100000),
 (4, N'Cắt tỉa lông', 200000), 
 (5, N'Cạo vôi răng', 300000)
) v(n, ten, gia);
GO

-- 9. Nhà cung cấp 
INSERT INTO NhaCungCap (maNCC, tenNCC, diaChi, sdt, email)
SELECT 'NCC' + dbo.fn_C3(v.n), v.ten, N'KCN Tân Bình, TP.HCM', '0283800' + dbo.fn_C3(v.n), 'lienhe@ncc.vn'
FROM (VALUES
 (1, N'Royal Canin Việt Nam'), 
 (2, N'PetCare Sài Gòn'), 
 (3, N'Thú Y Xanh')
) v(n, ten);
GO

-- 10. Vật tư 
INSERT INTO VatTu (maVT, maNCC, tenVT, soLuongTon, trangThai, giaBanCoBan, giaNhap)
SELECT 'VT' + dbo.fn_C3(v.n), 'NCC00' + CAST(((v.n - 1) % 3) + 1 AS VARCHAR(1)),
       v.ten, 50, N'Còn hàng', v.giaBan, v.giaNhap
FROM (VALUES
 (1, N'Thức ăn hạt cho chó 2kg', 250000, 200000),
 (2, N'Thức ăn hạt cho mèo 1.5kg', 220000, 180000),
 (3, N'Sữa tắm thú cưng 500ml', 150000, 110000),
 (4, N'Pate mèo 85g', 25000, 18000),
 (5, N'Xương gặm sạch răng', 60000, 45000)
) v(n, ten, giaBan, giaNhap);
GO

-- 11. Hồ sơ sức khỏe (Tháng 10/2026)
INSERT INTO HoSoSucKhoe (maHSSK, maTC, maBacSi, ngayKham, trieuChung, chuanDoan, canNang)
SELECT 'HS' + dbo.fn_C3(n), 'TC' + dbo.fn_C3((n % 10) + 1), 'BS' + dbo.fn_C3(((n - 1) % 5) + 1),
       DATEADD(DAY, (n % 28), '2026-10-01'),
       N'Mệt mỏi, biếng ăn', N'Rối loạn tiêu hóa nhẹ', 4.5
FROM #N WHERE n <= 10;
GO

-- 12. Thuốc
INSERT INTO Thuoc (maThuoc, tenThuoc, donViTinh, huongDanSuDung)
SELECT 'TH' + dbo.fn_C3(v.n), v.ten, N'Viên', N'Uống ngày 2 lần'
FROM (VALUES
 (1, N'Amoxicillin 250mg'), (2, N'Cefalexin 500mg'), 
 (3, N'Men tiêu hóa Probiotic'), (4, N'Vitamin B Complex'), (5, N'Ivermectin 1%')
) v(n, ten);
GO

-- 13. Đơn thuốc & Chi tiết đơn thuốc
INSERT INTO DonThuoc (maDonThuoc, maTC, maBacSi, maHSSK, ngayKeDon, ghiChu, trangThai)
SELECT 'DT' + dbo.fn_C3(n), 'TC' + dbo.fn_C3((n % 10) + 1), 'BS001', 'HS' + dbo.fn_C3(n), '2026-10-02', N'Uống đủ liều', N'Hoàn thành'
FROM #N WHERE n <= 10;

INSERT INTO ChiTietDonThuoc (maCTDT, maDonThuoc, maThuoc, soLuong, lieuLuong)
SELECT 'CTDT' + dbo.fn_C3(n), 'DT' + dbo.fn_C3(n), 'TH' + dbo.fn_C3((n % 5) + 1), 10, 2
FROM #N WHERE n <= 10;
GO

-- 14. KHUYẾN MÃI (Hoạt động đến hết 31/12/2026)
INSERT INTO KhuyenMai (maKM, tenKM, mucKM, ngayBatDau, ngayKetThuc, dieuKien)
VALUES 
('KM001', N'Ưu đãi tháng 10 đơn hàng trên 200k', 10.00, '2026-10-01', '2026-12-31', N'Áp dụng cho hóa đơn từ 200.000đ trở lên'),
('KM002', N'Đặc quyền khách hàng hạng Vàng và Kim Cương', 15.00, '2026-10-01', '2026-12-31', N'Áp dụng cho khách hàng thành viên từ hạng Vàng trở lên'),
('KM003', N'Tri ân cuối năm đơn hàng trên 500k', 20.00, '2026-10-15', '2026-12-31', N'Áp dụng cho hóa đơn giá trị từ 500.000đ trở lên');
GO

-- 15. Thuế
INSERT INTO Thue (maThue, tenThue, mucThue)
VALUES ('TX001', N'GTGT 10%', 10.00);
GO


-- 16. HÓA ĐƠN & THANH TOÁN (75 HÓA ĐƠN TRONG THÁNG 10)
INSERT INTO ThanhToan (maThanhToan, soTien, thoiGian, phuongThuc, trangThai)
SELECT 'TT' + dbo.fn_C3(n), 
       0, 
       DATEADD(HOUR, 8 + (n % 10), DATEADD(DAY, n % 31, '2026-10-01')), 
       CHOOSE((n % 3) + 1, N'Tiền mặt', N'Chuyển khoản', N'Ví MoMo'), 
       N'Đã thanh toán'
FROM #N WHERE n <= 75;

INSERT INTO HoaDon (maHD, ngayLap, trangThai, maKH, maNhanVien, maKM, maThue, maThanhToan)
SELECT 'HD' + dbo.fn_C3(n), 
       DATEADD(HOUR, 8 + (n % 10), DATEADD(DAY, n % 31, '2026-10-01')), 
       N'Đã thanh toán', 
       'KH' + dbo.fn_C3((n % 10) + 1), 
       'NV' + dbo.fn_C3((n % 5) + 1),  
       CHOOSE((n % 4) + 1, 'KM001', 'KM002', 'KM003', NULL), 
       'TX001', 
       'TT' + dbo.fn_C3(n)
FROM #N WHERE n <= 75;

INSERT INTO ChiTietHoaDon (maCTHD, maHD, maDV, maVT, maNhanVien, maBacSi, soLuong, donGia)
SELECT 'CTHD' + dbo.fn_C3(n), 
       'HD' + dbo.fn_C3(n), 
       'DV' + dbo.fn_C3((n % 5) + 1), 
       NULL, 
       'NV' + dbo.fn_C3((n % 5) + 6), 
       'BS' + dbo.fn_C3((n % 5) + 1), 
       1, 
       150000 + (n % 3) * 50000
FROM #N WHERE n <= 75;

INSERT INTO ChiTietHoaDon (maCTHD, maHD, maDV, maVT, maNhanVien, maBacSi, soLuong, donGia)
SELECT 'CTHD' + dbo.fn_C3(n + 75), 
       'HD' + dbo.fn_C3(n), 
       NULL, 
       'VT' + dbo.fn_C3((n % 5) + 1), 
       'NV' + dbo.fn_C3((n % 5) + 1), 
       NULL, 
       (n % 3) + 1, 
       100000 + (n % 2) * 50000
FROM #N WHERE n <= 75 AND n % 2 = 0;
GO

UPDATE tt
SET tt.soTien = v.tongThanhToan
FROM ThanhToan tt
JOIN HoaDon h ON h.maThanhToan = tt.maThanhToan
JOIN vw_TongTienHoaDon v ON v.maHD = h.maHD;
GO


-- 17. PHIẾU XUẤT KHO & CHI TIẾT
INSERT INTO PhieuXuatKho (maPXK, ngayXuat, ghiChu, trangThai, maNhanVien)
VALUES 
('PXK001', '2026-10-06 09:00:00', N'Xuất bán lẻ vật tư thú cưng', N'Hoàn thành', 'NV011'),
('PXK002', '2026-10-10 14:00:00', N'Xuất dùng cho phòng khám dịch vụ', N'Hoàn thành', 'NV012');

INSERT INTO ChiTietPhieuXuatKho (maCTPXK, maPXK, maVT, soLuong)
VALUES 
('CTPXK01', 'PXK001', 'VT001', 5),
('CTPXK02', 'PXK001', 'VT002', 3),
('CTPXK03', 'PXK002', 'VT003', 2);
GO

-- 18. PHIẾU NHẬP KHO & CHI TIẾT
INSERT INTO PhieuNhapKho (maPNK, ngayNhap, ghiChu, trangThai, maNhanVien, maNCC)
VALUES 
('PNK001', '2026-10-02 08:30:00', N'Nhập hàng định kỳ đầu tháng', N'Hoàn thành', 'NV011', 'NCC001'),
('PNK002', '2026-10-12 10:00:00', N'Nhập bổ sung thức ăn và sữa tắm', N'Hoàn thành', 'NV012', 'NCC002');

INSERT INTO ChiTietPhieuNhapKho (maCTPNK, maPNK, maVT, donGia, soLuong)
VALUES 
('CTPNK01', 'PNK001', 'VT001', 200000, 20),
('CTPNK02', 'PNK001', 'VT002', 180000, 15),
('CTPNK03', 'PNK002', 'VT003', 110000, 30);
GO


-- 19. Quản lý hạng thành viên
INSERT INTO QuanLyHangThanhVien
       (maHang, maKH, tenHang, chiTieuToiThieu, chiTieuToiDa, mucGiamGia, diemTichLuy1Van,
        quyenLoi, tongChiTieu, diemTichLuy, ngayCapNhatHang, trangThai)
SELECT 'HTV' + dbo.fn_C3(n),
       k.maKH,
       t.tenHang, t.chiTieuToiThieu, t.chiTieuToiDa, t.mucGiamGia, t.diemTichLuy1Van,
       t.quyenLoi,
       c.tongChiTieu,
       CAST(FLOOR(c.tongChiTieu / 10000.0 * t.diemTichLuy1Van) AS INT),
       ISNULL(c.ngayGanNhat, CAST('2026-10-01' AS DATETIME2)),
       N'Đang áp dụng'
FROM #N
JOIN KhachHang k ON k.maKH = 'KH' + dbo.fn_C3(n)
OUTER APPLY (
    SELECT ISNULL(SUM(v.tongThanhToan), 0) AS tongChiTieu,
           MAX(h.ngayLap)                  AS ngayGanNhat
    FROM HoaDon h
    JOIN vw_TongTienHoaDon v ON v.maHD = h.maHD
    WHERE h.maKH = k.maKH
) c
JOIN (VALUES
    (N'Đồng',      CAST(0        AS DECIMAL(18,2)), CAST(5000000  AS DECIMAL(18,2)),  2.00, 1.0,
        N'Giảm 2% hóa đơn dịch vụ' + CHAR(10) + N'Tích 1 điểm / 10.000 ₫' + CHAR(10) + N'Nhắc lịch tiêm và tái khám' + CHAR(10) + N'Ưu đãi sinh nhật thú cưng'),
    (N'Bạc',       CAST(5000000  AS DECIMAL(18,2)), CAST(15000000 AS DECIMAL(18,2)),  5.00, 1.2,
        N'Giảm 5% hóa đơn dịch vụ' + CHAR(10) + N'Tích 1,2 điểm / 10.000 ₫' + CHAR(10) + N'01 lần vệ sinh tai miễn phí / quý' + CHAR(10) + N'Ưu tiên khung giờ khám'),
    (N'Vàng',      CAST(15000000 AS DECIMAL(18,2)), CAST(30000000 AS DECIMAL(18,2)), 10.00, 1.5,
        N'Giảm 10% hóa đơn dịch vụ' + CHAR(10) + N'Tích 1,5 điểm / 10.000 ₫' + CHAR(10) + N'01 lần khám tổng quát / năm' + CHAR(10) + N'Miễn phí giao hàng nội thành'),
    (N'Kim cương', CAST(30000000 AS DECIMAL(18,2)), CAST(NULL     AS DECIMAL(18,2)), 15.00, 2.0,
        N'Giảm 15% hóa đơn dịch vụ' + CHAR(10) + N'Tích 2 điểm / 10.000 ₫' + CHAR(10) + N'02 lần khám tổng quát / năm' + CHAR(10) + N'Hotline bác sĩ ưu tiên 24/7')
) t(tenHang, chiTieuToiThieu, chiTieuToiDa, mucGiamGia, diemTichLuy1Van, quyenLoi)
  ON c.tongChiTieu >= t.chiTieuToiThieu
 AND (t.chiTieuToiDa IS NULL OR c.tongChiTieu < t.chiTieuToiDa)
WHERE n <= 10;
GO

DROP TABLE #N;
DROP FUNCTION dbo.fn_C3;
GO

/* =========================================================
   KIỂM TRA DỮ LIỆU CÁC BẢNG
   ========================================================= */
SELECT t.name AS TenBang, SUM(p.rows) AS SoDong
FROM sys.tables t
JOIN sys.partitions p ON p.object_id = t.object_id AND p.index_id IN (0, 1)
GROUP BY t.name
ORDER BY t.name;
GO

PRINT '========================================================';
PRINT 'ĐÃ XÓA HOÀN TOÀN BẢNG LUONGCHUCVU VÀ GÁN DỮ LIỆU THÀNH CÔNG!';
PRINT '========================================================';
GO
