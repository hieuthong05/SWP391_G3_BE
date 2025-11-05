-- ================================================
-- INSERT VinFast Electric Car Models into [model]
-- ================================================

INSERT INTO [model] ([model_name], [image_url])
VALUES
    (N'VF3', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1761475245/VF3_hhgnvh.jpg'),
    (N'VF5', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1761475281/VF5_migpq4.png'),
    (N'VF6', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1761446036/models/wwdifhtvnfdudghiyfz5.jpg'),
    (N'VF7', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1761473944/models/xjsyjxexmzv7t256ltb6.jpg'),
    (N'VF8', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1761474055/models/quv2cru9u4hxskyzsgw9.png'),
    (N'VF9', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1761475332/VF9_bhqoaa.webp'),
    (N'VFe34', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1761475355/VF_e34_z36oui.jpg');

-- ================================================
-- INSERT VinFast Service Centers into [service_center]
-- ================================================

ALTER TABLE service_center
ALTER COLUMN name NVARCHAR(255);

ALTER TABLE service_center
ALTER COLUMN address NVARCHAR(500);

ALTER TABLE service_center
ALTER COLUMN location NVARCHAR(100);

INSERT INTO [service_center]
    ([name], [address], [location], [phone], [email], [open_time], [close_time], [status], [image])
VALUES
    (N'VinFast Phan Trọng Tuệ', 
     N'Km 2+500, đường Phan Trọng Tuệ, Xã Thanh Liệt, Huyện Thanh Trì, TP. Hà Nội', 
     N'Hà Nội', 
     '02435551111', 
     'vinfast_phantrongtue_hanoi@gmail.com',
     '08:00', '20:00', 
     'active', 
     NULL),

    (N'VinFast Long Biên', 
     N'Tầng 1, TTTM Vincom Plaza, KĐT Vinhomes Riverside, Phường Phúc Lợi, Quận Long Biên, TP. Hà Nội', 
     N'Hà Nội', 
     '02436662222', 
     'vinfast_longbien_hanoi@gmail.com',
     '08:00', '20:00', 
     'active', 
     NULL),

    (N'VinFast Ocean Park', 
     N'TTTM Vincom Ocean Park, KĐT Vinhomes Ocean Park, Cổng số 9 – Khu Kiêu Kỵ, Gia Lâm, Hà Nội', 
     N'Hà Nội', 
     '02437773333', 
     'vinfast_oceanpark_hanoi@gmail.com',
     '08:00', '20:00', 
     'active', 
     NULL),

    (N'VinFast Smart City', 
     N'TTTM Vincom Smart City, KĐT Vinhomes Smart City, Phường Tây Mỗ, Quận Nam Từ Liêm, Hà Nội', 
     N'Hà Nội', 
     '02438884444', 
     'vinfast_smartcity_hanoi@gmail.com',
     '08:00', '20:00', 
     'active', 
     NULL),

    (N'VinFast Đồng Khởi', 
     N'Tầng 1, TTTM Vincom Center Đồng Khởi, 72 Lê Thánh Tôn, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', 
     N'TP. Hồ Chí Minh', 
     '02835551111', 
     'vinfast_dongkhoi_hcm@gmail.com',
     '08:00', '20:00', 
     'active', 
     NULL),

    (N'VinFast Landmark 81', 
     N'Tầng L1, Vincom Center Landmark 81, 208 Nguyễn Hữu Cảnh, Quận Bình Thạnh, TP. Hồ Chí Minh', 
     N'TP. Hồ Chí Minh', 
     '02836662222', 
     'vinfast_landmark81_hcm@gmail.com',
     '08:00', '20:00', 
     'active', 
     NULL),

    (N'VinFast Phú Mỹ Hưng', 
     N'54 Nguyễn Thị Thập, Phường Bình Thuận, Quận 7, TP. Hồ Chí Minh', 
     N'TP. Hồ Chí Minh', 
     '02837773333', 
     'vinfast_phumyhung_hcm@gmail.com',
     '08:00', '20:00', 
     'active', 
     NULL),

    (N'VinFast Cần Thơ', 
     N'Tầng L1, TTTM Vincom Plaza Xuân Khánh, Số 209 Đường 30 Tháng 4, Phường Xuân Khánh, Quận Ninh Kiều, TP. Cần Thơ', 
     N'Cần Thơ', 
     '02923881111', 
     'vinfast_cantho@gmail.com',
     '08:00', '20:00', 
     'active', 
     NULL),

    (N'VinFast Đà Nẵng', 
     N'Tầng 1, TTTM Vincom Plaza Đà Nẵng, Số 910A Ngô Quyền, P. An Hải Bắc, Quận Sơn Trà, TP. Đà Nẵng', 
     N'Đà Nẵng', 
     '02363882222', 
     'vinfast_danang@gmail.com',
     '08:00', '20:00', 
     'active', 
     NULL),

    (N'VinFast Nam Bình Cà Mau', 
     N'Số 139 Nguyễn Tất Thành, Phường 8, TP. Cà Mau', 
     N'Cà Mau', 
     '02903883333', 
     'vinfast_camau@gmail.com',
     '08:00', '20:00', 
     'active', 
     NULL);

	 INSERT INTO [service]
(
    [service_name],
    [description],
    [service_type],
    [estimated_time],
    [price],
    [warranty_peroid],
    [service_status],
    [component],
    [date]
)
VALUES
(
    N'Bảo Dưỡng Định Kì',
    N'Kiểm tra toàn bộ hỏng hóc cho xe',
    N'Combo',
    N'02:00:00',  -- estimated_time (ví dụ 2 tiếng)
    0,
    1,            -- warrantyPeriod: 1 năm
    N'active',
    NULL,
    GETDATE()     -- date hiện tại
);


INSERT INTO [checklist] (
    [checklist_name],
    [checklist_type],
    [description],
    [is_active],
    [created_at]
)
VALUES
(N'Hệ thống pin & quản lý pin', N'Điện - Điện tử', N'Kiểm tra tình trạng pin, cell, hệ thống làm mát và hiệu suất pin.', 1, '2025-10-23 19:15:00'),

(N'Hệ thống sạc & chuyển đổi', N'Điện - Điện tử', N'Kiểm tra bộ sạc, cổng sạc, dây sạc, bộ chuyển đổi AC/DC, sạc nhanh.', 1, '2025-10-23 19:15:00'),

(N'Hệ thống động cơ điện & truyền động', N'Cơ khí - Điện', N'Kiểm tra mô-tơ điện, trục truyền động, hộp giảm tốc và hệ thống điều khiển mô-tơ.', 1, '2025-10-23 19:15:00'),

(N'Hệ thống phanh', N'An toàn', N'Kiểm tra má phanh, đĩa phanh, dầu phanh, phanh tái sinh và cảm biến ABS.', 1, '2025-10-23 19:15:00'),

(N'Hệ thống treo & giảm xóc', N'Cơ khí', N'Kiểm tra phuộc, lò xo, cao su chân phuộc, thanh cân bằng và khớp nối.', 1, '2025-10-23 19:15:00'),

(N'Hệ thống lái', N'Cơ khí', N'Kiểm tra trục lái, trợ lực lái điện, rô tuyn, thanh răng và cân chỉnh góc lái.', 1, '2025-10-23 19:15:00'),

(N'Hệ thống khung gầm & thân vỏ', N'Cơ khí', N'Kiểm tra sườn gầm, bu lông liên kết, điểm hàn, chống rỉ và khung chịu lực.', 1, '2025-10-23 19:15:00'),

(N'Hệ thống điện - điện tử nội thất', N'Điện - Điện tử', N'Kiểm tra đèn, đồng hồ, nút điều khiển, camera, cảm biến và hệ thống giải trí.', 1, '2025-10-23 19:15:00'),

(N'Hệ thống điều hòa & làm mát', N'Tiện nghi', N'Kiểm tra gas lạnh, máy nén, lọc gió, quạt gió và ống dẫn khí.', 1, '2025-10-23 19:15:00'),

(N'Hệ thống lốp & mâm xe', N'Cơ khí', N'Kiểm tra áp suất lốp, độ mòn, cân bằng động và tình trạng mâm.', 1, '2025-10-23 19:15:00'),

(N'Hệ thống chiếu sáng & tín hiệu', N'An toàn', N'Kiểm tra đèn pha, đèn hậu, đèn phanh, xi-nhan, cảm biến và camera hỗ trợ.', 1, '2025-10-23 19:15:00'),

(N'Hệ thống an toàn & hỗ trợ lái', N'An toàn', N'Kiểm tra túi khí, dây an toàn, hệ thống cảnh báo, cảm biến va chạm và camera 360.', 1, '2025-10-23 19:15:00'),

(N'Hệ thống phần mềm & kết nối thông minh', N'Điện - Điều khiển', N'Cập nhật phần mềm ECU, kiểm tra kết nối ứng dụng, OTA và giao thức chẩn đoán.', 1, '2025-10-23 19:15:00'),

(N'Hệ thống nội thất & ngoại thất', N'Tiện nghi', N'Kiểm tra ghế, cửa, kính, cần gạt nước, sơn, và vật liệu hoàn thiện.', 1, '2025-10-23 19:15:00');


ALTER TABLE component
ALTER COLUMN name NVARCHAR(MAX);
ALTER TABLE component
ALTER COLUMN supplier_name NVARCHAR(MAX);

-- ================================================
-- INSERT Components (Spare Parts) for VinFast EV Maintenance
-- ================================================

INSERT INTO [component]
([service_centerID], [checklist_ID], [name], [code], [type], [description], 
 [price], [quantity], [min_quantity], [supplier_name], [image], [status], [image_url])
VALUES
-- 1. Hệ thống pin & quản lý pin (checklist_ID = 1)
(1, 1, N'Pin cao áp (Battery Pack)', 'VF-BAT-001', 'battery', N'Bộ pin cao áp chính của xe điện', 2000, 30, 10, N'CATL', NULL, 'active', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1761486133/components/bangbrbwijysvrx9fdmk.png'),
(2, 1, N'Battery Management System (BMS)', 'VF-BMS-002', 'bms', N'Bo mạch điều khiển quản lý pin', 2000, 25, 10, N'LG Energy Solution', NULL, 'active', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1762307328/Battery_Management_System_BMS_n5ljkg.avif'),
(3, 1, N'Contactor cao áp', 'VF-CT-003', 'contactor', N'Relay cao áp điều khiển kết nối pin', 1000, 40, 15, N'Bosch', NULL, 'active', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1762307327/Contactor_cao_%C3%A1p_hkmjvi.jpg'),
(4, 1, N'Bơm làm mát pin', 'VF-COOL-004', 'cooling_pump', N'Bơm tuần hoàn dung dịch làm mát pin', 1000, 35, 12, N'Valeo', NULL, 'active', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1762307327/B%C6%A1m_l%C3%A0m_m%C3%A1t_pin_kfh27m.jpg'),

-- 2. Hệ thống sạc & chuyển đổi (checklist_ID = 2)
(1, 2, N'Cổng sạc Type 2', 'VF-CHP-101', 'charging_port', N'Cổng sạc chuẩn Type 2', 1000, 50, 15, N'Phoenix Contact', NULL, 'active', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1762307821/C%E1%BB%95ng_s%E1%BA%A1c_Type_2_wxtypf.jpg'),
(2, 2, N'Bộ sạc Onboard (OBC)', 'VF-OBC-102', 'charger', N'Bộ sạc gắn trên xe', 2000, 25, 10, N'Delta Electronics', NULL, 'active', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1761498074/components/m0huojl4lm1zzoduu9zd.jpg'),
(3, 2, N'DC-DC Converter', 'VF-DCDC-103', 'converter', N'Bộ chuyển đổi điện cao áp sang 12V', 2000, 20, 10, N'Denso', NULL, 'active', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1762307805/DC-DC_Converter_twxqgw.jpg'),

-- 3. Truyền động điện (checklist_ID = 3)
(4, 3, N'Mô-tơ điện truyền động', 'VF-MOTOR-201', 'motor', N'Mô-tơ kéo chính', 2000, 25, 10, N'Nidec', NULL, 'active', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1762315797/M%C3%B4-t%C6%A1_%C4%91i%E1%BB%87n_truy%E1%BB%81n_%C4%91%E1%BB%99ng_qzjo87.jpg'),
(5, 3, N'Bộ inverter điều khiển mô-tơ', 'VF-INV-202', 'inverter', N'Bộ nghịch lưu điều khiển mô-tơ điện', 2000, 30, 10, N'Hitachi', NULL, 'active', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1762315797/B%E1%BB%99_inverter_%C4%91i%E1%BB%81u_khi%E1%BB%83n_m%C3%B4-t%C6%A1_sjuwmu.jpg'),
(6, 3, N'Hộp giảm tốc 1 cấp', 'VF-GBOX-203', 'gearbox', N'Hộp số đơn giản giảm tốc mô-tơ', 2000, 20, 10, N'ZF Friedrichshafen', NULL, 'active', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1762315797/H%E1%BB%99p_gi%E1%BA%A3m_t%E1%BB%91c_1_c%E1%BA%A5p_nxqoqg.webp'),

-- 4. Hệ thống làm mát & điều hòa điện tử (checklist_ID = 4)
(7, 4, N'Két nước làm mát pin', 'VF-RAD-301', 'radiator', N'Két nước tản nhiệt pin và inverter', 1000, 30, 10, N'Denso', NULL, 'active', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1762316664/K%C3%A9t_n%C6%B0%E1%BB%9Bc_l%C3%A0m_m%C3%A1t_pin_f3vvwq.jpg'),
(8, 4, N'Bơm nước làm mát', 'VF-PUMP-302', 'pump', N'Bơm tuần hoàn nước làm mát', 1000, 35, 10, N'Valeo', NULL, 'active', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1762316665/B%C6%A1m_n%C6%B0%E1%BB%9Bc_l%C3%A0m_m%C3%A1t_yv3vai.jpg'),

-- 5. Hệ thống phanh (checklist_ID = 5)
(9, 5, N'Má phanh trước', 'VF-BPAD-401', 'brake_pad', N'Má phanh trước ma sát cao', 1000, 40, 12, N'Brembo', NULL, 'active', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1762317210/M%C3%A1_phanh_tr%C6%B0%E1%BB%9Bc_ukx5gh.png'),
(10, 5, N'Đĩa phanh sau', 'VF-BDISC-402', 'brake_disc', N'Đĩa phanh bằng thép carbon', 1000, 30, 10, N'Bosch', NULL, 'active', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1762317209/%C4%90%C4%A9a_phanh_sau_p728qx.jpg'),
(1, 5, N'Caliper phanh', 'VF-CAL-403', 'caliper', N'Cụm phanh kẹp bánh xe', 2000, 25, 10, N'TR-W Automotive', NULL, 'active', N'https://res.cloudinary.com/dq5skmidv/image/upload/v1762317210/Caliper_phanh_xyywlp.jpg'),

-- 6. Hệ thống gầm / treo / lái (checklist_ID = 6)
(2, 6, N'Giảm xóc trước', 'VF-SHOCK-501', 'shock_absorber', N'Giảm xóc dầu cho bánh trước', 1000, 35, 10, N'KYB', NULL, 'active'),
(3, 6, N'Lò xo treo sau', 'VF-SPR-502', 'coil_spring', N'Lò xo thép chịu lực cao', 1000, 30, 10, N'Sachs', NULL, 'active'),

-- 7. Lốp & bánh xe (checklist_ID = 10)
(4, 10, N'Lốp 18 inch', 'VF-TIRE-601', 'tire', N'Lốp radial 18 inch tiêu chuẩn', 1000, 40, 12, N'Bridgestone', NULL, 'active'),
(5, 10, N'Cảm biến áp suất lốp', 'VF-TPMS-602', 'sensor', N'Cảm biến áp suất bánh xe', 1000, 25, 10, N'Continental', NULL, 'active'),

-- 8. Hệ thống điện 12V & chiếu sáng (checklist_ID = 8)
(6, 8, N'Ắc quy 12V phụ trợ', 'VF-12V-701', 'battery', N'Ắc quy phụ cấp nguồn cho hệ thống 12V', 1000, 30, 10, N'GS Yuasa', NULL, 'active'),
(7, 8, N'Đèn pha LED', 'VF-LED-702', 'light', N'Đèn pha LED hiệu suất cao', 2000, 25, 10, N'Philips Automotive', NULL, 'active'),

-- 9. Cảm biến & điện tử an toàn (checklist_ID = 12)
(8, 12, N'Cảm biến tốc độ bánh xe', 'VF-WSS-801', 'sensor', N'Cảm biến đo tốc độ quay bánh', 1000, 40, 12, N'Bosch', NULL, 'active'),
(9, 12, N'Camera 360 độ', 'VF-CAM-802', 'camera', N'Hệ thống camera toàn cảnh', 2000, 25, 10, N'Mobis', NULL, 'active'),

-- 10. Hệ thống HVAC (checklist_ID = 9)
(10, 9, N'Máy nén điều hòa điện', 'VF-AC-901', 'compressor', N'Máy nén điện điều hòa cho EV', 2000, 20, 10, N'Denso', NULL, 'active'),
(1, 9, N'Lọc gió cabin', 'VF-FLTR-902', 'filter', N'Lọc không khí điều hòa trong xe', 1000, 30, 10, N'MANN Filter', NULL, 'active'),

-- 11. Nội thất & ngoại thất (checklist_ID = 14)
(2, 14, N'Cần gạt nước', 'VF-WIP-1001', 'wiper', N'Lưỡi gạt nước cao su', 1000, 40, 12, N'Bosch', NULL, 'active'),
(3, 14, N'Động cơ nâng kính', 'VF-WM-1002', 'window_motor', N'Motor nâng/hạ kính cửa', 1000, 25, 10, N'Denso', NULL, 'active'),

-- 12. Tiêu hao & vật tư (checklist_ID = 12)
(4, 12, N'Dầu phanh DOT 4', 'VF-FLUID-1101', 'fluid', N'Dầu phanh thủy lực DOT 4', 1000, 50, 15, N'Castrol', NULL, 'active'),
(5, 12, N'Dung dịch làm mát pin', 'VF-COOL-1102', 'coolant', N'Dung dịch tản nhiệt cho pin', 1000, 45, 15, N'Shell', NULL, 'active'),

-- 13. Dụng cụ bảo dưỡng (checklist_ID = 13)
(6, 13, N'Máy chẩn đoán OBD-II', 'VF-OBD-1201', 'diagnostic_tool', N'Thiết bị đọc lỗi xe điện', 2000, 20, 10, N'Autel', NULL, 'active'),
(7, 13, N'Găng tay cách điện cao áp', 'VF-GLOVE-1202', 'safety_gear', N'Găng bảo hộ cho kỹ thuật viên EV', 1000, 30, 10, N'Honeywell', NULL, 'active'),

-- 14. Cập nhật phần mềm (checklist_ID = 13)
(8, 13, N'Module OTA', 'VF-OTA-1301', 'communication', N'Module truyền dữ liệu cập nhật OTA', 2000, 25, 10, N'Quectel', NULL, 'active'),
(9, 13, N'ECU Firmware Update Kit', 'VF-ECU-1302', 'ecu_update', N'Bộ dụng cụ cập nhật ECU', 2000, 20, 10, N'Bosch', NULL, 'active');

