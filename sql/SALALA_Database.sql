/* =========================================================
   DATABASE: SALALA
   HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG / PHÒNG KHÁM THÚ Y
   (Cập nhật: Nối trực tiếp Nhà Cung Cấp vào Vật Tư)
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
    luongCoBan   DECIMAL(18,2) NOT NULL,

    CONSTRAINT PK_QuanLyCuaHang PRIMARY KEY (maQuanLy),
    CONSTRAINT UQ_QuanLyCuaHang_TaiKhoan UNIQUE (maTK),
    CONSTRAINT FK_QuanLyCuaHang_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK),
    CONSTRAINT CK_QuanLyCuaHang_Luong CHECK (luongCoBan >= 0)
);
GO

-- Thiết lập khóa ngoại từ Tài khoản liên kết đến Quản lý
ALTER TABLE TaiKhoan
ADD CONSTRAINT FK_TaiKhoan_QuanLy FOREIGN KEY (maQuanLy) REFERENCES QuanLyCuaHang(maQuanLy);
GO

-- 3. NHÂN VIÊN
CREATE TABLE NhanVien
(
    maNhanVien  VARCHAR(20)   NOT NULL,
    maTK        VARCHAR(20)   NULL,
    hoTen       NVARCHAR(100) NOT NULL,
    chucVu      NVARCHAR(50)  NOT NULL,
    sdt         VARCHAR(20)   NOT NULL,
    diaChi      NVARCHAR(255),
    ngaySinh    DATE,
    gioiTinh    NVARCHAR(20),
    luongCoBan  DECIMAL(18,2) NOT NULL,

    CONSTRAINT PK_NhanVien PRIMARY KEY (maNhanVien),
    CONSTRAINT FK_NhanVien_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK),
    CONSTRAINT CK_NhanVien_Luong CHECK (luongCoBan >= 0)
);
GO
CREATE UNIQUE INDEX UQ_NhanVien_TaiKhoan ON NhanVien(maTK) WHERE maTK IS NOT NULL;
GO

-- MỨC LƯƠNG THEO CHỨC VỤ
CREATE TABLE LuongChucVu
(
    chucVu      NVARCHAR(50) NOT NULL,
    luongCoBan  DECIMAL(18,2) NOT NULL,

    CONSTRAINT PK_LuongChucVu PRIMARY KEY (chucVu),
    CONSTRAINT CK_LuongChucVu_Luong CHECK (luongCoBan >= 0)
);
GO

-- 4. BÁC SĨ THÚ Y
CREATE TABLE BacSyThuY
(
    maBacSi          VARCHAR(20)   NOT NULL,
    maTK             VARCHAR(20)   NULL,
    tenChuyenNganh   NVARCHAR(100) NOT NULL,
    hoTen            NVARCHAR(100) NOT NULL,
    gioiTinh         NVARCHAR(20),
    sdt              VARCHAR(20)   NOT NULL,
    email            VARCHAR(100)  NOT NULL,
    ngaySinh         DATE,
    luongThang       DECIMAL(18,2) NOT NULL CONSTRAINT DF_BacSyThuY_LuongThang DEFAULT 0,

    CONSTRAINT PK_BacSyThuY PRIMARY KEY (maBacSi),
    CONSTRAINT FK_BacSyThuY_TaiKhoan FOREIGN KEY (maTK) REFERENCES TaiKhoan(maTK),
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

-- 19. VẬT TƯ (Thêm cột maNCC nối trực tiếp tới Nhà Cung Cấp)
CREATE TABLE VatTu
(
    maVT         VARCHAR(20)   NOT NULL,
    maNCC        VARCHAR(20)   NOT NULL, -- Đồng bộ nối đến Nhà cung cấp
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
    maCTHD    VARCHAR(20)   NOT NULL,
    maHD      VARCHAR(20)   NOT NULL,
    maDV      VARCHAR(20)   NULL,
    maVT      VARCHAR(20)   NULL,
    maNhanVien VARCHAR(20)  NULL,
    maBacSi   VARCHAR(20)   NULL,
    soLuong   DECIMAL(18,2) NOT NULL,
    donGia    DECIMAL(18,2) NOT NULL,

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

CREATE TABLE #N (n INT PRIMARY KEY);
WITH x AS (SELECT 1 AS n UNION ALL SELECT n + 1 FROM x WHERE n < 45)
INSERT INTO #N SELECT n FROM x;
GO

-- 1. TaiKhoan: Tạo 45 dòng
INSERT INTO TaiKhoan (maTK, tenDangNhap, matKhau, vaiTro, trangThai, maQuanLy)
SELECT 'TK' + dbo.fn_C3(n),
       CASE WHEN n <= 15 THEN 'quanly'   + RIGHT('0' + CAST(n      AS VARCHAR(2)), 2)
            WHEN n <= 30 THEN 'nhanvien' + RIGHT('0' + CAST(n - 15 AS VARCHAR(2)), 2)
            ELSE              'bacsi'    + RIGHT('0' + CAST(n - 30 AS VARCHAR(2)), 2) END,
       CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', '123456'), 2),
       CASE WHEN n <= 15 THEN N'Quản lý'
            WHEN n <= 20 THEN N'Nhân viên bán hàng'
            WHEN n <= 25 THEN N'Nhân viên dịch vụ'
            WHEN n <= 30 THEN N'Nhân viên quản lý kho'
            ELSE N'Bác sĩ' END,
       CASE WHEN n % 9 = 0 THEN 0 ELSE 1 END,
       NULL 
FROM #N;
GO

-- 2. QuanLyCuaHang: 15 dòng
INSERT INTO QuanLyCuaHang (maQuanLy, maTK, hoTenQuanLy, sdt, diaChi, ngaySinh, gioiTinh, luongCoBan)
SELECT 'QL' + dbo.fn_C3(n), 'TK' + dbo.fn_C3(n),
       CHOOSE(n, N'Nguyễn Văn An', N'Trần Thị Bình', N'Lê Hoàng Cường', N'Phạm Thị Dung', N'Võ Minh Đức',
                 N'Đặng Thu Hà', N'Bùi Quốc Huy', N'Hồ Ngọc Lan', N'Ngô Văn Mạnh', N'Dương Thị Nga',
                 N'Lý Văn Nam', N'Phan Thị Oanh', N'Trương Văn Phúc', N'Huỳnh Thị Quyên', N'Đỗ Văn Sơn'),
       '0901000' + dbo.fn_C3(n),
       CHOOSE(n % 5 + 1, N'Quận 1', N'Quận 3', N'Quận 5', N'Quận 7', N'Gò Vấp') + N', TP.HCM',
       DATEADD(DAY, n * 150, '19800510'),
       CASE WHEN n % 2 = 1 THEN N'Nam' ELSE N'Nữ' END,
       15000000 + n * 1000000
FROM #N WHERE n <= 15;
GO

-- Cập nhật maQuanLy cho các tài khoản
UPDATE TaiKhoan
SET maQuanLy = 'QL' + dbo.fn_C3(((CAST(RIGHT(maTK, 3) AS INT) - 1) % 5) + 1)
WHERE vaiTro <> N'Quản lý';
GO

-- 3. NhanVien: 15 dòng
INSERT INTO NhanVien (maNhanVien, maTK, hoTen, chucVu, sdt, diaChi, ngaySinh, gioiTinh, luongCoBan)
SELECT 'NV' + dbo.fn_C3(n), 'TK' + dbo.fn_C3(n + 15),
       CHOOSE(n, N'Trần Quốc Bảo', N'Nguyễn Thị Cẩm', N'Lê Văn Dũng', N'Phạm Hồng Em', N'Võ Văn Phúc',
                 N'Đặng Thị Giang', N'Bùi Minh Hải', N'Hồ Thị Hạnh', N'Ngô Văn Khoa', N'Dương Thị Linh',
                 N'Lý Quang Minh', N'Phan Thị Ngân', N'Trương Văn Phát', N'Huỳnh Thị Quỳnh', N'Đỗ Văn Sang'),
       CASE WHEN n <= 5 THEN N'Nhân viên bán hàng'
            WHEN n <= 10 THEN N'Nhân viên dịch vụ'
            ELSE N'Nhân viên quản lý kho' END,
       '0902000' + dbo.fn_C3(n),
       CHOOSE(n % 5 + 1, N'Củ Chi', N'Quận 1', N'Quận 3', N'Thủ Đức', N'Bình Thạnh') + N', TP.HCM',
       DATEADD(DAY, n * 263, '19880115'),
       CASE WHEN n % 2 = 1 THEN N'Nam' ELSE N'Nữ' END,
       CASE WHEN n <= 5 THEN 8500000
            WHEN n <= 10 THEN 11000000
            ELSE 13500000 END
FROM #N WHERE n <= 15;
GO

-- Mức lương nhân viên dùng chung theo chức vụ; Quản lý chưa tính lương.
INSERT INTO LuongChucVu (chucVu, luongCoBan)
VALUES
    (N'Nhân viên bán hàng', 8500000),
    (N'Nhân viên dịch vụ', 11000000),
    (N'Nhân viên quản lý kho', 13500000),
    (N'Quản lý', 0);
GO

UPDATE nv
SET luongCoBan = lc.luongCoBan
FROM NhanVien nv
JOIN LuongChucVu lc ON lc.chucVu = nv.chucVu;
GO

-- 4. BacSyThuY: 15 dòng
INSERT INTO BacSyThuY (maBacSi, maTK, tenChuyenNganh, hoTen, gioiTinh, sdt, email, ngaySinh)
SELECT 'BS' + dbo.fn_C3(n), 'TK' + dbo.fn_C3(n + 30),
       CHOOSE(n % 5 + 1, N'Nội khoa', N'Ngoại khoa', N'Da liễu thú y', N'Răng hàm mặt', N'Sản khoa'),
       CHOOSE(n, N'Nguyễn Hữu Tài', N'Trần Thị Uyên', N'Lê Minh Vinh', N'Phạm Thị Xuân', N'Võ Thanh Tùng',
                 N'Đặng Thị Yến', N'Bùi Văn Khánh', N'Hồ Thị Diệp', N'Ngô Đức Long', N'Dương Thị Mỹ',
                 N'Lý Văn Nhân', N'Phan Thị Phượng', N'Trương Văn Quang', N'Huỳnh Thị Thu', N'Đỗ Văn Toàn'),
       CASE WHEN n % 2 = 1 THEN N'Nam' ELSE N'Nữ' END,
       '0903000' + dbo.fn_C3(n),
       'bacsi' + dbo.fn_C3(n) + '@salala.vn',
       DATEADD(DAY, n * 301, '19850301')
FROM #N WHERE n <= 15;
GO

-- 5. KhachHang: 15 dòng
INSERT INTO KhachHang (maKH, hoTen, sdt, email, diaChi)
SELECT 'KH' + dbo.fn_C3(n),
       CHOOSE(n, N'Nguyễn Thị Hoa', N'Trần Văn Hùng', N'Lê Thị Kim', N'Phạm Văn Lâm', N'Võ Thị My',
                 N'Đặng Văn Nghĩa', N'Bùi Thị Oanh', N'Hồ Văn Phong', N'Ngô Thị Quyên', N'Dương Văn Sĩ',
                 N'Lý Thị Tuyết', N'Phan Văn Thắng', N'Trương Thị Út', N'Huỳnh Văn Vinh', N'Đỗ Thị Xuyến'),
       '0904000' + dbo.fn_C3(n),
       'khachhang' + dbo.fn_C3(n) + '@gmail.com',
       N'Số ' + CAST(n * 7 AS NVARCHAR(5)) + N' ' +
       CHOOSE(n % 5 + 1, N'Tỉnh lộ 8, Củ Chi', N'Lê Lợi, Quận 1', N'Võ Văn Tần, Quận 3',
                         N'Võ Văn Ngân, Thủ Đức', N'Xô Viết Nghệ Tĩnh, Bình Thạnh') + N', TP.HCM'
FROM #N WHERE n <= 15;
GO

-- 6. ThuCung: 15 dòng
INSERT INTO ThuCung (maTC, maKH, tenTC, loai, gioiTinh, ngaySinh, chuHo, dacDiemNhanDien)
SELECT 'TC' + dbo.fn_C3(n), k.maKH,
       CHOOSE(n, N'Mimi', N'Lucky', N'Bông', N'Milo', N'Kitty', N'Boss', N'Susu', N'Nâu',
                 N'Tom', N'Mực', N'Vàng', N'Gấu', N'Mít', N'Bơ', N'Cún'),
       CHOOSE(n % 4 + 1, N'Chó', N'Mèo', N'Thỏ', N'Hamster'),
       CASE WHEN n % 2 = 1 THEN N'Đực' ELSE N'Cái' END,
       DATEADD(MONTH, -n * 5, '20251001'),
       k.hoTen,
       CHOOSE(n % 4 + 1, N'Lông trắng, mắt xanh', N'Lông vàng, đuôi ngắn',
                         N'Đốm đen ở tai trái', N'Lông xám, khoang trắng ở ngực')
FROM #N
JOIN KhachHang k ON k.maKH = 'KH' + dbo.fn_C3(((n - 1) % 12) + 1)
WHERE n <= 15;
GO

-- 7. LichLamViec: 15 dòng
INSERT INTO LichLamViec (maLLV, maNhanVien, ngay, gioBatDau, gioKetThuc, trangThai)
SELECT 'LLV' + dbo.fn_C3(n), 'NV' + dbo.fn_C3(n),
       DATEADD(DAY, n, '20251001'),
       CASE WHEN n % 2 = 0 THEN CAST('13:00' AS TIME) ELSE CAST('08:00' AS TIME) END,
       CASE WHEN n % 2 = 0 THEN CAST('21:00' AS TIME) ELSE CAST('17:00' AS TIME) END,
       CHOOSE(n % 3 + 1, N'Đã phân ca', N'Đang làm', N'Hoàn thành')
FROM #N WHERE n <= 15;
GO

-- 8. HoSoSucKhoe: 15 dòng
INSERT INTO HoSoSucKhoe (maHSSK, maTC, maBacSi, ngayKham, trieuChung, chuanDoan, canNang)
SELECT 'HS' + dbo.fn_C3(n), 'TC' + dbo.fn_C3(n), 'BS' + dbo.fn_C3(n),
       DATEADD(HOUR, 8 + n % 8, CAST(DATEADD(DAY, n * 5, '20250601') AS DATETIME2)),
       CHOOSE(n % 5 + 1, N'Nôn mửa, bỏ ăn', N'Ngứa, rụng lông', N'Tiêu chảy, mệt mỏi',
                         N'Ho, chảy nước mũi', N'Đi khập khiễng, đau chân'),
       CHOOSE(n % 5 + 1, N'Viêm dạ dày', N'Viêm da dị ứng', N'Rối loạn tiêu hóa',
                         N'Viêm đường hô hấp trên', N'Trật khớp nhẹ'),
       CAST(2 + n * 0.8 AS DECIMAL(10,2))
FROM #N WHERE n <= 15;
GO

-- 9. Thuoc: 15 dòng
INSERT INTO Thuoc (maThuoc, tenThuoc, donViTinh, huongDanSuDung)
SELECT 'TH' + dbo.fn_C3(v.n), v.ten, v.dvt, v.hd
FROM (VALUES
 (1,  N'Amoxicillin 250mg',        N'Viên', N'Uống sau ăn, 2 lần/ngày'),
 (2,  N'Cefalexin 500mg',          N'Viên', N'Uống 2 lần/ngày, liên tục 5-7 ngày'),
 (3,  N'Metronidazole 250mg',      N'Viên', N'Uống sau ăn, 2 lần/ngày'),
 (4,  N'Doxycycline 100mg',        N'Viên', N'Uống 1 lần/ngày, kèm thức ăn'),
 (5,  N'Prednisolone 5mg',         N'Viên', N'Uống buổi sáng sau ăn, theo chỉ định bác sĩ'),
 (6,  N'Meloxicam 1.5mg/ml',       N'Chai', N'Trộn vào thức ăn, 1 lần/ngày'),
 (7,  N'Ivermectin 1%',            N'Ống',  N'Nhỏ gáy 1 lần, lặp lại sau 2 tuần'),
 (8,  N'Praziquantel 50mg',        N'Viên', N'Tẩy giun sán, uống 1 lần'),
 (9,  N'Famotidine 10mg',          N'Viên', N'Uống trước ăn 30 phút'),
 (10, N'Omeprazole 20mg',          N'Viên', N'Uống buổi sáng lúc đói'),
 (11, N'Vitamin B Complex',        N'Chai', N'Uống 5ml/ngày'),
 (12, N'Men tiêu hóa Probiotic',   N'Gói',  N'Pha với nước, 1 gói/ngày'),
 (13, N'Thuốc nhỏ mắt Tobrex',     N'Chai', N'Nhỏ 2 giọt/mắt, 3 lần/ngày'),
 (14, N'Xịt trị nấm da Fungisol',  N'Chai', N'Xịt vùng da bệnh 2 lần/ngày'),
 (15, N'Dung dịch vệ sinh tai',    N'Chai', N'Nhỏ vào tai, massage nhẹ, lau sạch')
) v(n, ten, dvt, hd);
GO

-- 10. DonThuoc: 15 dòng
INSERT INTO DonThuoc (maDonThuoc, maTC, maBacSi, maHSSK, ngayKeDon, ghiChu, trangThai)
SELECT 'DT' + RIGHT(h.maHSSK, 3), h.maTC, h.maBacSi, h.maHSSK, h.ngayKham,
       CHOOSE(CAST(RIGHT(h.maHSSK, 3) AS INT) % 3 + 1, N'Tái khám sau 7 ngày', N'Theo dõi ăn uống', N'Cho uống đủ liều'),
       CHOOSE(CAST(RIGHT(h.maHSSK, 3) AS INT) % 3 + 1, N'Đã cấp thuốc', N'Chờ lấy thuốc', N'Hoàn thành')
FROM HoSoSucKhoe h;
GO

-- 11. ChiTietDonThuoc: 20 dòng
INSERT INTO ChiTietDonThuoc (maCTDT, maDonThuoc, maThuoc, soLuong, lieuLuong)
SELECT 'CTDT' + dbo.fn_C3(n),
       'DT' + dbo.fn_C3(((n - 1) % 15) + 1),
       'TH' + dbo.fn_C3(((n * 2 - 1) % 15) + 1),
       5 + n % 10,
       CAST(0.5 + (n % 4) * 0.5 AS DECIMAL(18,2))
FROM #N WHERE n <= 20;
GO

-- 12. DichVu: 15 dòng
INSERT INTO DichVu (maDV, tenDV, moTa, giaDV, thoiGian, trangThai)
SELECT 'DV' + dbo.fn_C3(v.n), v.ten, N'Dịch vụ: ' + v.ten,
       50000 + v.n * 30000,
       CAST(DATEADD(MINUTE, 15 + (v.n % 6) * 15, CAST('00:00' AS TIME)) AS TIME),
       CASE WHEN v.n % 8 = 0 THEN N'Tạm ngưng' ELSE N'Đang áp dụng' END
FROM (VALUES
 (1, N'Khám tổng quát'), (2, N'Tiêm phòng dại'), (3, N'Tiêm phòng 5 bệnh'), (4, N'Tắm vệ sinh'),
 (5, N'Cắt tỉa lông'), (6, N'Cạo vôi răng'), (7, N'Triệt sản'), (8, N'Xét nghiệm máu'),
 (9, N'Siêu âm'), (10, N'X-quang'), (11, N'Lưu chuồng theo ngày'), (12, N'Spa massage'),
 (13, N'Tẩy giun'), (14, N'Vệ sinh tai'), (15, N'Cắt móng')
) v(n, ten);
GO

-- 13. LichHen: 15 dòng
INSERT INTO LichHen (maLichHen, maTC, maKH, maNhanVien, maDV, ngayGio, lyDo, trangThai, ghiChu, loaiHen)
SELECT 'LH' + dbo.fn_C3(n), tc.maTC, tc.maKH, 'NV' + dbo.fn_C3(n), 'DV' + dbo.fn_C3(n),
       DATEADD(HOUR, 8 + n % 9, CAST(DATEADD(DAY, n, '20251001') AS DATETIME2)),
       CHOOSE(n % 5 + 1, N'Khám định kỳ', N'Tiêm phòng', N'Tắm - cắt tỉa lông', N'Tái khám', N'Kiểm tra tiêu hóa'),
       CHOOSE(n % 4 + 1, N'Chờ xác nhận', N'Đã xác nhận', N'Hoàn thành', N'Đã hủy'),
       CHOOSE(n % 3 + 1, N'Khách đến đúng giờ', N'Mang theo sổ tiêm chủng', N'Nhịn ăn trước 6 tiếng'),
       CHOOSE(n % 3 + 1, N'Khám bệnh', N'Tiêm phòng', N'Spa - Grooming')
FROM #N
JOIN ThuCung tc ON tc.maTC = 'TC' + dbo.fn_C3(n)
WHERE n <= 15;
GO

-- 14. KhuyenMai: 15 dòng
INSERT INTO KhuyenMai (maKM, tenKM, mucKM, ngayBatDau, ngayKetThuc, dieuKien)
SELECT 'KM' + dbo.fn_C3(n),
       N'Ưu đãi đợt ' + CAST(n AS NVARCHAR(2)),
       5 + (n % 6) * 5,
       CAST(DATEADD(MONTH, n - 1, '20250101') AS DATETIME2),
       CAST(DATEADD(DAY, 29, DATEADD(MONTH, n - 1, '20250101')) AS DATETIME2),
       N'Áp dụng cho hóa đơn từ ' + CAST((n % 5 + 1) * 200000 AS NVARCHAR(10)) + N'đ'
FROM #N WHERE n <= 15;
GO

-- 15. Thue: 15 dòng
INSERT INTO Thue (maThue, tenThue, mucThue)
SELECT 'TX' + dbo.fn_C3(v.n), v.ten, v.muc
FROM (VALUES
 (1, N'GTGT 0%', 0), (2, N'GTGT 5%', 5), (3, N'GTGT 8%', 8), (4, N'GTGT 10%', 10),
 (5, N'Thuế dịch vụ thú y', 0), (6, N'Thuế nhập khẩu thuốc', 5), (7, N'Thuế nhập khẩu vật tư', 8),
 (8, N'Thuế tiêu thụ đặc biệt', 10), (9, N'Phí môi trường', 2), (10, N'Phí dịch vụ', 5),
 (11, N'GTGT thực phẩm', 5), (12, N'GTGT phụ kiện', 10), (13, N'Thuế lưu kho', 1),
 (14, N'Phí vận chuyển', 3), (15, N'Thuế bảo vệ môi trường', 2)
) v(n, ten, muc);
GO

-- 16. ThanhToan: 15 dòng 
INSERT INTO ThanhToan (maThanhToan, soTien, thoiGian, phuongThuc, trangThai)
SELECT 'TT' + dbo.fn_C3(n), 100000 * n,
       DATEADD(HOUR, 10, CAST(DATEADD(DAY, n * 6, '20250901') AS DATETIME2)),
       CHOOSE(n % 4 + 1, N'Tiền mặt', N'Chuyển khoản', N'Thẻ ngân hàng', N'Ví MoMo'),
       CASE WHEN n % 5 = 0 THEN N'Chờ thanh toán' ELSE N'Đã thanh toán' END
FROM #N WHERE n <= 15;
GO

-- 17. HoaDon: 15 dòng
INSERT INTO HoaDon (maHD, ngayLap, trangThai, maKH, maNhanVien, maKM, maThue, maThanhToan)
SELECT 'HD' + dbo.fn_C3(n),
       DATEADD(HOUR, 9, CAST(DATEADD(DAY, n * 6, '20250901') AS DATETIME2)),
       CASE WHEN n % 5 = 0 THEN N'Chưa thanh toán' ELSE N'Đã thanh toán' END,
       'KH' + dbo.fn_C3(n), 'NV' + dbo.fn_C3(n),
       CASE WHEN n % 3 = 0 THEN NULL ELSE 'KM' + dbo.fn_C3(n) END,
       'TX' + dbo.fn_C3(((n - 1) % 4) + 1),
       'TT' + dbo.fn_C3(n)
FROM #N WHERE n <= 15;
GO

-- 18. NHÀ CUNG CẤP
INSERT INTO NhaCungCap (maNCC, tenNCC, diaChi, sdt, email)
SELECT 'NCC' + dbo.fn_C3(v.n), v.ten,
       N'Số ' + CAST(v.n * 12 AS NVARCHAR(5)) + N', KCN Tân Bình, TP.HCM',
       '0283800' + dbo.fn_C3(v.n),
       'lienhe' + dbo.fn_C3(v.n) + '@ncc.vn'
FROM (VALUES
 (1, N'Royal Canin Việt Nam'), (2, N'Pedigree Việt Nam'), (3, N'Whiskas Việt Nam'),
 (4, N'PetCare Sài Gòn'), (5, N'Thú Y Xanh'), (6, N'Bayer Animal Health VN'),
 (7, N'Zoetis Việt Nam'), (8, N'Virbac Việt Nam'), (9, N'Dược Thú Y Trung Ương'),
 (10, N'Pet Mart'), (11, N'Happy Pets'), (12, N'Paws Supplies'),
 (13, N'Cát Xanh Co.'), (14, N'Đồ Chơi Thú Cưng Việt'), (15, N'Hanvet')
) v(n, ten);
GO

-- 19. VẬT TƯ (Thêm logic gán mỗi vật tư thuộc về 1 nhà cung cấp)
INSERT INTO VatTu (maVT, maNCC, tenVT, soLuongTon, trangThai, giaBanCoBan, giaNhap)
SELECT 'VT' + dbo.fn_C3(v.n),
       'NCC' + dbo.fn_C3(((v.n - 1) % 15) + 1), -- Nối mã Nhà cung cấp
       v.ten,
       v.n * 10 + 20,
       CASE WHEN v.n % 6 = 0 THEN N'Sắp hết' ELSE N'Còn hàng' END,
       CAST((40000 + v.n * 15000) * 1.3 AS DECIMAL(18,2)),
       40000 + v.n * 15000
FROM (VALUES
 (1, N'Thức ăn hạt cho chó 2kg'), (2, N'Thức ăn hạt cho mèo 1.5kg'), (3, N'Pate mèo 85g'),
 (4, N'Cát vệ sinh đậu nành'), (5, N'Sữa tắm thú cưng 500ml'), (6, N'Dây dắt chó'),
 (7, N'Vòng cổ có chuông'), (8, N'Chuồng sắt cỡ vừa'), (9, N'Bóng đồ chơi cao su'),
 (10, N'Xương gặm sạch răng'), (11, N'Bát ăn inox'), (12, N'Nhà gỗ cho mèo'),
 (13, N'Lược chải lông'), (14, N'Khay vệ sinh'), (15, N'Bánh thưởng huấn luyện')
) v(n, ten);
GO

-- 20. CHI TIẾT HÓA ĐƠN
INSERT INTO ChiTietHoaDon (maCTHD, maHD, maDV, maVT, soLuong, donGia)
SELECT 'CTHD' + dbo.fn_C3(n),
       'HD' + dbo.fn_C3(((n - 1) % 15) + 1),
       d.maDV, v.maVT,
       1 + n % 3,
       COALESCE(d.giaDV, v.giaBanCoBan)
FROM #N
LEFT JOIN DichVu d ON n % 2 = 1 AND d.maDV = 'DV' + dbo.fn_C3((n + 1) / 2)
LEFT JOIN VatTu  v ON n % 2 = 0 AND v.maVT = 'VT' + dbo.fn_C3(n / 2)
WHERE n <= 20;
GO

-- Cập nhật số tiền thanh toán khớp với tổng hóa đơn
UPDATE tt
SET tt.soTien = v.tongThanhToan
FROM ThanhToan tt
JOIN HoaDon h ON h.maThanhToan = tt.maThanhToan
JOIN vw_TongTienHoaDon v ON v.maHD = h.maHD;
GO

-- 21. PHIẾU XUẤT KHO
INSERT INTO PhieuXuatKho (maPXK, ngayXuat, ghiChu, trangThai, maNhanVien)
SELECT 'PXK' + dbo.fn_C3(n),
       DATEADD(HOUR, 14, CAST(DATEADD(DAY, n * 4, '20250901') AS DATETIME2)),
       CHOOSE(n % 3 + 1, N'Xuất bán lẻ', N'Xuất dùng cho phòng khám', N'Xuất cho khu spa'),
       CHOOSE(n % 3 + 1, N'Hoàn thành', N'Chờ duyệt', N'Đã duyệt'),
       'NV' + dbo.fn_C3(n)
FROM #N WHERE n <= 15;
GO

-- 22. CHI TIẾT PHIẾU XUẤT KHO
INSERT INTO ChiTietPhieuXuatKho (maCTPXK, maPXK, maVT, soLuong)
SELECT 'CTPXK' + dbo.fn_C3(n),
       'PXK' + dbo.fn_C3(((n - 1) % 15) + 1),
       'VT'  + dbo.fn_C3(((n * 2 - 1) % 15) + 1),
       1 + n % 5
FROM #N WHERE n <= 20;
GO

-- 23. PHIẾU NHẬP KHO
INSERT INTO PhieuNhapKho (maPNK, ngayNhap, ghiChu, trangThai, maNhanVien, maNCC)
SELECT 'PNK' + dbo.fn_C3(n),
       DATEADD(HOUR, 9, CAST(DATEADD(DAY, n * 4, '20250801') AS DATETIME2)),
       CHOOSE(n % 3 + 1, N'Nhập hàng định kỳ', N'Nhập bổ sung hàng sắp hết', N'Nhập hàng khuyến mãi'),
       CHOOSE(n % 3 + 1, N'Hoàn thành', N'Chờ duyệt', N'Đã duyệt'),
       'NV' + dbo.fn_C3(n), 'NCC' + dbo.fn_C3(n)
FROM #N WHERE n <= 15;
GO

-- 24. CHI TIẾT PHIẾU NHẬP KHO
INSERT INTO ChiTietPhieuNhapKho (maCTPNK, maPNK, maVT, donGia, soLuong)
SELECT 'CTPNK' + dbo.fn_C3(n),
       'PNK' + dbo.fn_C3(((n - 1) % 15) + 1),
       v.maVT, v.giaNhap, 10 + n * 2
FROM #N
JOIN VatTu v ON v.maVT = 'VT' + dbo.fn_C3(((n - 1) % 15) + 1)
WHERE n <= 20;
GO

DROP TABLE #N;
DROP FUNCTION dbo.fn_C3;
GO

/* =========================================================
   KIỂM TRA
   ========================================================= */
SELECT t.name AS TenBang, SUM(p.rows) AS SoDong
FROM sys.tables t
JOIN sys.partitions p ON p.object_id = t.object_id AND p.index_id IN (0, 1)
GROUP BY t.name
ORDER BY t.name;
GO

PRINT '========================================';
PRINT 'DATABASE SALALA DA DUOC TAO THANH CONG';
PRINT '========================================';
GO
