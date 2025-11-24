package br.edu.icev.aed.forense.test;

import br.edu.icev.aed.forense.SolucaoForenseImpl;
import br.edu.icev.aed.forense.extended.SessoesInvalidasExtended;
import br.edu.icev.aed.forense.model.ResultadoSessoesInvalidas;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Bateria de testes específicos para o Desafio 1: Encontrar Sessões Inválidas.
 * 
 * Esta classe testa todos os cenários possíveis:
 * - LOGIN aninhado
 * - LOGOUT órfão
 * - LOGOUT incorreto
 * - Sessões abertas
 * - Múltiplos usuários
 * - Casos extremos
 */
public class TesteDesafio1 {

    private static final SolucaoForenseImpl solucao = new SolucaoForenseImpl();
    private static int testesPassados = 0;
    private static int testesFalhados = 0;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     BATERIA DE TESTES - DESAFIO 1: SESSÕES INVÁLIDAS      ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        try {
            // Testes básicos
            testarSessaoNormal();
            testarLoginAninhado();
            testarLogoutOrfao();
            testarSessaoAberta();
            testarLogoutIncorreto();
            
            // Testes com múltiplos usuários
            testarMultiplosUsuarios();
            testarUsuariosIndependentes();
            
            // Casos extremos
            testarArquivoVazio();
            testarApenasLogins();
            testarApenasLogouts();
            testarMesmoSessionIdUsuariosDiferentes();
            
            // Testes complexos
            testarCenarioComplexo();
            
            // Teste com versão estendida
            testarVersaoEstendida();
            
            // Resumo
            imprimirResumo();

        } catch (Exception e) {
            System.err.println("ERRO FATAL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== TESTES BÁSICOS =====

    private static void testarSessaoNormal() throws IOException {
        System.out.println("🧪 Teste 1: Sessão Normal (LOGIN + LOGOUT correto)");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-1,LOGIN,/usr/bin/sshd,5,0\n" +
            "2000,alice,session-1,LOGOUT,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste1.csv", conteudo);
        Set<String> resultado = solucao.desafio1_encontrarSessoesInvalidas(arquivo);
        
        verificar("Sessão normal não deve ter inválidas", 
                 resultado.isEmpty(), resultado);
    }

    private static void testarLoginAninhado() throws IOException {
        System.out.println("🧪 Teste 2: LOGIN Aninhado");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-1,LOGIN,/usr/bin/sshd,5,0\n" +
            "2000,alice,session-2,LOGIN,/usr/bin/sshd,5,0\n" +
            "3000,alice,session-2,LOGOUT,/usr/bin/sshd,5,0\n" +
            "4000,alice,session-1,LOGOUT,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste2.csv", conteudo);
        Set<String> resultado = solucao.desafio1_encontrarSessoesInvalidas(arquivo);
        
        verificar("Deve detectar session-2 como inválida", 
                 resultado.size() == 1 && resultado.contains("session-2"), resultado);
    }

    private static void testarLogoutOrfao() throws IOException {
        System.out.println("🧪 Teste 3: LOGOUT Órfão (sem LOGIN)");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-1,LOGOUT,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste3.csv", conteudo);
        Set<String> resultado = solucao.desafio1_encontrarSessoesInvalidas(arquivo);
        
        verificar("Deve detectar LOGOUT órfão", 
                 resultado.size() == 1 && resultado.contains("session-1"), resultado);
    }

    private static void testarSessaoAberta() throws IOException {
        System.out.println("🧪 Teste 4: Sessão Aberta (LOGIN sem LOGOUT)");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-1,LOGIN,/usr/bin/sshd,5,0\n" +
            "2000,alice,session-1,FILE_ACCESS,/var/log/auth.log,5,0\n";
        
        String arquivo = criarArquivoTeste("teste4.csv", conteudo);
        Set<String> resultado = solucao.desafio1_encontrarSessoesInvalidas(arquivo);
        
        verificar("Deve detectar sessão sem LOGOUT", 
                 resultado.size() == 1 && resultado.contains("session-1"), resultado);
    }

