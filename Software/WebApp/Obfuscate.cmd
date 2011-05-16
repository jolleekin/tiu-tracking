@echo off

rem	Script to obfuscate the JavaScript files.
rem	Source folder: src
rem	Output folder: js
rem
rem	@author	Man Hoang

for %%f in (src\*.js) do java -jar yuicompressor-2.4.2.jar --type js %%f -o js\%%~nxf
