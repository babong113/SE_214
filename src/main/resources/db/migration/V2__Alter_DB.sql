--thêm trường link ảnh vào
ALTER TABLE courts
    ADD COLUMN image_url VARCHAR(500);

--Thêm trường otp vào
ALTER TABLE users
    ADD COLUMN reset_otp VARCHAR(6);