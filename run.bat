@echo off
rem -------------------------------------------------
rem  DatLichKhamBenh – Khởi chạy với MySQL Workbench
rem -------------------------------------------------

rem Kill process dang dung port 8080 (neu co)
echo [INFO] Kiem tra va giai phong port 8080...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080 " ^| findstr "LISTENING" 2^>nul') do (
    echo [INFO] Kill PID %%a dang chiem port 8080
    taskkill /PID %%a /F >nul 2>&1
)

rem Chuyen toi thu muc backend
cd /d "%~dp0backend"

rem Profile dev bat du lieu mau; production khong tu tao tai khoan mac dinh.
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev
