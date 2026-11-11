/* =========================================================
   DATABASE: SALALA
   HỆ THỐNG QUẢN LÝ CỬA HÀNG THÚ CƯNG / PHÒNG KHÁM THÚ Y
   (Cập nhật: Xóa bỏ bảng LuongChucVu, gán lương cơ bản trực tiếp, cập nhật khóa chính ChiTietHoaDon, xóa bảng ThanhToan và thêm phuongThucThanhToan vào HoaDon)
   ========================================================= */

USE master;
GO

-- Thông báo 'Nonqualified transactions are being rolled back' khi xóa DB cũ
-- là thông báo tiến trình của SQL Server, không phải lỗi cú pháp của script.
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
    CONSTRAINT UQ_ThuCung_MaKH_MaTC UNIQUE (maKH, maTC),
    CONSTRAINT FK_ThuCung_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
);
GO

-- 7. LỊCH LÀM VIỆC
CREATE TABLE LichLamViec
(
    maLLV        VARCHAR(20) NOT NULL,
    maNhanVien   VARCHAR(20) NULL,
    maBacSi      VARCHAR(20) NULL,
    ngay         DATE,
    gioBatDau    TIME,
    gioKetThuc   TIME,
    trangThai    NVARCHAR(50),

    CONSTRAINT PK_LichLamViec PRIMARY KEY (maLLV),
    CONSTRAINT FK_LichLamViec_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_LichLamViec_BacSi FOREIGN KEY (maBacSi) REFERENCES BacSyThuY(maBacSi),
    CONSTRAINT CK_LichLamViec_NhanSu CHECK (
        (maNhanVien IS NOT NULL AND maBacSi IS NULL)
        OR (maNhanVien IS NULL AND maBacSi IS NOT NULL)
    ),
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

-- 17. HÓA ĐƠN
CREATE TABLE HoaDon
(
    maHD         VARCHAR(20)  NOT NULL,
    ngayLap      DATETIME2    NOT NULL,
    trangThai    NVARCHAR(50) NOT NULL,
    maKH         VARCHAR(20)  NOT NULL,
    maTC         VARCHAR(20)  NULL,
    maNhanVien   VARCHAR(20)  NOT NULL,
    maKM         VARCHAR(20)  NULL,
    maThue       VARCHAR(20)  NULL,
    phuongThucThanhToan  NVARCHAR(50) NULL,

    CONSTRAINT PK_HoaDon PRIMARY KEY (maHD),
    CONSTRAINT UQ_HoaDon_maHD_maKH UNIQUE (maHD, maKH),
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_HoaDon_ThuCung_KhachHang FOREIGN KEY (maKH, maTC) REFERENCES ThuCung(maKH, maTC),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_HoaDon_KhuyenMai FOREIGN KEY (maKM) REFERENCES KhuyenMai(maKM),
    CONSTRAINT FK_HoaDon_Thue FOREIGN KEY (maThue) REFERENCES Thue(maThue)
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
    maThuoc      VARCHAR(20)   NULL,

    CONSTRAINT PK_VatTu PRIMARY KEY (maVT),
    CONSTRAINT FK_VatTu_NhaCungCap FOREIGN KEY (maNCC) REFERENCES NhaCungCap(maNCC),
    CONSTRAINT FK_VatTu_Thuoc FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc),
    CONSTRAINT CK_VatTu_SoLuong CHECK (soLuongTon >= 0),
    CONSTRAINT CK_VatTu_GiaBan CHECK (giaBanCoBan >= 0),
    CONSTRAINT CK_VatTu_GiaNhap CHECK (giaNhap >= 0)
);
GO
CREATE UNIQUE INDEX UQ_VatTu_Thuoc ON VatTu(maThuoc) WHERE maThuoc IS NOT NULL;
GO

CREATE TRIGGER trg_ChiTietDonThuoc_KiemTraVatTu
ON ChiTietDonThuoc
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted ctdt
        LEFT JOIN VatTu vt ON vt.maThuoc = ctdt.maThuoc
        WHERE vt.maVT IS NULL
    )
    BEGIN
        ;THROW 51000, N'Thuốc kê đơn phải được liên kết với một vật tư trong kho.', 1;
    END;

END;
GO

-- 20. CHI TIẾT HÓA ĐƠN
CREATE TABLE ChiTietHoaDon
(
    maHD       VARCHAR(20)   NOT NULL,
    maKH       VARCHAR(20)   NOT NULL, -- Thêm trường mã khách hàng
    soDong     INT           NOT NULL,
    maDV       VARCHAR(20)   NULL,
    maVT       VARCHAR(20)   NULL,
    maCTDT     VARCHAR(20)   NULL,
    maNhanVien VARCHAR(20)   NULL,
    maBacSi    VARCHAR(20)   NULL,
    soLuong    DECIMAL(18,2) NOT NULL,
    donGia     DECIMAL(18,2) NOT NULL,

    CONSTRAINT PK_ChiTietHoaDon PRIMARY KEY (maHD, maKH, soDong),

    CONSTRAINT FK_ChiTietHoaDon_HoaDon_KhachHang FOREIGN KEY (maHD, maKH) REFERENCES HoaDon(maHD, maKH),
    CONSTRAINT FK_ChiTietHoaDon_DichVu FOREIGN KEY (maDV) REFERENCES DichVu(maDV),
    CONSTRAINT FK_ChiTietHoaDon_VatTu FOREIGN KEY (maVT) REFERENCES VatTu(maVT),
    CONSTRAINT FK_ChiTietHoaDon_ChiTietDonThuoc FOREIGN KEY (maCTDT) REFERENCES ChiTietDonThuoc(maCTDT),
    CONSTRAINT FK_ChiTietHoaDon_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT FK_ChiTietHoaDon_BacSy FOREIGN KEY (maBacSi) REFERENCES BacSyThuY(maBacSi),
    CONSTRAINT CK_ChiTietHoaDon_SoLuong CHECK (soLuong > 0),
    CONSTRAINT CK_ChiTietHoaDon_SoDong CHECK (soDong > 0),
    CONSTRAINT CK_ChiTietHoaDon_DonGia CHECK (donGia >= 0),
    CONSTRAINT CK_ChiTietHoaDon_DV_hoac_VT
        CHECK ((CASE WHEN maDV IS NULL THEN 0 ELSE 1 END) + (CASE WHEN maVT IS NULL THEN 0 ELSE 1 END) = 1)
);
GO

