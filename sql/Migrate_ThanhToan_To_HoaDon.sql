USE SALALA;
GO

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.HoaDon', N'U') IS NULL
        THROW 50001, N'Không tìm thấy bảng HoaDon trong cơ sở dữ liệu SALALA.', 1;

    IF COL_LENGTH(N'dbo.HoaDon', N'phuongThucThanhToan') IS NULL
        ALTER TABLE dbo.HoaDon ADD phuongThucThanhToan NVARCHAR(50) NULL;

    IF OBJECT_ID(N'dbo.ThanhToan', N'U') IS NOT NULL
       AND COL_LENGTH(N'dbo.HoaDon', N'maThanhToan') IS NOT NULL
    BEGIN
        EXEC sys.sp_executesql N'
            UPDATE h
            SET h.trangThai =
                    CASE
                        WHEN tt.trangThai IN (N''Đã thanh toán'', N''Paid'')
                          OR h.trangThai IN (N''Đã thanh toán'', N''Paid'')
                        THEN N''Đã thanh toán''
                        ELSE N''Chờ thanh toán''
                    END,
                h.phuongThucThanhToan = COALESCE(tt.phuongThuc, h.phuongThucThanhToan)
            FROM dbo.HoaDon h
            LEFT JOIN dbo.ThanhToan tt ON tt.maThanhToan = h.maThanhToan;';
    END
    ELSE IF COL_LENGTH(N'dbo.HoaDon', N'trangThaiThanhToan') IS NOT NULL
    BEGIN
        EXEC sys.sp_executesql N'
            UPDATE dbo.HoaDon
            SET trangThai = CASE
                WHEN trangThaiThanhToan IN (N''Đã thanh toán'', N''Paid'')
                THEN N''Đã thanh toán''
                ELSE N''Chờ thanh toán''
            END;';
    END;

    IF OBJECT_ID(N'dbo.ThanhToan', N'U') IS NOT NULL
    BEGIN
        DECLARE @constraintName SYSNAME;

        WHILE EXISTS (
            SELECT 1
            FROM sys.foreign_keys
            WHERE parent_object_id = OBJECT_ID(N'dbo.HoaDon')
              AND referenced_object_id = OBJECT_ID(N'dbo.ThanhToan')
        )
        BEGIN
            SELECT TOP (1) @constraintName = name
            FROM sys.foreign_keys
            WHERE parent_object_id = OBJECT_ID(N'dbo.HoaDon')
              AND referenced_object_id = OBJECT_ID(N'dbo.ThanhToan');

            EXEC(N'ALTER TABLE dbo.HoaDon DROP CONSTRAINT ' + QUOTENAME(@constraintName));
        END;

        IF COL_LENGTH(N'dbo.HoaDon', N'maThanhToan') IS NOT NULL
            ALTER TABLE dbo.HoaDon DROP COLUMN maThanhToan;

        DROP TABLE dbo.ThanhToan;
    END;

    IF COL_LENGTH(N'dbo.HoaDon', N'trangThaiThanhToan') IS NOT NULL
    BEGIN
        DECLARE @defaultConstraint SYSNAME;

        SELECT @defaultConstraint = dc.name
        FROM sys.default_constraints dc
        JOIN sys.columns c
          ON c.object_id = dc.parent_object_id
         AND c.column_id = dc.parent_column_id
        WHERE dc.parent_object_id = OBJECT_ID(N'dbo.HoaDon')
          AND c.name = N'trangThaiThanhToan';

        IF @defaultConstraint IS NOT NULL
            EXEC(N'ALTER TABLE dbo.HoaDon DROP CONSTRAINT ' + QUOTENAME(@defaultConstraint));

        ALTER TABLE dbo.HoaDon DROP COLUMN trangThaiThanhToan;
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
