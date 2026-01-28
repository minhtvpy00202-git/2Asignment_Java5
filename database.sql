-- Assignment Java 5 - SQL Server schema & seed

IF DB_ID(N'ASM_Java5') IS NULL
BEGIN
    CREATE DATABASE ASM_Java5;
END
GO

USE ASM_Java5;
GO

-- Drop tables if needed (safe order)
IF OBJECT_ID(N'dbo.notifications', N'U') IS NOT NULL DROP TABLE dbo.notifications;
IF OBJECT_ID(N'dbo.product_reviews', N'U') IS NOT NULL DROP TABLE dbo.product_reviews;
IF OBJECT_ID(N'dbo.cart_items', N'U') IS NOT NULL DROP TABLE dbo.cart_items;
IF OBJECT_ID(N'dbo.order_details', N'U') IS NOT NULL DROP TABLE dbo.order_details;
IF OBJECT_ID(N'dbo.product_sizes', N'U') IS NOT NULL DROP TABLE dbo.product_sizes;
IF OBJECT_ID(N'dbo.sizes', N'U') IS NOT NULL DROP TABLE dbo.sizes;
IF OBJECT_ID(N'dbo.orders', N'U') IS NOT NULL DROP TABLE dbo.orders;
IF OBJECT_ID(N'dbo.authorities', N'U') IS NOT NULL DROP TABLE dbo.authorities;
IF OBJECT_ID(N'dbo.roles', N'U') IS NOT NULL DROP TABLE dbo.roles;
IF OBJECT_ID(N'dbo.products', N'U') IS NOT NULL DROP TABLE dbo.products;
IF OBJECT_ID(N'dbo.categories', N'U') IS NOT NULL DROP TABLE dbo.categories;
IF OBJECT_ID(N'dbo.accounts', N'U') IS NOT NULL DROP TABLE dbo.accounts;
GO

-- Tables
CREATE TABLE dbo.categories (
    id VARCHAR(20) NOT NULL PRIMARY KEY,
    name NVARCHAR(100) NOT NULL
);

CREATE TABLE dbo.products (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name NVARCHAR(200) NOT NULL,
    image VARCHAR(255) NULL,
    price DECIMAL(12,2) NOT NULL,
    discount DECIMAL(5,2) NULL,
    available BIT NOT NULL CONSTRAINT DF_products_available DEFAULT (1),
    quantity INT NULL,
    description NVARCHAR(2000) NULL,
    create_date DATETIME2 NOT NULL CONSTRAINT DF_products_create_date DEFAULT (SYSDATETIME()),
    category_id VARCHAR(20) NULL,
    CONSTRAINT FK_products_categories
        FOREIGN KEY (category_id) REFERENCES dbo.categories(id)
);

