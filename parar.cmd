@echo off
rem Para o banco. Os dados ficam no volume.
rem
rem   parar.cmd           para
rem   parar.cmd apagar    para e apaga os dados (recria do zero na proxima subida)
rem
rem Usa o mesmo motor que o subir.cmd escolheria. Para forcar:  set MOTOR=podman  (ou docker)
setlocal
cd /d "%~dp0"

if not defined MOTOR docker info >nul 2>&1 && set MOTOR=docker
if not defined MOTOR podman info >nul 2>&1 && set MOTOR=podman
if not defined MOTOR (
  echo Nao achei o Docker nem o Podman em execucao: nao ha banco no ar para parar.
  exit /b 0
)
echo Usando %MOTOR%.

if /i "%~1"=="apagar" (
  %MOTOR% compose down -v
) else (
  %MOTOR% compose down
)
