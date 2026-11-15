#!/bin/sh
# Sobe o banco (Docker ou Podman) e a aplicação.
#
#   ./subir.sh          banco + aplicação em http://localhost:8080/kairos
#   ./subir.sh banco    só o banco
#
# Antes, se precisar:
#   MOTOR=podman              força o motor (docker ou podman)
#   PORTA=8082                troca a porta da aplicação
#   JAVA_HOME=/caminho/jdk8   JDK 8 (o Jetty da prova não roda em Java mais novo)
cd "$(dirname "$0")" || exit 1

# Docker no ar tem preferência; senão, Podman (iniciando a máquina virtual no Windows e no Mac)
if [ -z "$MOTOR" ]; then
  if docker info >/dev/null 2>&1; then
    MOTOR=docker
  elif command -v podman >/dev/null 2>&1; then
    if ! podman info >/dev/null 2>&1; then
      echo "Iniciando a máquina do Podman..."
      podman machine start >/dev/null 2>&1
    fi
    podman info >/dev/null 2>&1 && MOTOR=podman
  fi
fi
if [ -z "$MOTOR" ]; then
  echo "Não achei o Docker nem o Podman em execução. Abra o Docker ou o Podman e tente de novo."
  exit 1
fi
echo "Usando $MOTOR."

if ! $MOTOR compose version >/dev/null 2>&1; then
  echo "O $MOTOR não tem o compose instalado."
  echo "No Podman: instale o podman-compose ou o docker-compose (no Podman Desktop: Settings, Resources, Compose)."
  exit 1
fi

if ! $MOTOR compose up -d; then
  [ "$MOTOR" = podman ] && outro=docker || outro=podman
  echo
  echo "Não foi possível subir o banco."
  echo "Se o erro fala da porta 3306, outro banco está usando a porta. Se for o Kairos no outro motor:"
  echo "  MOTOR=$outro ./parar.sh"
  exit 1
fi

echo "Esperando o banco ficar pronto (na primeira vez leva alguns minutos)..."
tentativas=0
until $MOTOR compose exec -T db sh -c 'mysqladmin ping -h127.0.0.1 -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" --silent' >/dev/null 2>&1; do
  tentativas=$((tentativas + 1))
  if [ "$tentativas" -ge 120 ]; then
    echo "O banco não respondeu em 10 minutos. Veja o motivo com: $MOTOR compose logs db"
    exit 1
  fi
  sleep 5
done
echo "Banco pronto."

[ "$1" = "banco" ] && exit 0

if [ -n "$JAVA_HOME" ]; then JAVA="$JAVA_HOME/bin/java"; else JAVA=java; fi
if ! "$JAVA" -version 2>&1 | grep -q '"1\.8\.'; then
  echo "A aplicação precisa do JDK 8, e o Java encontrado é outro:"
  "$JAVA" -version 2>&1 | head -1
  echo "Aponte o JAVA_HOME para um JDK 8 e rode de novo, por exemplo:"
  echo "  JAVA_HOME=/caminho/do/jdk8 ./subir.sh"
  exit 1
fi

PORTA="${PORTA:-8080}"
echo "Subindo a aplicação em http://localhost:$PORTA/kairos  (Ctrl+C para parar)"
./mvnw jetty:run -Dkairos.porta="$PORTA"
