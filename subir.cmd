@echo off
rem Sobe o banco (Docker ou Podman) e a aplicacao.
rem
rem   subir.cmd          banco + aplicacao em http://localhost:8080/kairos
rem   subir.cmd banco    so o banco
rem
rem Antes, se precisar:
rem   set MOTOR=podman              forca o motor (docker ou podman)
rem   set PORTA=8082                troca a porta da aplicacao
rem   set JAVA_HOME=C:\...\jdk8     JDK 8 (o Jetty da prova nao roda em Java mais novo)
setlocal
cd /d "%~dp0"

rem Docker no ar tem preferencia; senao, Podman (iniciando a maquina virtual, se estiver parada)
if not defined MOTOR docker info >nul 2>&1 && set MOTOR=docker
if not defined MOTOR where podman >nul 2>&1 && (
  podman info >nul 2>&1 || (
    echo Iniciando a maquina do Podman...
    podman machine start >nul 2>&1
  )
  podman info >nul 2>&1 && set MOTOR=podman
)
if not defined MOTOR (
  echo Nao achei o Docker nem o Podman em execucao. Abra o Docker Desktop ou o Podman Desktop e tente de novo.
  exit /b 1
)
echo Usando %MOTOR%.

%MOTOR% compose version >nul 2>&1
if errorlevel 1 (
  echo O %MOTOR% nao tem o compose instalado.
  echo No Podman Desktop: Settings, Resources, Compose, Setup. Depois rode de novo.
  exit /b 1
)

%MOTOR% compose up -d
if errorlevel 1 (
  echo.
  echo Nao foi possivel subir o banco.
  echo Se o erro fala da porta 3306, outro banco esta usando a porta. Se for o Kairos no outro motor:
  if /i "%MOTOR%"=="podman" (echo   set MOTOR=docker ^& parar.cmd) else (echo   set MOTOR=podman ^& parar.cmd)
  exit /b 1
)

echo Esperando o banco ficar pronto (na primeira vez leva alguns minutos)...
set /a TENTATIVAS=0
:espera
%MOTOR% compose exec -T db sh -c "mysqladmin ping -h127.0.0.1 -u$MYSQL_USER -p$MYSQL_PASSWORD --silent" >nul 2>&1
if not errorlevel 1 goto pronto
set /a TENTATIVAS+=1
if %TENTATIVAS% geq 120 (
  echo O banco nao respondeu em 10 minutos. Veja o motivo com: %MOTOR% compose logs db
  exit /b 1
)
rem ping espera 5 segundos e funciona em qualquer janela (o "timeout" falha fora de um console)
ping -n 6 127.0.0.1 >nul
goto espera

:pronto
echo Banco pronto.
if /i "%~1"=="banco" exit /b 0

if defined JAVA_HOME (set "JAVA=%JAVA_HOME%\bin\java") else (set "JAVA=java")
rem Java 8 se apresenta como version "1.8.0_xxx" (o ponto da expressao casa com as aspas)
"%JAVA%" -version 2>&1 | findstr /r /c:"version .1\.8\." >nul
if errorlevel 1 (
  echo A aplicacao precisa do JDK 8, e o Java encontrado e outro:
  "%JAVA%" -version 2>&1 | findstr /i "version"
  echo Aponte o JAVA_HOME para um JDK 8 e rode de novo, por exemplo:
  echo   set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_xxx
  exit /b 1
)

if not defined PORTA set PORTA=8080
echo Subindo a aplicacao em http://localhost:%PORTA%/kairos  (Ctrl+C para parar)
rem Caminho completo: alguns Windows nao procuram programas na pasta atual
call "%~dp0mvnw.cmd" jetty:run -Dkairos.porta=%PORTA%
