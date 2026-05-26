@echo off
echo Stopping ExpenseOS app...
.\.android-sdk\platform-tools\adb.exe shell am force-stop com.expenseos.app

echo Shutting down Android Emulator...
.\.android-sdk\platform-tools\adb.exe emu kill

echo Emulator and App stopped successfully!
pause
