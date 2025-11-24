package br.edu.icev.aed.forense.test;

import br.edu.icev.aed.forense.Alerta;
import br.edu.icev.aed.forense.SolucaoForenseImpl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Classe de testes para validar a implementação dos desafios.
 * Execute esta classe para verificar se a solução está funcionando corretamente.
 */
public class TesteSolucao {

    public static void main(String[] args) {
        SolucaoForenseImpl solucao = new SolucaoForenseImpl();
        String arquivoTeste = "forensic_logs.csv";

        System.out.println("=== TESTE DA SOLUÇÃO DE ANÁLISE FORENSE ===\n");

        try {
            // Teste 1: Sessões Inválidas
            System.out.println("--- DESAFIO 1: Sessões Inválidas ---");
            Set<String> sessoesInvalidas = solucao.desafio1_encontrarSessoesInvalidas(arquivoTeste);
            System.out.println("Sessões inválidas encontradas: " + sessoesInvalidas.size());
            System.out.println("IDs: " + sessoesInvalidas);
            System.out.println();

            // Teste 2: Linha do Tempo
            System.out.println("--- DESAFIO 2: Linha do Tempo ---");
            String sessionId = "session-alpha-723";
            List<String> linhaTempo = solucao.desafio2_reconstruirLinhaDoTempo(arquivoTeste, sessionId);
            System.out.println("Linha do tempo da sessão " + sessionId + ":");
            System.out.println("Ações: " + linhaTempo);
            System.out.println();

            // Teste 3: Priorizar Alertas
            System.out.println("--- DESAFIO 3: Priorizar Alertas ---");
            int n = 5;
            List<Alerta> alertas = solucao.desafio3_priorizarAlertas(arquivoTeste, n);
            System.out.println("Top " + n + " alertas mais severos:");
           for (int i = 0; i < alertas.size(); i++) {
                Alerta a = alertas.get(i);
                System.out.println((i + 1) + ". " + a.toString());      
            }

            System.out.println();

            // Teste 4: Picos de Transferência
            System.out.println("--- DESAFIO 4: Picos de Transferência ---");
            Map<Long, Long> picos = solucao.desafio4_encontrarPicosDeTransferencia(arquivoTeste);
            System.out.println("Picos de transferência encontrados: " + picos.size());
            System.out.println("Primeiros 5 picos:");
            int count = 0;
            for (Map.Entry<Long, Long> entry : picos.entrySet()) {
                if (count >= 5) break;
                System.out.println("  Evento em " + entry.getKey() + 
                                 " -> próximo maior em " + entry.getValue());
                count++;
            }
            System.out.println();

            // Teste 5: Rastrear Contaminação
            System.out.println("--- DESAFIO 5: Rastrear Contaminação ---");
            String inicio = "/usr/bin/sshd";
            String alvo = "/var/secrets/key.dat";
            Optional<List<String>> caminho = solucao.desafio5_rastrearContaminacao(
                arquivoTeste, inicio, alvo
            );
            
            if (caminho.isPresent()) {
                System.out.println("Caminho de contaminação de " + inicio + " até " + alvo + ":");
                List<String> path = caminho.get();
                for (int i = 0; i < path.size(); i++) {
                    System.out.print(path.get(i));
                    if (i < path.size() - 1) {
                        System.out.print(" -> ");
                    }
                }
                System.out.println();
            } else {
                System.out.println("Nenhum caminho encontrado entre " + inicio + " e " + alvo);
            }
            System.out.println();

            System.out.println("=== TODOS OS TESTES EXECUTADOS COM SUCESSO ===");

        } catch (IOException e) {
            System.err.println("ERRO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
