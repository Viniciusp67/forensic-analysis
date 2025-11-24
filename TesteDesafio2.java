package br.edu.icev.aed.forense.test;

import br.edu.icev.aed.forense.SolucaoForenseImpl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Bateria de testes específicos para o Desafio 2: Reconstruir Linha do Tempo.
 * 
 * Esta classe testa todos os cenários possíveis:
 * - Sessão normal com múltiplas ações
 * - Sessão não existente
 * - Sessão com uma única ação
 * - Múltiplas sessões entrelaçadas
 * - Ações repetidas
 * - Casos extremos
 */
public class TesteDesafio2 {

    private static final SolucaoForenseImpl solucao = new SolucaoForenseImpl();
    private static int testesPassados = 0;
    private static int testesFalhados = 0;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║     BATERIA DE TESTES - DESAFIO 2: LINHA DO TEMPO         ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");

        try {
            // Testes básicos
            testarSessaoNormal();
            testarSessaoComMultiplasAcoes();
            testarSessaoNaoExistente();
            testarSessaoUnicaAcao();
            
            // Testes com múltiplas sessões
            testarSessoesEntrelacadas();
            testarMultiplasSessionsIndependentes();
            
            // Testes com ações repetidas
            testarAcoesRepetidas();
            
            // Casos extremos
            testarArquivoVazio();
            testarApenasUmaSessao();
            testarMuitasAcoes();
            
            // Teste de ordem cronológica
            testarOrdemCronologicaCompleta();
            
            // Resumo
            imprimirResumo();

        } catch (Exception e) {
            System.err.println("ERRO FATAL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== TESTES BÁSICOS =====

    private static void testarSessaoNormal() throws IOException {
        System.out.println("🧪 Teste 1: Sessão Normal");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-1,LOGIN,/usr/bin/sshd,5,0\n" +
            "2000,alice,session-1,FILE_ACCESS,/var/log/auth.log,5,0\n" +
            "3000,alice,session-1,LOGOUT,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste1.csv", conteudo);
        List<String> resultado = solucao.desafio2_reconstruirLinhaDoTempo(arquivo, "session-1");
        
        List<String> esperado = Arrays.asList("LOGIN", "FILE_ACCESS", "LOGOUT");
        
        verificar("Deve reconstruir linha do tempo corretamente", 
                 resultado.equals(esperado), resultado, esperado);
    }

    private static void testarSessaoComMultiplasAcoes() throws IOException {
        System.out.println("🧪 Teste 2: Sessão com Múltiplas Ações");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-alpha,LOGIN,/usr/bin/sshd,5,0\n" +
            "1100,alice,session-alpha,COMMAND_EXEC,/bin/ls,3,1024\n" +
            "1200,alice,session-alpha,FILE_ACCESS,/var/log/auth.log,7,4096\n" +
            "1300,alice,session-alpha,DATA_TRANSFER,198.51.100.2,8,512000\n" +
            "1400,alice,session-alpha,FILE_ACCESS,/var/secrets/key.dat,10,256\n" +
            "1500,alice,session-alpha,LOGOUT,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste2.csv", conteudo);
        List<String> resultado = solucao.desafio2_reconstruirLinhaDoTempo(arquivo, "session-alpha");
        
        List<String> esperado = Arrays.asList(
            "LOGIN", "COMMAND_EXEC", "FILE_ACCESS", "DATA_TRANSFER", "FILE_ACCESS", "LOGOUT"
        );
        
        verificar("Deve preservar todas as ações na ordem", 
                 resultado.equals(esperado), resultado, esperado);
    }

    private static void testarSessaoNaoExistente() throws IOException {
        System.out.println("🧪 Teste 3: Sessão Não Existente");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-1,LOGIN,/usr/bin/sshd,5,0\n" +
            "2000,bob,session-2,LOGIN,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste3.csv", conteudo);
        List<String> resultado = solucao.desafio2_reconstruirLinhaDoTempo(arquivo, "session-X");
        