CREATE TRIGGER trg_ChiTietDonThuoc_BaoVeHoaDonDaCap
ON ChiTietDonThuoc
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted ctdt
        JOIN ChiTietHoaDon cthd ON cthd.maCTDT = ctdt.maCTDT
        JOIN VatTu vt ON vt.maVT = cthd.maVT
        WHERE vt.maThuoc <> ctdt.maThuoc
    )
    BEGIN
        ;THROW 51007, N'Không thể đổi thuốc trong đơn đã được cấp trên hóa đơn.', 1;
    END;

    IF EXISTS (
        SELECT 1
        FROM inserted ctdt
        JOIN (
            SELECT maCTDT, SUM(soLuong) AS soLuongDaCap
            FROM ChiTietHoaDon
            WHERE maCTDT IS NOT NULL
            GROUP BY maCTDT
        ) daCap ON daCap.maCTDT = ctdt.maCTDT
        WHERE daCap.soLuongDaCap > ctdt.soLuong
    )
    BEGIN
        ;THROW 51010, N'Số lượng kê không thể thấp hơn số thuốc đã cấp.', 1;
    END;
END;
GO

CREATE TRIGGER trg_DonThuoc_BaoVeHoaDonDaCap
ON DonThuoc
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted dt
        JOIN ChiTietDonThuoc ctdt ON ctdt.maDonThuoc = dt.maDonThuoc
        JOIN ChiTietHoaDon cthd ON cthd.maCTDT = ctdt.maCTDT
        JOIN HoaDon h ON h.maHD = cthd.maHD
        WHERE dt.maTC <> h.maTC
    )
    BEGIN
        ;THROW 51008, N'Không thể đổi thú cưng của đơn thuốc đã được cấp trên hóa đơn.', 1;
    END;
END;
GO

CREATE TRIGGER trg_VatTu_BaoVeLienKetThuoc
ON VatTu
AFTER UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM deleted d
        LEFT JOIN inserted i ON i.maVT = d.maVT
        JOIN ChiTietDonThuoc ctdt ON ctdt.maThuoc = d.maThuoc
        WHERE d.maThuoc IS NOT NULL
          AND (i.maVT IS NULL OR i.maThuoc IS NULL OR i.maThuoc <> d.maThuoc)
    )
    BEGIN
        ;THROW 51009, N'Không thể xóa hoặc đổi liên kết vật tư của thuốc đã kê đơn.', 1;
    END;
END;
GO

CREATE TRIGGER trg_ChiTietHoaDon_DongBoKhoVaDonThuoc
ON ChiTietHoaDon
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN HoaDon h ON h.maHD = i.maHD
        LEFT JOIN VatTu vt ON vt.maVT = i.maVT
        WHERE (i.maDV IS NOT NULL OR vt.maThuoc IS NOT NULL)
          AND h.maTC IS NULL
    )
    BEGIN
        ;THROW 51001, N'Hóa đơn có dịch vụ hoặc thuốc phải gắn với thú cưng.', 1;
    END;

    IF EXISTS (
        SELECT 1
        FROM inserted i
        LEFT JOIN VatTu vt ON vt.maVT = i.maVT
        LEFT JOIN ChiTietDonThuoc ctdt ON ctdt.maCTDT = i.maCTDT
        LEFT JOIN DonThuoc dt ON dt.maDonThuoc = ctdt.maDonThuoc
        JOIN HoaDon h ON h.maHD = i.maHD
        WHERE (vt.maThuoc IS NOT NULL AND (
                    i.maCTDT IS NULL
                    OR ctdt.maThuoc <> vt.maThuoc
                    OR dt.maTC <> h.maTC
                ))
           OR (vt.maThuoc IS NULL AND i.maCTDT IS NOT NULL)
    )
    BEGIN
        ;THROW 51002, N'Thuốc trên hóa đơn phải khớp với đơn thuốc của thú cưng trên hóa đơn.', 1;
    END;

    IF EXISTS (
        SELECT 1
        FROM ChiTietDonThuoc ctdt
        JOIN (
            SELECT maCTDT, SUM(soLuong) AS soLuongDaCap
            FROM ChiTietHoaDon
            WHERE maCTDT IS NOT NULL
            GROUP BY maCTDT
        ) daCap ON daCap.maCTDT = ctdt.maCTDT
        WHERE daCap.soLuongDaCap > ctdt.soLuong
    )
    BEGIN
        ;THROW 51003, N'Số lượng thuốc bán vượt quá số lượng được kê.', 1;
    END;

    DECLARE @BienDong TABLE (maVT VARCHAR(20) PRIMARY KEY, soLuong DECIMAL(18,2));

    INSERT INTO @BienDong (maVT, soLuong)
    SELECT thayDoi.maVT, SUM(thayDoi.soLuong)
    FROM (
        SELECT maVT, CAST(soLuong AS DECIMAL(18,2)) AS soLuong
        FROM inserted
        WHERE maVT IS NOT NULL
        UNION ALL
        SELECT maVT, -CAST(soLuong AS DECIMAL(18,2))
        FROM deleted
        WHERE maVT IS NOT NULL
    ) thayDoi
    GROUP BY thayDoi.maVT
    HAVING SUM(thayDoi.soLuong) <> 0;

    IF EXISTS (
        SELECT 1
        FROM VatTu vt
        JOIN @BienDong bd ON bd.maVT = vt.maVT
        WHERE vt.soLuongTon - bd.soLuong < 0
    )
    BEGIN
        ;THROW 51004, N'Số lượng vật tư tồn kho không đủ.', 1;
    END;

    UPDATE vt
    SET soLuongTon = vt.soLuongTon - bd.soLuong,
        trangThai = CASE
            WHEN vt.soLuongTon - bd.soLuong <= 0 THEN N'Hết tồn'
            WHEN vt.soLuongTon - bd.soLuong <= 30 THEN N'Sắp hết'
            ELSE N'Còn hàng'
        END
    FROM VatTu vt
    JOIN @BienDong bd ON bd.maVT = vt.maVT;
END;
GO

