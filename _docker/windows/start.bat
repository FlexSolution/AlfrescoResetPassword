@ECHO OFF
chcp 65001
set BASEDIR=%~dp0

call %BASEDIR%docker-compose-Windows-x86_64.exe -f %BASEDIR%..\docker-compose.yml stop
call mvn -f %BASEDIR%..\..\pom.xml clean package
del %BASEDIR%..\logs.txt
call %BASEDIR%docker-compose-Windows-x86_64.exe -f %BASEDIR%..\docker-compose.yml up  --no-recreate --no-start
start /B %BASEDIR%docker-compose-Windows-x86_64.exe -f %BASEDIR%..\docker-compose.yml start
PING localhost -n 20 >NUL
start /B %BASEDIR%docker-compose-Windows-x86_64.exe -f %BASEDIR%..\docker-compose.yml logs -f --tail=0 > %BASEDIR%..\logs.txt