        verificar("Deve retornar lista vazia para sessão não existente", 
                 resultado.isEmpty(), resultado, Arrays.asList());
    }

    private static void testarSessaoUnicaAcao() throws IOException {
        System.out.println("🧪 Teste 4: Sessão com Única Ação");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-1,LOGIN,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste4.csv", conteudo);
        List<String> resultado = solucao.desafio2_reconstruirLinhaDoTempo(arquivo, "session-1");
        
        List<String> esperado = Arrays.asList("LOGIN");
        
        verificar("Deve funcionar com apenas uma ação", 
                 resultado.equals(esperado), resultado, esperado);
    }

    // ===== TESTES COM MÚLTIPLAS SESSÕES =====

    private static void testarSessoesEntrelacadas() throws IOException {
        System.out.println("🧪 Teste 5: Sessões Entrelaçadas");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-A,LOGIN,/usr/bin/sshd,5,0\n" +
            "1100,bob,session-B,LOGIN,/usr/bin/sshd,5,0\n" +
            "1200,alice,session-A,FILE_ACCESS,/var/log,5,0\n" +
            "1300,bob,session-B,FILE_ACCESS,/home,5,0\n" +
            "1400,alice,session-A,LOGOUT,/usr/bin/sshd,5,0\n" +
            "1500,bob,session-B,LOGOUT,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste5.csv", conteudo);
        
        // Testar session-A
        List<String> resultadoA = solucao.desafio2_reconstruirLinhaDoTempo(arquivo, "session-A");
        List<String> esperadoA = Arrays.asList("LOGIN", "FILE_ACCESS", "LOGOUT");
        
        // Testar session-B
        List<String> resultadoB = solucao.desafio2_reconstruirLinhaDoTempo(arquivo, "session-B");
        List<String> esperadoB = Arrays.asList("LOGIN", "FILE_ACCESS", "LOGOUT");
        
        boolean passou = resultadoA.equals(esperadoA) && resultadoB.equals(esperadoB);
        
        if (passou) {
            System.out.println("   ✅ PASSOU: Sessões independentes filtradas corretamente");
            testesPassados++;
        } else {
            System.out.println("   ❌ FALHOU: Erro na filtragem de sessões");
            System.out.println("   Session-A - Esperado: " + esperadoA + ", Obtido: " + resultadoA);
            System.out.println("   Session-B - Esperado: " + esperadoB + ", Obtido: " + resultadoB);
            testesFalhados++;
        }
        System.out.println();
    }

    private static void testarMultiplasSessionsIndependentes() throws IOException {
        System.out.println("🧪 Teste 6: Múltiplas Sessões Independentes");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,s1,LOGIN,/usr/bin/sshd,5,0\n" +
            "1100,bob,s2,LOGIN,/usr/bin/sshd,5,0\n" +
            "1200,carlos,s3,LOGIN,/usr/bin/sshd,5,0\n" +
            "1300,alice,s1,LOGOUT,/usr/bin/sshd,5,0\n" +
            "1400,bob,s2,LOGOUT,/usr/bin/sshd,5,0\n" +
            "1500,carlos,s3,LOGOUT,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste6.csv", conteudo);
        
        List<String> resultado1 = solucao.desafio2_reconstruirLinhaDoTempo(arquivo, "s1");
        List<String> resultado2 = solucao.desafio2_reconstruirLinhaDoTempo(arquivo, "s2");
        List<String> resultado3 = solucao.desafio2_reconstruirLinhaDoTempo(arquivo, "s3");
        
        List<String> esperado = Arrays.asList("LOGIN", "LOGOUT");
        
        boolean passou = resultado1.equals(esperado) && 
                        resultado2.equals(esperado) && 
                        resultado3.equals(esperado);
        
        if (passou) {
            System.out.println("   ✅ PASSOU: Todas as sessões independentes corretas");
            testesPassados++;
        } else {
            System.out.println("   ❌ FALHOU: Erro em sessões independentes");
            testesFalhados++;
        }
        System.out.println();
    }

    // ===== TESTES COM AÇÕES REPETIDAS =====

    private static void testarAcoesRepetidas() throws IOException {
        System.out.println("🧪 Teste 7: Ações Repetidas");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-1,FILE_ACCESS,/file1,5,0\n" +
            "1100,alice,session-1,FILE_ACCESS,/file2,5,0\n" +
            "1200,alice,session-1,FILE_ACCESS,/file3,5,0\n" +
            "1300,alice,session-1,FILE_ACCESS,/file4,5,0\n";
        
        String arquivo = criarArquivoTeste("teste7.csv", conteudo);
        List<String> resultado = solucao.desafio2_reconstruirLinhaDoTempo(arquivo, "session-1");
        
        List<String> esperado = Arrays.asList("FILE_ACCESS", "FILE_ACCESS", "FILE_ACCESS", "FILE_ACCESS");
        
        verificar("Deve preservar ações repetidas", 
                 resultado.equals(esperado), resultado, esperado);
    }

    // ===== CASOS EXTREMOS =====

    private static void testarArquivoVazio() throws IOException {
        System.out.println("🧪 Teste 8: Arquivo Vazio");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n";
        
        String arquivo = criarArquivoTeste("teste8.csv", conteudo);
        List<String> resultado = solucao.desafio2_reconstruirLinhaDoTempo(arquivo, "session-1");
        
        verificar("Deve retornar lista vazia para arquivo vazio", 
                 resultado.isEmpty(), resultado, Arrays.asList());
    }

    private static void testarApenasUmaSessao() throws IOException {
        System.out.println("🧪 Teste 9: Apenas Uma Sessão no Arquivo");
        
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,session-X,LOGIN,/usr/bin/sshd,5,0\n" +
            "1100,alice,session-X,COMMAND_EXEC,/bin/ls,3,1024\n" +
            "1200,alice,session-X,LOGOUT,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste9.csv", conteudo);
        List<String> resultado = solucao.desafio2_reconstruirLinhaDoTempo(arquivo, "session-X");
        
        List<String> esperado = Arrays.asList("LOGIN", "COMMAND_EXEC", "LOGOUT");
        
        verificar("Deve funcionar com apenas uma sessão no arquivo", 
                 resultado.equals(esperado), resultado, esperado);
    }

    private static void testarMuitasAcoes() throws IOException {
        System.out.println("🧪 Teste 10: Muitas Ações (50 eventos)");
        
        StringBuilder sb = new StringBuilder();
        sb.append("TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n");
        
        // Criar 50 eventos
        for (int i = 1; i <= 50; i++) {
            sb.append(String.format("%d,alice,session-big,FILE_ACCESS,/file%d,5,0\n", 
                                   1000 + i * 10, i));
        }
        
        String arquivo = criarArquivoTeste("teste10.csv", sb.toString());
        List<String> resultado = solucao.desafio2_reconstruirLinhaDoTempo(arquivo, "session-big");
        
        boolean passou = resultado.size() == 50 && 
                        resultado.stream().allMatch(a -> a.equals("FILE_ACCESS"));
        
        if (passou) {
            System.out.println("   ✅ PASSOU: Processou 50 eventos corretamente");
            testesPassados++;
        } else {
            System.out.println("   ❌ FALHOU: Erro ao processar muitos eventos");
            System.out.println("   Esperado: 50 eventos FILE_ACCESS");
            System.out.println("   Obtido: " + resultado.size() + " eventos");
            testesFalhados++;
        }
        System.out.println();
    }

    // ===== TESTE DE ORDEM CRONOLÓGICA =====

    private static void testarOrdemCronologicaCompleta() throws IOException {
        System.out.println("🧪 Teste 11: Ordem Cronológica Completa");
        
        // Propositalmente desordenado por SessionID mas ordenado por timestamp
        String conteudo = 
            "TIMESTAMP,USER_ID,SESSION_ID,ACTION_TYPE,TARGET_RESOURCE,SEVERITY_LEVEL,BYTES_TRANSFERRED\n" +
            "1000,alice,target,LOGIN,/usr/bin/sshd,5,0\n" +
            "1100,bob,other,LOGIN,/usr/bin/sshd,5,0\n" +
            "1200,alice,target,FILE_ACCESS,/file1,5,0\n" +
            "1300,bob,other,FILE_ACCESS,/file2,5,0\n" +
            "1400,alice,target,COMMAND_EXEC,/bin/cmd,5,0\n" +
            "1500,bob,other,LOGOUT,/usr/bin/sshd,5,0\n" +
            "1600,alice,target,DATA_TRANSFER,192.168.1.1,5,0\n" +
            "1700,alice,target,LOGOUT,/usr/bin/sshd,5,0\n";
        
        String arquivo = criarArquivoTeste("teste11.csv", conteudo);
        List<String> resultado = solucao.desafio2_reconstruirLinhaDoTempo(arquivo, "target");
        
        List<String> esperado = Arrays.asList(
            "LOGIN", "FILE_ACCESS", "COMMAND_EXEC", "DATA_TRANSFER", "LOGOUT"
        );
        
        verificar("Deve manter ordem cronológica mesmo com sessões entrelaçadas", 
                 resultado.equals(esperado), resultado, esperado);
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

    private static void verificar(String descricao, boolean condicao, 
                                  List<String> resultado, List<String> esperado) {
        if (condicao) {
            System.out.println("   ✅ PASSOU: " + descricao);
            testesPassados++;
        } else {
            System.out.println("   ❌ FALHOU: " + descricao);
            System.out.println("   Esperado: " + esperado);
            System.out.println("   Obtido: " + resultado);
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