CREATE TRIGGER trg_HoaDon_ThuCungBatBuoc
ON HoaDon
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM inserted h
        JOIN ChiTietHoaDon cthd ON cthd.maHD = h.maHD
        LEFT JOIN VatTu vt ON vt.maVT = cthd.maVT
        WHERE (cthd.maDV IS NOT NULL OR vt.maThuoc IS NOT NULL)
          AND h.maTC IS NULL
    )
    BEGIN
        ;THROW 51005, N'Hóa đơn có dịch vụ hoặc thuốc phải gắn với thú cưng.', 1;
    END;

    IF EXISTS (
        SELECT 1
        FROM inserted h
        JOIN ChiTietHoaDon cthd ON cthd.maHD = h.maHD
        JOIN VatTu vt ON vt.maVT = cthd.maVT
        JOIN ChiTietDonThuoc ctdt ON ctdt.maCTDT = cthd.maCTDT
        JOIN DonThuoc dt ON dt.maDonThuoc = ctdt.maDonThuoc
        WHERE vt.maThuoc IS NOT NULL
          AND (ctdt.maThuoc <> vt.maThuoc OR dt.maTC <> h.maTC)
    )
    BEGIN
        ;THROW 51006, N'Đơn thuốc phải thuộc thú cưng trên hóa đơn.', 1;
    END;
END;
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

-- Bảng đếm dùng chung cho dữ liệu mẫu (tối đa 200 bản ghi)
CREATE TABLE #N (n INT PRIMARY KEY);
WITH x AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM x WHERE n < 200
)
INSERT INTO #N(n)
SELECT n FROM x OPTION (MAXRECURSION 0);
GO

-- 1. TÀI KHOẢN (46 tài khoản: 1 Admin, 15 Quản lý, 15 Nhân viên, 15 Bác sĩ)
INSERT INTO TaiKhoan (maTK, tenDangNhap, matKhau, vaiTro, trangThai, maQuanLy)
SELECT
    'TK' + dbo.fn_C3(n),
    CASE
        WHEN n = 1 THEN 'admin'
        WHEN n <= 16 THEN 'quanly' + dbo.fn_C3(n - 1)
        WHEN n <= 31 THEN 'nhanvien' + dbo.fn_C3(n - 16)
        ELSE 'bacsi' + dbo.fn_C3(n - 31)
    END,
    CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', '123456'), 2),
    CASE
        WHEN n = 1 THEN N'Admin'
        WHEN n <= 16 THEN N'Quản lý'
        WHEN n <= 21 THEN N'Nhân viên bán hàng'
        WHEN n <= 26 THEN N'Nhân viên dịch vụ'
        WHEN n <= 31 THEN N'Nhân viên quản lý kho'
        ELSE N'Bác sĩ'
    END,
    1, NULL
FROM #N WHERE n <= 46;
GO

-- 2. QUẢN LÝ CỬA HÀNG (15 quản lý)
INSERT INTO QuanLyCuaHang (maQuanLy, maTK, hoTenQuanLy, sdt, diaChi, ngaySinh, gioiTinh, luongCoBan)
SELECT 'QL' + dbo.fn_C3(nums.n), 'TK' + dbo.fn_C3(nums.n + 1), v.hoTen,
       '0901000' + dbo.fn_C3(nums.n), N'Quận Gò Vấp, TP.HCM',
       DATEADD(DAY, nums.n * 150, '19850510'),
       CASE WHEN nums.n % 2 = 1 THEN N'Nam' ELSE N'Nữ' END,
       18000000 + nums.n * 250000
FROM #N AS nums
JOIN (VALUES
 (1,N'Lê Văn Thành'),(2,N'Nguyễn Văn An'),(3,N'Trần Thị Bình'),
 (4,N'Phạm Minh Cường'),(5,N'Võ Thị Diễm'),(6,N'Đặng Quốc Đạt'),
 (7,N'Bùi Ngọc Hà'),(8,N'Hồ Thanh Hùng'),(9,N'Ngô Thị Lan'),
 (10,N'Dương Minh Long'),(11,N'Lý Thị Mai'),(12,N'Phan Quốc Nam'),
 (13,N'Hoàng Thu Phương'),(14,N'Đỗ Văn Quân'),(15,N'Vũ Thị Trang')
) v(n, hoTen) ON v.n = nums.n
WHERE nums.n <= 15;
GO

-- Gán người quản lý phụ trách cho Admin và các tài khoản nhân viên/bác sĩ
UPDATE TaiKhoan
SET maQuanLy = 'QL001'
WHERE maTK = 'TK001' OR vaiTro LIKE N'Nhân viên%' OR vaiTro = N'Bác sĩ';
GO

-- 3. NHÂN VIÊN (15 nhân viên: 5 bán hàng, 5 dịch vụ, 5 quản lý kho)
WITH NhanVien_Temp AS (
    SELECT n,
           CASE WHEN n <= 5 THEN N'Nhân viên bán hàng'
                WHEN n <= 10 THEN N'Nhân viên dịch vụ'
                ELSE N'Nhân viên quản lý kho' END AS chucVu
    FROM #N WHERE n <= 15
)
INSERT INTO NhanVien (maNhanVien, maTK, hoTen, chucVu, sdt, diaChi, ngaySinh, gioiTinh, luongCoBan)
SELECT 'NV' + dbo.fn_C3(t.n), 'TK' + dbo.fn_C3(t.n + 16), v.hoTen, t.chucVu,
       '0902000' + dbo.fn_C3(t.n), N'Quận Gò Vấp, TP.HCM',
       DATEADD(DAY, t.n * 263, '19950115'),
       CASE WHEN t.n % 2 = 1 THEN N'Nam' ELSE N'Nữ' END,
       CASE t.chucVu WHEN N'Nhân viên bán hàng' THEN 45000
                     WHEN N'Nhân viên dịch vụ' THEN 60000
                     ELSE 13500000 END
FROM NhanVien_Temp t
JOIN (VALUES
 (1,N'Trần Quốc Bảo'),(2,N'Nguyễn Thị Cẩm'),(3,N'Lê Văn Dũng'),
 (4,N'Phạm Hồng Em'),(5,N'Võ Văn Phúc'),(6,N'Đặng Thị Giang'),
 (7,N'Bùi Minh Hải'),(8,N'Hồ Thị Hạnh'),(9,N'Ngô Văn Khoa'),
 (10,N'Dương Thị Linh'),(11,N'Lý Quang Minh'),(12,N'Phan Thị Ngân'),
 (13,N'Hoàng Anh Tuấn'),(14,N'Đỗ Ngọc Yến'),(15,N'Vũ Thành Đạt')
) v(n, hoTen) ON v.n = t.n;
GO

