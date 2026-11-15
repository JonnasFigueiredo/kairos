#!/bin/sh
# Para o banco. Os dados ficam no volume.
#
#   ./parar.sh           para
#   ./parar.sh apagar    para e apaga os dados (recria do zero na próxima subida)
#
# Usa o mesmo motor que o subir.sh escolheria. Para forçar:  MOTOR=podman ./parar.sh  (ou docker)
cd "$(dirname "$0")" || exit 1

if [ -z "$MOTOR" ]; then
  if docker info >/dev/null 2>&1; then
    MOTOR=docker
  elif podman info >/dev/null 2>&1; then
    MOTOR=podman
  fi
fi
if [ -z "$MOTOR" ]; then
  echo "Não achei o Docker nem o Podman em execução: não há banco no ar para parar."
  exit 0
fi
echo "Usando $MOTOR."

if [ "$1" = "apagar" ]; then
  $MOTOR compose down -v
else
  $MOTOR compose down
fi
