/*
   Migration an toàn cho cơ sở dữ liệu SALALA đang tồn tại.
   Không chạy script khởi tạo drop/create database. Chỉ thêm các cột liên kết
   người thực hiện dịch vụ vào cơ sở dữ liệu đang có.
*/

USE SALALA;
GO

IF COL_LENGTH('NhanVien', 'loaiLuong') IS NOT NULL
BEGIN
    IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_NhanVien_LoaiLuong')
        ALTER TABLE NhanVien DROP CONSTRAINT CK_NhanVien_LoaiLuong;
    IF EXISTS (SELECT 1 FROM sys.default_constraints WHERE name = 'DF_NhanVien_LoaiLuong')
        ALTER TABLE NhanVien DROP CONSTRAINT DF_NhanVien_LoaiLuong;
    ALTER TABLE NhanVien DROP COLUMN loaiLuong;
END;
GO

IF COL_LENGTH('NhanVien', 'luongTheoGio') IS NOT NULL
BEGIN
    IF EXISTS (SELECT 1 FROM sys.default_constraints WHERE name = 'DF_NhanVien_LuongTheoGio')
        ALTER TABLE NhanVien DROP CONSTRAINT DF_NhanVien_LuongTheoGio;
    ALTER TABLE NhanVien DROP COLUMN luongTheoGio;
END;
GO

IF COL_LENGTH('NhanVien', 'luongThang') IS NOT NULL
BEGIN
    IF EXISTS (SELECT 1 FROM sys.default_constraints WHERE name = 'DF_NhanVien_LuongThang')
        ALTER TABLE NhanVien DROP CONSTRAINT DF_NhanVien_LuongThang;
    ALTER TABLE NhanVien DROP COLUMN luongThang;
END;
GO

IF COL_LENGTH('BacSyThuY', 'luongThang') IS NULL
BEGIN
    ALTER TABLE BacSyThuY ADD luongThang DECIMAL(18,2) NOT NULL CONSTRAINT DF_BacSyThuY_LuongThang DEFAULT 0;
END;
GO

IF OBJECT_ID('LuongChucVu', 'U') IS NULL
BEGIN
    CREATE TABLE LuongChucVu
    (
        chucVu      NVARCHAR(50) NOT NULL,
        luongCoBan  DECIMAL(18,2) NOT NULL,
        CONSTRAINT PK_LuongChucVu PRIMARY KEY (chucVu),
        CONSTRAINT CK_LuongChucVu_Luong CHECK (luongCoBan >= 0)
    );
END;
GO

MERGE LuongChucVu AS target
USING (VALUES
    (N'Nhân viên bán hàng', CAST(8500000 AS DECIMAL(18,2))),
    (N'Nhân viên dịch vụ', CAST(11000000 AS DECIMAL(18,2))),
    (N'Nhân viên quản lý kho', CAST(13500000 AS DECIMAL(18,2))),
    (N'Quản lý', CAST(0 AS DECIMAL(18,2)))
) AS source(chucVu, luongCoBan)
ON target.chucVu = source.chucVu
WHEN MATCHED THEN UPDATE SET luongCoBan = source.luongCoBan
WHEN NOT MATCHED THEN INSERT (chucVu, luongCoBan)
    VALUES (source.chucVu, source.luongCoBan);
GO

UPDATE nv
SET luongCoBan = lc.luongCoBan
FROM NhanVien nv
JOIN LuongChucVu lc ON lc.chucVu = nv.chucVu;
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

SELECT 'Migration payroll completed' AS KetQua;
GO