-- 4. CHUYÊN NGÀNH BÁC SĨ (15 chuyên ngành mẫu)
INSERT INTO ChuyenNganh (maChuyenNganh, tenChuyenNganh)
VALUES
('CN001',N'Nội khoa'),('CN002',N'Ngoại khoa'),('CN003',N'Da liễu thú y'),
('CN004',N'Răng hàm mặt'),('CN005',N'Sản khoa'),('CN006',N'Nhãn khoa'),
('CN007',N'Tim mạch'),('CN008',N'Tiêu hóa'),('CN009',N'Tai mũi họng'),
('CN010',N'Thần kinh'),('CN011',N'Dinh dưỡng thú y'),('CN012',N'Truyền nhiễm'),
('CN013',N'Chẩn đoán hình ảnh'),('CN014',N'Hồi sức cấp cứu'),('CN015',N'Tiêm chủng và phòng bệnh');
GO

-- 5. BÁC SĨ THÚ Y (15 bác sĩ)
INSERT INTO BacSyThuY (maBacSi, maTK, maChuyenNganh, hoTen, gioiTinh, sdt, email, ngaySinh, luongThang)
SELECT 'BS' + dbo.fn_C3(nums.n), 'TK' + dbo.fn_C3(nums.n + 31),
       'CN' + dbo.fn_C3(nums.n), v.hoTen,
       CASE WHEN nums.n % 2 = 1 THEN N'Nam' ELSE N'Nữ' END,
       '0903000' + dbo.fn_C3(nums.n), 'bacsi' + dbo.fn_C3(nums.n) + '@salala.vn',
       DATEADD(DAY, nums.n * 301, '19900301'), 16000000 + nums.n * 1000000
FROM #N AS nums
JOIN (VALUES
 (1,N'Nguyễn Hữu Tài'),(2,N'Trần Thị Uyên'),(3,N'Lê Minh Vinh'),
 (4,N'Phạm Quốc Bảo'),(5,N'Võ Ngọc Anh'),(6,N'Đặng Minh Khoa'),
 (7,N'Bùi Thị Thu'),(8,N'Hồ Quốc Việt'),(9,N'Ngô Thanh Tâm'),
 (10,N'Dương Hoàng Phúc'),(11,N'Lý Minh Châu'),(12,N'Phan Văn Khánh'),
 (13,N'Hoàng Thị Như'),(14,N'Đỗ Thành Công'),(15,N'Vũ Ngọc Diệp')
) v(n, hoTen) ON v.n = nums.n
WHERE nums.n <= 15;
GO

-- 6. KHÁCH HÀNG (15 khách hàng)
INSERT INTO KhachHang (maKH, hoTen, sdt, email, diaChi)
SELECT 'KH' + dbo.fn_C3(nums.n), v.hoTen,
       '0904000' + dbo.fn_C3(nums.n), 'khachhang' + dbo.fn_C3(nums.n) + '@gmail.com',
       N'Đường Nguyễn Oanh, Gò Vấp, TP.HCM'
FROM #N AS nums
JOIN (VALUES
 (1,N'Nguyễn Thị Hoa'),(2,N'Trần Văn Hùng'),(3,N'Lê Thị Kim'),
 (4,N'Phạm Văn Lâm'),(5,N'Võ Thị My'),(6,N'Đặng Văn Nghĩa'),
 (7,N'Bùi Thị Oanh'),(8,N'Hồ Văn Phong'),(9,N'Ngô Thị Quyên'),
 (10,N'Dương Văn Sĩ'),(11,N'Lý Minh Tâm'),(12,N'Phan Thị Vân'),
 (13,N'Hoàng Quốc Việt'),(14,N'Đỗ Thu Hà'),(15,N'Vũ Thành Trung')
) v(n, hoTen) ON v.n = nums.n
WHERE nums.n <= 15;
GO

-- 7. THÚ CƯNG (15 thú cưng)
INSERT INTO ThuCung (maTC, maKH, tenTC, loai, gioiTinh, ngaySinh, chuHo, dacDiemNhanDien)
SELECT 'TC' + dbo.fn_C3(nums.n), k.maKH, v.tenTC,
       CHOOSE(nums.n % 4 + 1, N'Chó', N'Mèo', N'Thỏ', N'Hamster'),
       CASE WHEN nums.n % 2 = 1 THEN N'Đực' ELSE N'Cái' END,
       DATEADD(MONTH, -nums.n * 3, '20251001'), k.hoTen, N'Lông khỏe mạnh, lanh lợi'
FROM #N AS nums
JOIN KhachHang k ON k.maKH = 'KH' + dbo.fn_C3(nums.n)
JOIN (VALUES
 (1,N'Mimi'),(2,N'Lucky'),(3,N'Bông'),(4,N'Milo'),(5,N'Kitty'),
 (6,N'Boss'),(7,N'Susu'),(8,N'Nâu'),(9,N'Tom'),(10,N'Mực'),
 (11,N'Bắp'),(12,N'Đậu'),(13,N'Kem'),(14,N'Na'),(15,N'Cookie')
) v(n, tenTC) ON v.n = nums.n
WHERE nums.n <= 15;
GO

-- 8. LỊCH LÀM VIỆC (450 ca của 15 nhân viên trong 30 ngày)
INSERT INTO LichLamViec (maLLV, maNhanVien, ngay, gioBatDau, gioKetThuc, trangThai)
SELECT 'LLV' + RIGHT('0000' + CAST(ROW_NUMBER() OVER (ORDER BY nv.maNhanVien, d.n) AS VARCHAR(4)), 4),
       nv.maNhanVien, DATEADD(DAY, d.n - 1, '2026-10-01'),
       CAST('08:00' AS TIME), CAST('17:00' AS TIME), N'Hoàn thành'
