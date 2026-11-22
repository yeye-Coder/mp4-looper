@echo off
rem Builds ..\MP4Looper.exe with mp4-looper.html embedded inside.
rem Run again after editing mp4-looper.html.
cd /d "%~dp0"
"%WINDIR%\Microsoft.NET\Framework64\v4.0.30319\csc.exe" /nologo /target:winexe /optimize+ ^
  /win32icon:mp4looper.ico ^
  /resource:..\mp4-looper.html,mp4-looper.html ^
  /reference:System.Windows.Forms.dll ^
  /out:..\MP4Looper.exe Launcher.cs
if errorlevel 1 (echo BUILD FAILED & exit /b 1)
echo Built ..\MP4Looper.exe
