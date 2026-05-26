@echo off
echo Starting Android Emulator (ExpenseOS_Phone)...
start "" .\.android-sdk\emulator\emulator.exe -avd ExpenseOS_Phone -no-snapshot-save

echo Waiting for emulator to fully boot up (this may take a minute)...
.\.android-sdk\platform-tools\adb.exe wait-for-device

:: Adding a small delay to ensure the system is ready
timeout /t 5 /nobreak > NUL

echo Installing the latest compiled version of ExpenseOS...
.\.android-sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk

echo Starting ExpenseOS app...
.\.android-sdk\platform-tools\adb.exe shell am start -n com.expenseos.app/com.expenseos.app.MainActivity

echo Emulator and App started successfully!
pause