FROM NhanVien nv CROSS JOIN (SELECT n FROM #N WHERE n <= 30) d;
GO

-- 9. DỊCH VỤ (15 dịch vụ)
INSERT INTO DichVu (maDV, tenDV, moTa, giaDV, thoiGian, trangThai)
SELECT 'DV' + dbo.fn_C3(n), tenDV, N'Dịch vụ chăm sóc thú cưng: ' + tenDV,
       giaDV, thoiGian, N'Đang áp dụng'
FROM (VALUES
 (1,N'Khám tổng quát',150000,CAST('00:30:00' AS TIME)),
 (2,N'Tiêm phòng dại',120000,CAST('00:20:00' AS TIME)),
 (3,N'Tắm vệ sinh thú cưng',100000,CAST('01:00:00' AS TIME)),
 (4,N'Cắt tỉa lông',200000,CAST('01:00:00' AS TIME)),
 (5,N'Cạo vôi răng',300000,CAST('00:45:00' AS TIME)),
 (6,N'Khám da liễu',180000,CAST('00:40:00' AS TIME)),
 (7,N'Siêu âm',250000,CAST('00:30:00' AS TIME)),
 (8,N'Xét nghiệm máu',350000,CAST('00:45:00' AS TIME)),
 (9,N'Vệ sinh tai',80000,CAST('00:20:00' AS TIME)),
 (10,N'Cắt móng',50000,CAST('00:15:00' AS TIME)),
 (11,N'Tẩy giun',90000,CAST('00:20:00' AS TIME)),
 (12,N'Lưu chuồng theo ngày',180000,CAST('01:00:00' AS TIME)),
 (13,N'Chăm sóc hậu phẫu',220000,CAST('00:45:00' AS TIME)),
 (14,N'Tư vấn dinh dưỡng',100000,CAST('00:30:00' AS TIME)),
 (15,N'Vệ sinh răng miệng',160000,CAST('00:30:00' AS TIME))
) v(n,tenDV,giaDV,thoiGian);
GO

-- 10. NHÀ CUNG CẤP (15 nhà cung cấp)
INSERT INTO NhaCungCap (maNCC, tenNCC, diaChi, sdt, email)
SELECT 'NCC' + dbo.fn_C3(n), tenNCC, diaChi,
       '0283800' + dbo.fn_C3(n), 'lienhe' + dbo.fn_C3(n) + '@ncc.vn'
FROM (VALUES
 (1,N'Royal Canin Việt Nam',N'Quận 1, TP.HCM'),(2,N'PetCare Sài Gòn',N'Quận 3, TP.HCM'),
 (3,N'Thú Y Xanh',N'Quận 5, TP.HCM'),(4,N'VietPet Supply',N'Tân Bình, TP.HCM'),
 (5,N'An Phú Veterinary',N'Bình Thạnh, TP.HCM'),(6,N'Happy Paws',N'Gò Vấp, TP.HCM'),
 (7,N'Pet Nutrition Việt',N'Thủ Đức, TP.HCM'),(8,N'Nam Việt Pharma',N'Quận 10, TP.HCM'),
 (9,N'Thiết Bị Thú Y Á Châu',N'Quận 7, TP.HCM'),(10,N'Green Pet',N'Phú Nhuận, TP.HCM'),
 (11,N'Vật Tư Chăn Nuôi Minh Tâm',N'Bình Tân, TP.HCM'),(12,N'BioVet',N'Tân Phú, TP.HCM'),
 (13,N'Pet House',N'Quận 6, TP.HCM'),(14,N'CareVet',N'Quận 11, TP.HCM'),
 (15,N'Thú Cưng Đại Phát',N'Bình Chánh, TP.HCM')
) v(n,tenNCC,diaChi);
GO

-- 11. THUỐC (15 loại thuốc)
INSERT INTO Thuoc (maThuoc, tenThuoc, donViTinh, huongDanSuDung)
SELECT 'TH' + dbo.fn_C3(n), tenThuoc, donViTinh, huongDan
FROM (VALUES
 (1,N'Amoxicillin 250mg',N'Viên',N'Dùng theo chỉ định bác sĩ'),
 (2,N'Cefalexin 500mg',N'Viên',N'Dùng theo chỉ định bác sĩ'),
 (3,N'Men tiêu hóa Probiotic',N'Gói',N'Trộn cùng thức ăn'),
 (4,N'Vitamin B Complex',N'Viên',N'Dùng theo chỉ định bác sĩ'),
 (5,N'Ivermectin 1%',N'Ml',N'Dùng theo cân nặng'),
 (6,N'Prednisolone 5mg',N'Viên',N'Dùng theo chỉ định bác sĩ'),
 (7,N'Metronidazole 250mg',N'Viên',N'Dùng theo chỉ định bác sĩ'),
 (8,N'Enrofloxacin 50mg',N'Viên',N'Dùng theo chỉ định bác sĩ'),
 (9,N'Vitamin C thú y',N'Viên',N'Dùng theo hướng dẫn'),
 (10,N'Canxi bổ sung',N'Viên',N'Dùng theo hướng dẫn'),
 (11,N'Thuốc nhỏ mắt thú y',N'Lọ',N'Nhỏ theo chỉ định'),
 (12,N'Dung dịch sát khuẩn',N'Chai',N'Dùng ngoài da'),
 (13,N'Thuốc trị ve rận',N'Ống',N'Dùng theo cân nặng'),
 (14,N'Điện giải thú y',N'Gói',N'Pha theo hướng dẫn'),
 (15,N'Thuốc hỗ trợ gan',N'Viên',N'Dùng theo chỉ định bác sĩ')
) v(n,tenThuoc,donViTinh,huongDan);
GO

-- 12. VẬT TƯ (15 hàng hóa thông thường + 15 mặt hàng thuốc = 30)
INSERT INTO VatTu (maVT, maNCC, tenVT, soLuongTon, trangThai, giaBanCoBan, giaNhap, maThuoc)
SELECT 'VT' + dbo.fn_C3(n), 'NCC' + dbo.fn_C3(((n - 1) % 15) + 1), tenVT,
       50 + n * 3, N'Còn hàng', giaBan, giaNhap, NULL
FROM (VALUES
 (1,N'Thức ăn hạt cho chó 2kg',250000,200000),(2,N'Thức ăn hạt cho mèo 1.5kg',220000,180000),
 (3,N'Sữa tắm thú cưng 500ml',150000,110000),(4,N'Pate mèo 85g',25000,18000),
 (5,N'Xương gặm sạch răng',60000,45000),(6,N'Vòng cổ chó mèo',85000,55000),
 (7,N'Dây dắt thú cưng',120000,80000),(8,N'Khay vệ sinh mèo',180000,130000),
 (9,N'Cát vệ sinh mèo 5kg',95000,70000),(10,N'Balo vận chuyển thú cưng',450000,350000),
 (11,N'Bát ăn inox',70000,45000),(12,N'Đồ chơi bóng cao su',45000,25000),
 (13,N'Lược chải lông',65000,40000),(14,N'Tã lót thú cưng',110000,80000),
 (15,N'Chuồng vận chuyển mini',550000,430000)
) v(n,tenVT,giaBan,giaNhap);

INSERT INTO VatTu (maVT, maNCC, tenVT, soLuongTon, trangThai, giaBanCoBan, giaNhap, maThuoc)
SELECT 'VT' + dbo.fn_C3(nums.n + 15), 'NCC' + dbo.fn_C3(((nums.n - 1) % 15) + 1),
       t.tenThuoc, 100, N'Còn hàng', 5000 + nums.n * 2500, 3000 + nums.n * 1500,
       'TH' + dbo.fn_C3(nums.n)
FROM #N AS nums JOIN Thuoc t ON t.maThuoc = 'TH' + dbo.fn_C3(nums.n)
WHERE nums.n <= 15;
GO

-- 13. HỒ SƠ SỨC KHỎE (15 hồ sơ)
INSERT INTO HoSoSucKhoe (maHSSK, maTC, maBacSi, ngayKham, trieuChung, chuanDoan, canNang)
SELECT 'HS' + dbo.fn_C3(n), 'TC' + dbo.fn_C3(n), 'BS' + dbo.fn_C3(n),
       DATEADD(DAY, n, '2026-10-01'), N'Mệt mỏi, biếng ăn', N'Cần theo dõi và tái khám', 2.5 + n * 0.7
FROM #N WHERE n <= 15;
GO

-- 14. ĐƠN THUỐC VÀ CHI TIẾT ĐƠN THUỐC (mỗi bảng 15 bản ghi)
INSERT INTO DonThuoc (maDonThuoc, maTC, maBacSi, maHSSK, ngayKeDon, ghiChu, trangThai)
SELECT 'DT' + dbo.fn_C3(n), 'TC' + dbo.fn_C3(n), 'BS' + dbo.fn_C3(n),
       'HS' + dbo.fn_C3(n), DATEADD(DAY, n, '2026-10-02'), N'Uống đủ liều theo chỉ định', N'Hoàn thành'
FROM #N WHERE n <= 15;

INSERT INTO ChiTietDonThuoc (maCTDT, maDonThuoc, maThuoc, soLuong, lieuLuong)
SELECT 'CTDT' + dbo.fn_C3(n), 'DT' + dbo.fn_C3(n), 'TH' + dbo.fn_C3(n), 10, 2
FROM #N WHERE n <= 15;
GO

-- 15. KHUYẾN MÃI (15 chương trình mẫu; thời gian áp dụng trong năm 2026)
INSERT INTO KhuyenMai (maKM, tenKM, mucKM, ngayBatDau, ngayKetThuc, dieuKien)
VALUES
('KM001',N'Ưu đãi khách mới',5,'2026-01-01','2026-12-31',N'Áp dụng cho khách hàng lần đầu'),
('KM002',N'Ưu đãi hóa đơn từ 200 nghìn',10,'2026-01-01','2026-12-31',N'Áp dụng cho hóa đơn từ 200.000 đồng'),
('KM003',N'Tri ân hóa đơn từ 500 nghìn',15,'2026-01-01','2026-12-31',N'Áp dụng cho hóa đơn từ 500.000 đồng'),
('KM004',N'Ưu đãi khách hàng thành viên',8,'2026-02-01','2026-12-31',N'Áp dụng cho khách hàng thành viên'),
('KM005',N'Ưu đãi thức ăn thú cưng',7,'2026-03-01','2026-12-31',N'Áp dụng cho nhóm thức ăn'),
('KM006',N'Ưu đãi phụ kiện',10,'2026-04-01','2026-12-31',N'Áp dụng cho nhóm phụ kiện'),
('KM007',N'Ưu đãi dịch vụ tắm',12,'2026-05-01','2026-12-31',N'Áp dụng cho dịch vụ tắm vệ sinh'),
('KM008',N'Ưu đãi tiêm phòng',5,'2026-06-01','2026-12-31',N'Áp dụng cho dịch vụ tiêm phòng'),
('KM009',N'Ưu đãi khám tổng quát',10,'2026-07-01','2026-12-31',N'Áp dụng cho dịch vụ khám tổng quát'),
('KM010',N'Ưu đãi sinh nhật thú cưng',15,'2026-08-01','2026-12-31',N'Áp dụng trong tháng sinh nhật thú cưng'),
('KM011',N'Ưu đãi khách hàng thân thiết',18,'2026-09-01','2026-12-31',N'Áp dụng cho khách hàng thân thiết'),
('KM012',N'Ưu đãi cuối tuần',6,'2026-09-01','2026-12-31',N'Áp dụng vào thứ Bảy và Chủ nhật'),
('KM013',N'Ưu đãi combo chăm sóc',20,'2026-10-01','2026-12-31',N'Áp dụng cho combo dịch vụ'),
('KM014',N'Ưu đãi mua hàng số lượng lớn',10,'2026-10-01','2026-12-31',N'Áp dụng cho đơn hàng số lượng lớn'),
('KM015',N'Khuyến mãi chào năm mới',25,'2026-11-01','2027-01-15',N'Chương trình ưu đãi cuối năm');
GO

-- 16. THUẾ (15 hồ sơ thuế mẫu để kiểm thử nhiều cấu hình mức thuế)
-- Lưu ý: đây là dữ liệu giả lập phục vụ kiểm thử phần mềm, không phải bảng tư vấn thuế/phân loại thuế pháp lý.
INSERT INTO Thue (maThue, tenThue, mucThue)
VALUES
('TX001',N'Mẫu thuế 01 - 0%',0.00),('TX002',N'Mẫu thuế 02 - 5%',5.00),
('TX003',N'Mẫu thuế 03 - 8%',8.00),('TX004',N'Mẫu thuế 04 - 10%',10.00),
('TX005',N'Mẫu thuế 05 - 0%',0.00),('TX006',N'Mẫu thuế 06 - 5%',5.00),
('TX007',N'Mẫu thuế 07 - 8%',8.00),('TX008',N'Mẫu thuế 08 - 10%',10.00),
('TX009',N'Mẫu thuế 09 - 0%',0.00),('TX010',N'Mẫu thuế 10 - 5%',5.00),
('TX011',N'Mẫu thuế 11 - 8%',8.00),('TX012',N'Mẫu thuế 12 - 10%',10.00),
('TX013',N'Mẫu thuế 13 - 0%',0.00),('TX014',N'Mẫu thuế 14 - 5%',5.00),
('TX015',N'Mẫu thuế 15 - 8%',8.00);
GO

-- 17. HÓA ĐƠN (75 bản ghi)
INSERT INTO HoaDon (maHD, ngayLap, trangThai, maKH, maTC, maNhanVien, maKM, maThue,
                    phuongThucThanhToan)
SELECT 'HD' + dbo.fn_C3(n),
       DATEADD(HOUR, 8 + (n % 10), DATEADD(DAY, n % 31, '2026-10-01')),
       N'Đã thanh toán',
       'KH' + dbo.fn_C3(((n - 1) % 15) + 1),
       'TC' + dbo.fn_C3(((n - 1) % 15) + 1),
       'NV' + dbo.fn_C3(((n - 1) % 15) + 1),
       'KM' + dbo.fn_C3(((n - 1) % 15) + 1),
       'TX' + dbo.fn_C3(((n - 1) % 15) + 1),
       CHOOSE((n % 3) + 1, N'Tiền mặt', N'Chuyển khoản', N'Ví điện tử')
FROM #N WHERE n <= 75;

-- Chi tiết dịch vụ: 75 dòng
INSERT INTO ChiTietHoaDon (maHD, maKH, soDong, maDV, maVT, maNhanVien, maBacSi, soLuong, donGia)
SELECT 'HD' + dbo.fn_C3(n),
       'KH' + dbo.fn_C3(((n - 1) % 15) + 1), 1,
       'DV' + dbo.fn_C3(((n - 1) % 15) + 1), NULL,
       'NV' + dbo.fn_C3(((n - 1) % 15) + 1),
       'BS' + dbo.fn_C3(((n - 1) % 15) + 1), 1,
       100000 + (n % 5) * 50000
FROM #N WHERE n <= 75;

-- Chi tiết hàng hóa: 37 dòng, dùng VT001-VT015 (nhóm hàng không phải thuốc)
INSERT INTO ChiTietHoaDon (maHD, maKH, soDong, maDV, maVT, maNhanVien, maBacSi, soLuong, donGia)
SELECT 'HD' + dbo.fn_C3(n),
       'KH' + dbo.fn_C3(((n - 1) % 15) + 1), 2, NULL,
       'VT' + dbo.fn_C3(((n - 1) % 15) + 1),
       'NV' + dbo.fn_C3(((n - 1) % 15) + 1), NULL,
       (n % 3) + 1, 50000 + (n % 4) * 25000
FROM #N WHERE n <= 37;
GO

-- 18. PHIẾU XUẤT KHO VÀ CHI TIẾT (15 phiếu, 30 dòng chi tiết)
INSERT INTO PhieuXuatKho (maPXK, ngayXuat, ghiChu, trangThai, maNhanVien)
SELECT 'PXK' + dbo.fn_C3(n), DATEADD(DAY,n,'2026-10-01'),
       N'Xuất kho phục vụ bán hàng và hoạt động phòng khám', N'Hoàn thành',
       'NV' + dbo.fn_C3(n)
FROM #N WHERE n <= 15;

INSERT INTO ChiTietPhieuXuatKho (maCTPXK, maPXK, maVT, soLuong)
SELECT 'CTPXK' + dbo.fn_C3((p.n - 1) * 2 + d.stt),
       'PXK' + dbo.fn_C3(p.n),
       'VT' + dbo.fn_C3(((p.n + d.stt - 2) % 30) + 1), 1 + d.stt
FROM #N p CROSS JOIN (VALUES (1),(2)) d(stt)
WHERE p.n <= 15;
GO

-- 19. PHIẾU NHẬP KHO VÀ CHI TIẾT (15 phiếu, 30 dòng chi tiết)
INSERT INTO PhieuNhapKho (maPNK, ngayNhap, ghiChu, trangThai, maNhanVien, maNCC)
SELECT 'PNK' + dbo.fn_C3(n), DATEADD(DAY,n,'2026-10-01'),
       N'Nhập hàng bổ sung định kỳ', N'Hoàn thành',
       'NV' + dbo.fn_C3(n), 'NCC' + dbo.fn_C3(n)
FROM #N WHERE n <= 15;

INSERT INTO ChiTietPhieuNhapKho (maCTPNK, maPNK, maVT, donGia, soLuong)
SELECT 'CTPNK' + dbo.fn_C3((p.n - 1) * 2 + d.stt),
       'PNK' + dbo.fn_C3(p.n),
       'VT' + dbo.fn_C3(((p.n + d.stt - 2) % 30) + 1),
       30000 + p.n * 5000, 10 + d.stt
FROM #N p CROSS JOIN (VALUES (1),(2)) d(stt)
WHERE p.n <= 15;
GO

-- 20. QUẢN LÝ HẠNG THÀNH VIÊN (15 khách hàng; 4 hạng được phân bổ theo chi tiêu)
INSERT INTO QuanLyHangThanhVien
       (maHang, maKH, tenHang, chiTieuToiThieu, chiTieuToiDa, mucGiamGia, diemTichLuy1Van,
        quyenLoi, tongChiTieu, diemTichLuy, ngayCapNhatHang, trangThai)
SELECT 'HTV' + dbo.fn_C3(n), k.maKH,
       t.tenHang, t.chiTieuToiThieu, t.chiTieuToiDa, t.mucGiamGia, t.diemTichLuy1Van,
       t.quyenLoi, c.tongChiTieu,
       CAST(FLOOR(c.tongChiTieu / 10000.0 * t.diemTichLuy1Van) AS INT),
       ISNULL(c.ngayGanNhat, CAST('2026-10-01' AS DATETIME2)), N'Đang áp dụng'
FROM #N
JOIN KhachHang k ON k.maKH = 'KH' + dbo.fn_C3(n)
OUTER APPLY (
    SELECT ISNULL(SUM(v.tongThanhToan), 0) AS tongChiTieu,
           MAX(h.ngayLap) AS ngayGanNhat
    FROM HoaDon h JOIN vw_TongTienHoaDon v ON v.maHD = h.maHD
    WHERE h.maKH = k.maKH
) c
JOIN (VALUES
    (N'Đồng',CAST(0 AS DECIMAL(18,2)),CAST(5000000 AS DECIMAL(18,2)),2.00,1.0,N'Giảm 2%, tích điểm và nhắc lịch tiêm'),
    (N'Bạc',CAST(5000000 AS DECIMAL(18,2)),CAST(15000000 AS DECIMAL(18,2)),5.00,1.2,N'Giảm 5%, ưu tiên lịch hẹn và vệ sinh tai'),
    (N'Vàng',CAST(15000000 AS DECIMAL(18,2)),CAST(30000000 AS DECIMAL(18,2)),10.00,1.5,N'Giảm 10%, khám tổng quát và giao hàng ưu đãi'),
    (N'Kim cương',CAST(30000000 AS DECIMAL(18,2)),CAST(NULL AS DECIMAL(18,2)),15.00,2.0,N'Giảm 15%, ưu tiên hotline và khám tổng quát')
) t(tenHang,chiTieuToiThieu,chiTieuToiDa,mucGiamGia,diemTichLuy1Van,quyenLoi)
  ON c.tongChiTieu >= t.chiTieuToiThieu
 AND (t.chiTieuToiDa IS NULL OR c.tongChiTieu < t.chiTieuToiDa)
WHERE n <= 15;
GO

DROP TABLE #N;
DROP FUNCTION dbo.fn_C3;
GO

-- 21. DỮ LIỆU MẪU LỊCH HẸN (15 lịch hẹn)
INSERT INTO LichHen (maLichHen, maTC, maKH, maNhanVien, maDV, ngayGio, lyDo, trangThai, ghiChu, loaiHen)
VALUES
('LH001','TC001','KH001','NV001','DV001','2026-11-01 08:30:00',N'Khám tổng quát',N'Đã khám',NULL,N'Khám bệnh'),
('LH002','TC002','KH002','NV002','DV002','2026-11-01 10:00:00',N'Tiêm phòng dại',N'Đã khám',NULL,N'Khám bệnh'),
('LH003','TC003','KH003','NV003','DV003','2026-11-02 09:00:00',N'Tắm vệ sinh',N'Đã khám',N'Khách đến sớm',N'Dịch vụ'),
('LH004','TC004','KH004','NV004','DV004','2026-11-02 10:30:00',N'Cắt tỉa lông',N'Đang khám',NULL,N'Dịch vụ'),
('LH005','TC005','KH005','NV005','DV005','2026-11-02 14:00:00',N'Cạo vôi răng',N'Chờ khám',N'Bé hơi nhát',N'Khám bệnh'),
('LH006','TC006','KH006','NV006','DV006','2026-11-02 15:30:00',N'Khám tiêu hóa',N'Chờ khám',NULL,N'Khám bệnh'),
('LH007','TC007','KH007','NV007',NULL,'2026-11-02 16:30:00',N'Tái khám',N'Đã hủy',N'Khách bận đột xuất',N'Khám bệnh'),
('LH008','TC008','KH008','NV008','DV008','2026-11-03 08:30:00',N'Tiêm vắc xin',N'Chờ khám',NULL,N'Khám bệnh'),
('LH009','TC009','KH009','NV009','DV009','2026-11-04 09:30:00',N'Vệ sinh tai',N'Chờ khám',NULL,N'Dịch vụ'),
('LH010','TC010','KH010','NV010','DV010','2026-11-05 10:00:00',N'Cắt móng',N'Chờ khám',N'Bé cần làm quen',N'Dịch vụ'),
('LH011','TC011','KH011','NV011','DV011','2026-11-06 14:30:00',N'Tẩy giun',N'Chờ khám',NULL,N'Khám bệnh'),
('LH012','TC012','KH012','NV012','DV012','2026-11-08 15:00:00',N'Lưu chuồng',N'Chờ khám',NULL,N'Dịch vụ'),
('LH013','TC013','KH013','NV013','DV013','2026-11-09 09:00:00',N'Chăm sóc hậu phẫu',N'Chờ khám',NULL,N'Dịch vụ'),
('LH014','TC014','KH014','NV014','DV014','2026-11-10 10:00:00',N'Tư vấn dinh dưỡng',N'Chờ khám',NULL,N'Dịch vụ'),
('LH015','TC015','KH015','NV015','DV015','2026-11-11 14:00:00',N'Vệ sinh răng miệng',N'Chờ khám',NULL,N'Dịch vụ');
GO

/* =========================================================
   KIỂM TRA DỮ LIỆU CÁC BẢNG
   ========================================================= */
;WITH DemDong AS (
    SELECT t.name AS TenBang, SUM(p.rows) AS SoDong
    FROM sys.tables t
    JOIN sys.partitions p ON p.object_id = t.object_id AND p.index_id IN (0, 1)
    WHERE t.is_ms_shipped = 0
    GROUP BY t.name
)
SELECT TenBang, SoDong,
       CASE WHEN SoDong >= 15 THEN N'ĐẠT' ELSE N'CHƯA ĐẠT' END AS KiemTra
FROM DemDong
ORDER BY TenBang;

IF EXISTS (
    SELECT 1
    FROM sys.tables t
    JOIN sys.partitions p ON p.object_id = t.object_id AND p.index_id IN (0, 1)
    WHERE t.is_ms_shipped = 0
    GROUP BY t.object_id
    HAVING SUM(p.rows) < 15
)
BEGIN
    ;THROW 51020, N'Còn bảng có ít hơn 15 bản ghi. Vui lòng kiểm tra dữ liệu mẫu.', 1;
END;
GO

PRINT '========================================================';
PRINT 'ĐÃ XÓA HOÀN TOÀN BẢNG LUONGCHUCVU VÀ GÁN DỮ LIỆU THÀNH CÔNG!';
PRINT 'CẬP NHẬT: CHI TIẾT HÓA ĐƠN SỬ DỤNG KHÓA (maHD, maKH)';
PRINT '========================================================';
GO