CREATE TABLE dbo.sizes (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name NVARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE dbo.product_sizes (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    product_id INT NOT NULL,
    size_id INT NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT FK_product_sizes_products
        FOREIGN KEY (product_id) REFERENCES dbo.products(id),
    CONSTRAINT FK_product_sizes_sizes
        FOREIGN KEY (size_id) REFERENCES dbo.sizes(id),
    CONSTRAINT UQ_product_sizes UNIQUE (product_id, size_id)
);

CREATE TABLE dbo.accounts (
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    fullname NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    photo VARCHAR(255) NULL,
    activated BIT NOT NULL CONSTRAINT DF_accounts_activated DEFAULT (1)
);

CREATE TABLE dbo.cart_items (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    product_id INT NOT NULL,
    size_id INT NOT NULL,
    quantity INT NOT NULL CONSTRAINT DF_cart_items_quantity DEFAULT (1),
    created_at DATETIME2 NOT NULL CONSTRAINT DF_cart_items_created_at DEFAULT (SYSDATETIME()),
    CONSTRAINT FK_cart_items_accounts
        FOREIGN KEY (username) REFERENCES dbo.accounts(username),
    CONSTRAINT FK_cart_items_products
        FOREIGN KEY (product_id) REFERENCES dbo.products(id),
    CONSTRAINT FK_cart_items_sizes
        FOREIGN KEY (size_id) REFERENCES dbo.sizes(id),
    CONSTRAINT UQ_cart_items UNIQUE (username, product_id, size_id)
);

CREATE TABLE dbo.roles (
    id VARCHAR(20) NOT NULL PRIMARY KEY,
    name NVARCHAR(50) NOT NULL
);

CREATE TABLE dbo.authorities (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NULL,
    role_id VARCHAR(20) NULL,
    CONSTRAINT FK_authorities_accounts
        FOREIGN KEY (username) REFERENCES dbo.accounts(username),
    CONSTRAINT FK_authorities_roles
        FOREIGN KEY (role_id) REFERENCES dbo.roles(id)
);

CREATE TABLE dbo.orders (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    address NVARCHAR(255) NOT NULL,
    status VARCHAR(20) NULL,
    create_date DATETIME2 NOT NULL CONSTRAINT DF_orders_create_date DEFAULT (SYSDATETIME()),
    username VARCHAR(50) NULL,
    CONSTRAINT FK_orders_accounts
        FOREIGN KEY (username) REFERENCES dbo.accounts(username)
);

CREATE TABLE dbo.notifications (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    order_id BIGINT NULL,
    title NVARCHAR(200) NOT NULL,
    content NVARCHAR(1000) NOT NULL,
    is_read BIT NOT NULL CONSTRAINT DF_notifications_is_read DEFAULT (0),
    created_at DATETIME2 NOT NULL CONSTRAINT DF_notifications_created_at DEFAULT (SYSDATETIME()),
    CONSTRAINT FK_notifications_accounts
        FOREIGN KEY (username) REFERENCES dbo.accounts(username),
    CONSTRAINT FK_notifications_orders
        FOREIGN KEY (order_id) REFERENCES dbo.orders(id)
);

CREATE TABLE dbo.order_details (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    price DECIMAL(12,2) NOT NULL,
    quantity INT NOT NULL,
    size_id INT NULL,
    size_name NVARCHAR(10) NULL,
    order_id BIGINT NULL,
    product_id INT NULL,
    CONSTRAINT FK_order_details_orders
        FOREIGN KEY (order_id) REFERENCES dbo.orders(id),
    CONSTRAINT FK_order_details_products
        FOREIGN KEY (product_id) REFERENCES dbo.products(id)
);

CREATE TABLE dbo.product_reviews (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    product_id INT NOT NULL,
    order_id BIGINT NOT NULL,
    star_rating INT NOT NULL,
    review_content NVARCHAR(2000) NULL,
    images NVARCHAR(2000) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_product_reviews_created_at DEFAULT (SYSDATETIME()),
    CONSTRAINT FK_product_reviews_accounts
        FOREIGN KEY (username) REFERENCES dbo.accounts(username),
    CONSTRAINT FK_product_reviews_products
        FOREIGN KEY (product_id) REFERENCES dbo.products(id),
    CONSTRAINT FK_product_reviews_orders
        FOREIGN KEY (order_id) REFERENCES dbo.orders(id),
    CONSTRAINT UQ_product_reviews UNIQUE (username, product_id, order_id)
);
GO

-- Seed roles
INSERT INTO dbo.roles (id, name) VALUES
('ADMIN', N'Quản trị'),
('USER', N'Khách hàng');

-- Seed accounts (passwords are plain text for demo only)
INSERT INTO dbo.accounts (username, password, fullname, email, photo, activated) VALUES
('admin', 'admin123', N'Nguyễn Quản Trị', 'admin@shop.local', 'admin.png', 1),
('user01', '123456', N'Trần Minh Anh', 'user01@shop.local', 'u01.png', 1),
('user02', '123456', N'Lê Thu Hà', 'user02@shop.local', 'u02.png', 1),
('user03', '123456', N'Phạm Quốc Bảo', 'user03@shop.local', 'u03.png', 1),
('user04', '123456', N'Ngô Tuấn Kiệt', 'user04@shop.local', 'u04.png', 1),
('user05', '123456', N'Đỗ Khánh Ly', 'user05@shop.local', 'u05.png', 1);

-- Seed authorities (role mapping)
INSERT INTO dbo.authorities (username, role_id) VALUES
('admin', 'ADMIN'),
('admin', 'USER'),
('user01', 'USER'),
('user02', 'USER'),
('user03', 'USER'),
('user04', 'USER'),
('user05', 'USER');

-- Seed categories (10)
INSERT INTO dbo.categories (id, name) VALUES
('CAT01', N'Áo thun'),
('CAT02', N'Áo sơ mi'),
('CAT03', N'Áo khoác'),
('CAT04', N'Quần jeans'),
('CAT05', N'Quần short'),
('CAT06', N'Váy/Đầm'),
('CAT07', N'Giày sneaker'),
('CAT08', N'Giày da'),
('CAT09', N'Túi xách'),
('CAT10', N'Phụ kiện');

-- Seed products (200)
DECLARE @i INT = 1;

WHILE @i <= 200
BEGIN
    DECLARE @cat VARCHAR(20);
    DECLARE @catIndex INT = ((@i - 1) % 10) + 1;

    SET @cat = RIGHT('CAT0' + CAST(@catIndex AS VARCHAR), 4);

    INSERT INTO dbo.products (
        name, image, price, discount, available, quantity, description, create_date, category_id
    )
    VALUES (
        N'Sản phẩm ' + CAST(@i AS NVARCHAR),
        'p' + RIGHT('000' + CAST(@i AS VARCHAR), 3) + '.jpg',
        CAST(99000 + (@i * 350) AS DECIMAL(12,2)),
        CASE WHEN @i % 7 = 0 THEN CAST(10 AS DECIMAL(5,2)) ELSE NULL END,
        1,
        100,
        N'Mô tả sản phẩm ' + CAST(@i AS NVARCHAR),
        GETDATE(),
        @cat
    );

    SET @i = @i + 1;
END
GO

-- Seed sizes
INSERT INTO dbo.sizes (name) VALUES
(N'S'), (N'M'), (N'L'), (N'XL'), (N'2XL'), (N'3XL');

-- Seed product_sizes (all products with 3-4 sizes)
DECLARE @pid INT = 1;
WHILE @pid <= 200
BEGIN
    INSERT INTO dbo.product_sizes (product_id, size_id, quantity)
    SELECT @pid, s.id,
           5 + (@pid % 20)
    FROM dbo.sizes s
    WHERE s.name IN (N'S', N'M', N'L', N'XL');

    IF @pid % 3 = 0
    BEGIN
        INSERT INTO dbo.product_sizes (product_id, size_id, quantity)
        SELECT @pid, s.id, 5 + (@pid % 20)
        FROM dbo.sizes s
        WHERE s.name = N'2XL';
    END
    IF @pid % 5 = 0
    BEGIN
        INSERT INTO dbo.product_sizes (product_id, size_id, quantity)
        SELECT @pid, s.id, 5 + (@pid % 20)
        FROM dbo.sizes s
        WHERE s.name = N'3XL';
    END

    SET @pid = @pid + 1;
END
GO


SELECT 
    fk.name              AS fk_name,
    OBJECT_SCHEMA_NAME(fk.referenced_object_id) AS ref_schema,
    OBJECT_NAME(fk.referenced_object_id)        AS ref_table,
    fk.referenced_object_id
FROM sys.foreign_keys fk
WHERE fk.name = 'FK_products_categories';