    private static void testarLogoutIncorreto() throws IOException {
        System.out.println("🧪 Teste 5: LOGOUT Incorreto (não corresponde ao topo)");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-1,LOGIN,/usr/bin/sshd,5,0\n" +
            "2000,alice,session-2,LOGOUT,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste5.csv", conteudo);
        Set<String> resultado = solucao.desafio1_encontrarSessoesInvalidas(arquivo);
        
        verificar("Deve detectar LOGOUT incorreto e sessão aberta", 
                 resultado.size() == 2 && resultado.contains("session-1") && 
                 resultado.contains("session-2"), resultado);
    }

    // ===== TESTES COM MÚLTIPLOS USUÁRIOS =====

    private static void testarMultiplosUsuarios() throws IOException {
        System.out.println("🧪 Teste 6: Múltiplos Usuários");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-A1,LOGIN,/usr/bin/sshd,5,0\n" +
            "2000,bob,session-B1,LOGIN,/usr/bin/sshd,5,0\n" +
            "3000,alice,session-A1,LOGOUT,/usr/bin/sshd,5,0\n" +
            "4000,bob,session-B2,LOGIN,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste6.csv", conteudo);
        Set<String> resultado = solucao.desafio1_encontrarSessoesInvalidas(arquivo);
        
        verificar("Bob tem LOGIN aninhado e sessões abertas", 
                 resultado.size() == 2 && resultado.contains("session-B1") && 
                 resultado.contains("session-B2"), resultado);
    }

    private static void testarUsuariosIndependentes() throws IOException {
        System.out.println("🧪 Teste 7: Usuários com Mesmo Session ID (independentes)");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-X,LOGIN,/usr/bin/sshd,5,0\n" +
            "2000,bob,session-X,LOGIN,/usr/bin/sshd,5,0\n" +
            "3000,alice,session-X,LOGOUT,/usr/bin/sshd,5,0\n" +
            "4000,bob,session-X,LOGOUT,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste7.csv", conteudo);
        Set<String> resultado = solucao.desafio1_encontrarSessoesInvalidas(arquivo);
        
        verificar("Sessões com mesmo ID mas usuários diferentes são independentes", 
                 resultado.isEmpty(), resultado);
    }

    // ===== CASOS EXTREMOS =====

    private static void testarArquivoVazio() throws IOException {
        System.out.println("🧪 Teste 8: Arquivo Vazio");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n";
        
        String arquivo = criarArquivoTeste("teste8.csv", conteudo);
        Set<String> resultado = solucao.desafio1_encontrarSessoesInvalidas(arquivo);
        
        verificar("Arquivo vazio deve retornar Set vazio", 
                 resultado.isEmpty(), resultado);
    }

    private static void testarApenasLogins() throws IOException {
        System.out.println("🧪 Teste 9: Apenas LOGINs (nenhum LOGOUT)");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-1,LOGIN,/usr/bin/sshd,5,0\n" +
            "2000,alice,session-2,LOGIN,/usr/bin/sshd,5,0\n" +
            "3000,bob,session-3,LOGIN,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste9.csv", conteudo);
        Set<String> resultado = solucao.desafio1_encontrarSessoesInvalidas(arquivo);
        
        verificar("Todas as 3 sessões devem ser inválidas", 
                 resultado.size() == 3, resultado);
    }

    private static void testarApenasLogouts() throws IOException {
        System.out.println("🧪 Teste 10: Apenas LOGOUTs (nenhum LOGIN)");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-1,LOGOUT,/usr/bin/sshd,5,0\n" +
            "2000,bob,session-2,LOGOUT,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste10.csv", conteudo);
        Set<String> resultado = solucao.desafio1_encontrarSessoesInvalidas(arquivo);
        
        verificar("Todas as 2 sessões devem ser inválidas (órfãs)", 
                 resultado.size() == 2, resultado);
    }

    private static void testarMesmoSessionIdUsuariosDiferentes() throws IOException {
        System.out.println("🧪 Teste 11: Mesmo Session ID, Usuários Diferentes");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-1,LOGIN,/usr/bin/sshd,5,0\n" +
            "2000,bob,session-1,LOGIN,/usr/bin/sshd,5,0\n" +
            "3000,alice,session-1,LOGOUT,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste11.csv", conteudo);
        Set<String> resultado = solucao.desafio1_encontrarSessoesInvalidas(arquivo);
        
        // Bob não fez LOGOUT, então sua session-1 está aberta
        verificar("Apenas a sessão de Bob deve ser inválida", 
                 resultado.size() == 1 && resultado.contains("session-1"), resultado);
    }

    // ===== TESTE COMPLEXO =====

    private static void testarCenarioComplexo() throws IOException {
        System.out.println("🧪 Teste 12: Cenário Complexo (múltiplos tipos de erros)");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,s1,LOGIN,/usr/bin/sshd,5,0\n" +
            "1100,alice,s2,LOGIN,/usr/bin/sshd,5,0\n" +          // LOGIN aninhado
            "1200,bob,s3,LOGOUT,/usr/bin/sshd,5,0\n" +            // LOGOUT órfão
            "1300,alice,s2,LOGOUT,/usr/bin/sshd,5,0\n" +          // LOGOUT correto
            "1400,carlos,s4,LOGIN,/usr/bin/sshd,5,0\n" +          // Fica aberto
            "1500,alice,s5,LOGOUT,/usr/bin/sshd,5,0\n" +          // LOGOUT incorreto
            "1600,alice,s1,LOGOUT,/usr/bin/sshd,5,0\n";           // LOGOUT correto
        
        String arquivo = criarArquivoTeste("teste12.csv", conteudo);
        Set<String> resultado = solucao.desafio1_encontrarSessoesInvalidas(arquivo);
        
        // Inválidas: s2 (aninhada), s3 (órfã), s4 (aberta), s5 (incorreta)
        verificar("Deve detectar 4 sessões inválidas", 
                 resultado.size() == 4, resultado);
    }

    // ===== TESTE VERSÃO ESTENDIDA =====

    private static void testarVersaoEstendida() throws IOException {
        System.out.println("\n🧪 Teste 13: Versão Estendida com Métricas");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,s1,LOGIN,/usr/bin/sshd,5,0\n" +
            "1100,alice,s2,LOGIN,/usr/bin/sshd,5,0\n" +
            "1200,bob,s3,LOGOUT,/usr/bin/sshd,5,0\n" +
            "1300,alice,s2,LOGOUT,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste13.csv", conteudo);
        ResultadoSessoesInvalidas resultado = 
            SessoesInvalidasExtended.analisarComMetricas(arquivo);
        
        System.out.println("\n" + resultado.gerarRelatorio());
        
        boolean passou = resultado.getTotalInvalidas() == 3 &&  // s1, s2, s3
                        resultado.getLoginAninhados() == 1 &&   // s2
                        resultado.getLogoutOrfaos() == 1 &&      // s3
                        resultado.getSessoesAbertas() == 1;      // s1
        
        if (passou) {
            System.out.println("✅ PASSOU: Métricas corretas");
            testesPassados++;
        } else {
            System.out.println("❌ FALHOU: Métricas incorretas");
            testesFalhados++;
        }
    }

    // ===== MÉTODOS AUXILIARES =====

    private static String criarArquivoTeste(String nome, String conteudo) throws IOException {
        Path tempDir = Files.createTempDirectory("forensic-tests");
        Path arquivo = tempDir.resolve(nome);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo.toFile()))) {
            writer.write(conteudo);
        }
        
        return arquivo.toString();
    }

    private static void verificar(String descricao, boolean condicao, Set<String> resultado) {
        if (condicao) {
            System.out.println("   ✅ PASSOU: " + descricao);
            testesPassados++;
        } else {
            System.out.println("   ❌ FALHOU: " + descricao);
            System.out.println("   Resultado: " + resultado);
            testesFalhados++;
        }
        System.out.println();
    }

    private static void imprimirResumo() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                     RESUMO DOS TESTES                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Total de testes: " + (testesPassados + testesFalhados));
        System.out.println("✅ Passados: " + testesPassados);
        System.out.println("❌ Falhados: " + testesFalhados);
        System.out.println();
        
        if (testesFalhados == 0) {
            System.out.println("🎉 TODOS OS TESTES PASSARAM! Implementação correta.");
        } else {
            System.out.println("⚠️  Alguns testes falharam. Revise a implementação.");
        }
    }
}
