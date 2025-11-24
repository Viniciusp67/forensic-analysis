#!/bin/bash

# ============================================================================
# Script de Build - Trabalho Final AED 2025.2
# Cria Fat JAR pronto para submissão ao validador automático
# ============================================================================

MATRICULA1="1010612"

CLASSE_COMPLETA="br.edu.icev.aed.forense.SolucaoForenseImpl"
JAR_NAME="${MATRICULA1}.jar"

# ============================================================================
# VERIFICAÇÕES INICIAIS
# ============================================================================
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║          Build Script - Análise Forense AED 2025.2             ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Verificar se API existe (aceita dois nomes possíveis)
API_JAR=""
if [ -f "lib/analise-forense-api.jar" ]; then
    API_JAR="lib/analise-forense-api.jar"
elif [ -f "lib/analise-forense-aed.jar" ]; then
    API_JAR="lib/analise-forense-aed.jar"
else
    echo "❌ ERRO: Nenhum JAR de API encontrado!"
    echo ""
    echo "📥 Esperado um dos arquivos:"
    echo "   - lib/analise-forense-api.jar"
    echo "   - lib/analise-forense-aed.jar"
    echo ""
    echo "   Baixe do repositório do professor:"
    echo "   https://github.com/dimmykarson/trabalho_final_AED_2025.2"
    exit 1
fi

# Verificar Java
if ! command -v javac &> /dev/null; then
    echo "❌ ERRO: javac não encontrado!"
    echo "   Instale o JDK 11 ou superior e adicione ao PATH"
    exit 1
fi

echo "✅ Dependências verificadas"
echo "   Usando API: ${API_JAR}"
echo ""

# ============================================================================
# PASSO 1: LIMPEZA
# ============================================================================
echo "🧹 Passo 1/6: Limpando diretório build..."
rm -rf build
rm -f "${JAR_NAME}"
rm -f README.txt
mkdir -p build
echo "   ✅ Diretório limpo"
echo ""

# ============================================================================
# PASSO 2: COMPILAÇÃO
# ============================================================================
echo "🔨 Passo 2/6: Compilando código fonte..."

# Descobrir onde estão os .java
if [ -d "src/main/java" ]; then
    # Estrutura estilo Maven
    JAVA_FILES=$(find src/main/java -name "*.java")
else
    # Estrutura "solta" (como a sua, com .java na raiz)
    # Ignora build, target e lib
    JAVA_FILES=$(find . -name "*.java" \
        ! -path "./build/*" \
        ! -path "./target/*" \
        ! -path "./lib/*")
fi

if [ -z "$JAVA_FILES" ]; then
    echo "❌ ERRO: Nenhum arquivo .java encontrado!"
    echo "   Verifique se os .java estão na raiz ou em src/main/java"
    exit 1
fi

# Compilar
javac -d build \
      -cp "${API_JAR}" \
      -encoding UTF-8 \
      ${JAVA_FILES}

if [ $? -ne 0 ]; then
    echo "❌ ERRO na compilação!"
    echo "   Verifique os erros acima e corrija"
    exit 1
fi

echo "   ✅ Código compilado com sucesso"
echo ""

# ============================================================================
# PASSO 3: COPIAR RECURSOS
# ============================================================================
echo "📋 Passo 3/6: Copiando recursos..."

if [ -d "src/main/resources" ]; then
    cp -r src/main/resources/* build/ 2>/dev/null || true
    echo "   ✅ Recursos copiados"
else
    echo "   ⏭️  Sem recursos para copiar"
fi
echo ""

# ============================================================================
# PASSO 4: EXTRAIR DEPENDÊNCIAS
# ============================================================================
echo "📚 Passo 4/6: Extraindo dependências da API..."

cd build

# Extrair a API
jar xf "../${API_JAR}" 2>/dev/null

# Remover arquivos desnecessários
rm -rf META-INF/maven 2>/dev/null || true
rm -f META-INF/MANIFEST.MF 2>/dev/null || true

cd ..

echo "   ✅ Dependências extraídas"
echo ""

# ============================================================================
# PASSO 5: CRIAR FAT JAR
# ============================================================================
echo "🎁 Passo 5/6: Criando Fat JAR..."

# Criar manifest simples
mkdir -p build/META-INF
echo "Manifest-Version: 1.0" > build/META-INF/MANIFEST.MF
echo "Created-By: Trabalho AED 2025.2" >> build/META-INF/MANIFEST.MF

# Criar JAR
jar cf "${JAR_NAME}" -C build .

if [ $? -ne 0 ]; then
    echo "❌ ERRO ao criar JAR!"
    exit 1
fi

echo "   ✅ Fat JAR criado: ${JAR_NAME}"
echo ""

# ============================================================================
# PASSO 6: CRIAR README.txt
# ============================================================================
echo "📝 Passo 6/6: Criando README.txt..."

echo "${CLASSE_COMPLETA}" > README.txt

echo "   ✅ README.txt criado"
echo ""

# ============================================================================
# VALIDAÇÃO
# ============================================================================
echo "🔍 Validando saída..."
echo ""

# Verificar se JAR contém a classe principal
jar tf "${JAR_NAME}" | grep "br/edu/icev/aed/forense/SolucaoForenseImpl.class" > /dev/null

if [ $? -eq 0 ]; then
    echo "✅ Classe principal encontrada no JAR"
else
    echo "❌ ERRO: Classe principal não encontrada no JAR!"
    echo "   Procurando por: br/edu/icev/aed/forense/SolucaoForenseImpl.class"
    exit 1
fi

# Verificar tamanho do JAR (du existe no Git Bash)
if command -v du &> /dev/null; then
    JAR_SIZE=$(du -h "${JAR_NAME}" | cut -f1)
    echo "✅ Tamanho do JAR: ${JAR_SIZE}"
else
    echo "ℹ️ Não foi possível determinar o tamanho do JAR (comando du não encontrado)"
fi

# Verificar README.txt
if [ -f "README.txt" ]; then
    README_CONTENT=$(cat README.txt)
    if [ "$README_CONTENT" == "$CLASSE_COMPLETA" ]; then
        echo "✅ README.txt correto"
    else
        echo "⚠️  README.txt com conteúdo diferente do esperado"
        echo "   Esperado: ${CLASSE_COMPLETA}"
        echo "   Encontrado: ${README_CONTENT}"
    fi
else
    echo "❌ README.txt não criado!"
    exit 1
fi

echo ""
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                    BUILD CONCLUÍDO COM SUCESSO                 ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""
echo "📦 ARQUIVOS PARA ENTREGA:"
if [ -n "${JAR_SIZE}" ]; then
    echo "   ✅ ${JAR_NAME} (${JAR_SIZE})"
else
    echo "   ✅ ${JAR_NAME}"
fi
echo "   ✅ README.txt"
echo ""
echo "📝 Conteúdo do README.txt:"
echo "   ${CLASSE_COMPLETA}"
echo ""
echo "🎯 PRÓXIMOS PASSOS:"
echo "   1. Testar o JAR conforme instruções do professor"
echo "   2. Verificar se todos os métodos funcionam"
echo "   3. Submeter o JAR + link do GitHub ao professor"
echo ""
