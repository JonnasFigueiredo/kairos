#!/usr/bin/env bash
# PostToolUse: depois de editar um .java, compila o projeto com Java 8.
# Falhou: devolve o erro (exit 2) para o assistente corrigir na mesma hora.
entrada=$(cat)
arquivo=$(printf '%s' "$entrada" | grep -oE '"file_path"[[:space:]]*:[[:space:]]*"[^"]*"' | head -1 | sed -E 's/.*"([^"]*)"$/\1/')

case "$arquivo" in
  *src*.java) ;;
  *) exit 0 ;;
esac

cd "$CLAUDE_PROJECT_DIR" || exit 0
[ -f pom.xml ] || exit 0

# O projeto exige Java 8. Se o JAVA_HOME não for 8, procura um JDK 8 instalado.
if ! "${JAVA_HOME:+$JAVA_HOME/bin/}java" -version 2>&1 | grep -q 'version "1\.8'; then
  achou=
  for jdk in "$HOME/.jdks"/jdk8* "$HOME/.jdks"/*-1.8* \
             "/c/Program Files/Eclipse Adoptium"/jdk-8* "/c/Program Files/Java"/jdk1.8* \
             /usr/lib/jvm/*-8-* /usr/lib/jvm/*1.8* \
             /Library/Java/JavaVirtualMachines/*-8*/Contents/Home; do
    if [ -x "$jdk/bin/javac" ] || [ -x "$jdk/bin/javac.exe" ]; then
      export JAVA_HOME="$jdk"; achou=1; break
    fi
  done
  # Sem JDK 8 na máquina: não bloqueia a edição.
  [ -n "$achou" ] || exit 0
fi

saida=$(./mvnw -q -B compile 2>&1)
if [ $? -ne 0 ]; then
  printf '%s\n' "$saida" | grep -E "ERROR|error:" | head -30 >&2
  exit 2
fi
exit 0
