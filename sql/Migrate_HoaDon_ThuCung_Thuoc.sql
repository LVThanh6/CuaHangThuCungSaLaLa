USE SALALA;
GO
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
GO

IF COL_LENGTH('dbo.HoaDon', 'maTC') IS NULL
BEGIN
    ALTER TABLE dbo.HoaDon ADD maTC VARCHAR(20) NULL;
END;
GO

IF COL_LENGTH('dbo.VatTu', 'maThuoc') IS NULL
BEGIN
    ALTER TABLE dbo.VatTu ADD maThuoc VARCHAR(20) NULL;
END;
GO

IF COL_LENGTH('dbo.ChiTietHoaDon', 'maCTDT') IS NULL
BEGIN
    ALTER TABLE dbo.ChiTietHoaDon ADD maCTDT VARCHAR(20) NULL;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.key_constraints
    WHERE name = 'UQ_ThuCung_MaKH_MaTC'
      AND parent_object_id = OBJECT_ID('dbo.ThuCung')
)
BEGIN
    ALTER TABLE dbo.ThuCung
        ADD CONSTRAINT UQ_ThuCung_MaKH_MaTC UNIQUE (maKH, maTC);
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_HoaDon_ThuCung_KhachHang'
      AND parent_object_id = OBJECT_ID('dbo.HoaDon')
)
BEGIN
    ALTER TABLE dbo.HoaDon
        ADD CONSTRAINT FK_HoaDon_ThuCung_KhachHang
        FOREIGN KEY (maKH, maTC) REFERENCES dbo.ThuCung(maKH, maTC);
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_VatTu_Thuoc'
      AND parent_object_id = OBJECT_ID('dbo.VatTu')
)
BEGIN
    ALTER TABLE dbo.VatTu
        ADD CONSTRAINT FK_VatTu_Thuoc
        FOREIGN KEY (maThuoc) REFERENCES dbo.Thuoc(maThuoc);
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_ChiTietHoaDon_ChiTietDonThuoc'
      AND parent_object_id = OBJECT_ID('dbo.ChiTietHoaDon')
)
BEGIN
    ALTER TABLE dbo.ChiTietHoaDon
        ADD CONSTRAINT FK_ChiTietHoaDon_ChiTietDonThuoc
        FOREIGN KEY (maCTDT) REFERENCES dbo.ChiTietDonThuoc(maCTDT);
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UQ_VatTu_Thuoc'
      AND object_id = OBJECT_ID('dbo.VatTu')
)
BEGIN
    CREATE UNIQUE INDEX UQ_VatTu_Thuoc
        ON dbo.VatTu(maThuoc)
        WHERE maThuoc IS NOT NULL;
END;
GO
