/*
   Migration an toàn cho cơ sở dữ liệu SALALA đang tồn tại.
   Không chạy script khởi tạo drop/create database. Chỉ thêm các cột cần thiết
   cho tính lương theo chức danh và người thực hiện từng dịch vụ.
*/

USE SALALA;
GO

IF COL_LENGTH('NhanVien', 'loaiLuong') IS NULL
BEGIN
    ALTER TABLE NhanVien ADD loaiLuong NVARCHAR(20) NULL;
END;
GO

IF COL_LENGTH('NhanVien', 'luongTheoGio') IS NULL
BEGIN
    ALTER TABLE NhanVien ADD luongTheoGio DECIMAL(18,2) NOT NULL CONSTRAINT DF_NhanVien_LuongTheoGio DEFAULT 0;
END;
GO

IF COL_LENGTH('NhanVien', 'luongThang') IS NULL
BEGIN
    ALTER TABLE NhanVien ADD luongThang DECIMAL(18,2) NOT NULL CONSTRAINT DF_NhanVien_LuongThang DEFAULT 0;
END;
GO

IF COL_LENGTH('BacSyThuY', 'luongThang') IS NULL
BEGIN
    ALTER TABLE BacSyThuY ADD luongThang DECIMAL(18,2) NOT NULL CONSTRAINT DF_BacSyThuY_LuongThang DEFAULT 0;
END;
GO

IF COL_LENGTH('ChiTietHoaDon', 'maNhanVien') IS NULL
BEGIN
    ALTER TABLE ChiTietHoaDon ADD maNhanVien VARCHAR(20) NULL;
END;
GO

IF COL_LENGTH('ChiTietHoaDon', 'maBacSi') IS NULL
BEGIN
    ALTER TABLE ChiTietHoaDon ADD maBacSi VARCHAR(20) NULL;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_ChiTietHoaDon_NhanVien'
)
BEGIN
    ALTER TABLE ChiTietHoaDon
        ADD CONSTRAINT FK_ChiTietHoaDon_NhanVien FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien);
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_ChiTietHoaDon_BacSy'
)
BEGIN
    ALTER TABLE ChiTietHoaDon
        ADD CONSTRAINT FK_ChiTietHoaDon_BacSy FOREIGN KEY (maBacSi) REFERENCES BacSyThuY(maBacSi);
END;
GO

UPDATE NhanVien
SET loaiLuong = N'THEO_GIO', luongTheoGio = ISNULL(luongTheoGio, 0), luongThang = ISNULL(luongThang, 0)
WHERE loaiLuong IS NULL;
GO

SELECT 'Migration payroll completed' AS KetQua;
GO
