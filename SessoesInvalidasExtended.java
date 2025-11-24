package br.edu.icev.aed.forense.extended;

import br.edu.icev.aed.forense.LogEntry;
import br.edu.icev.aed.forense.model.ResultadoSessoesInvalidas;
import br.edu.icev.aed.forense.util.CSVReader;

import java.io.IOException;
import java.util.*;

/**
 * Versão estendida do Desafio 1 com métricas detalhadas.
 * 
 * Esta classe oferece análises mais profundas sobre as sessões inválidas,
 * incluindo contadores por tipo de anomalia e relatórios detalhados.
 * 
 * USO OPCIONAL: Para análises detalhadas além do requisito básico.
 * 
 * @author Análise Forense - Versão Estendida
 */
public class SessoesInvalidasExtended {

    /**
     * Versão estendida que retorna informações detalhadas sobre sessões inválidas.
     * 
     * Esta versão mantém a mesma complexidade O(n) mas fornece métricas adicionais
     * úteis para análise forense detalhada.
     * 
     * @param caminhoArquivoCsv Caminho para o arquivo de logs
     * @return Objeto com sessões inválidas e estatísticas detalhadas
     * @throws IOException Se houver erro na leitura do arquivo
     */
    public static ResultadoSessoesInvalidas analisarComMetricas(String caminhoArquivoCsv) 
            throws IOException {
        
        // Validação de entrada
        if (caminhoArquivoCsv == null || caminhoArquivoCsv.trim().isEmpty()) {
            throw new IllegalArgumentException("Caminho do arquivo não pode ser nulo ou vazio");
        }
        
        // Estruturas de dados principais
        Set<String> sessoesInvalidas = new HashSet<>();
        Map<String, Stack<String>> pilhasPorUsuario = new HashMap<>();
        
        // Contadores de métricas
        int loginAninhados = 0;
        int logoutOrfaos = 0;
        int logoutIncorretos = 0;
        int totalEventos = 0;
        
        // Ler todos os logs
        List<LogEntry> logs = CSVReader.lerTodosOsLogs(caminhoArquivoCsv);
        totalEventos = logs.size();
        
        // Processar cada log
        for (LogEntry log : logs) {
            String userId = log.getUserId();
            String sessionId = log.getSessionId();
            String actionType = log.getActionType();
            
            // Garantir que o usuário tenha uma pilha
            pilhasPorUsuario.putIfAbsent(userId, new Stack<>());
            Stack<String> pilhaUsuario = pilhasPorUsuario.get(userId);
            
            if ("LOGIN".equals(actionType)) {
                // LOGIN: verificar se já há sessão ativa (LOGIN aninhado)
                if (!pilhaUsuario.isEmpty()) {
                    sessoesInvalidas.add(sessionId);
                    loginAninhados++;
                }
                // Empilhar a sessão atual
                pilhaUsuario.push(sessionId);
                
            } else if ("LOGOUT".equals(actionType)) {
                // LOGOUT: três possibilidades
                
                if (pilhaUsuario.isEmpty()) {
                    // Caso 1: LOGOUT sem LOGIN correspondente (órfão)
                    sessoesInvalidas.add(sessionId);
                    logoutOrfaos++;
                    
                } else {
                    String sessaoTopo = pilhaUsuario.peek();
                    
                    if (sessaoTopo.equals(sessionId)) {
                        // Caso 2: LOGOUT correto - remove da pilha
                        pilhaUsuario.pop();
                        
                    } else {
                        // Caso 3: LOGOUT não corresponde ao topo (incorreto)
                        sessoesInvalidas.add(sessionId);
                        logoutIncorretos++;
                    }
                }
            }
        }
        
        // Contar sessões abertas (sem LOGOUT)
        int sessoesAbertas = 0;
        for (Stack<String> pilha : pilhasPorUsuario.values()) {
            sessoesAbertas += pilha.size();
            sessoesInvalidas.addAll(pilha);
        }
        
        // Construir e retornar resultado
        return new ResultadoSessoesInvalidas(
            sessoesInvalidas,
            loginAninhados,
            logoutOrfaos,
            sessoesAbertas,
            logoutIncorretos,
            pilhasPorUsuario.size(),  // total de usuários
            totalEventos
        );
    }

    /**
     * Versão que gera apenas o Set de sessões inválidas (compatível com a interface).
     * Delega para a versão com métricas mas retorna apenas o Set.
     * 
     * @param caminhoArquivoCsv Caminho para o arquivo de logs
     * @return Set com IDs das sessões inválidas
     * @throws IOException Se houver erro na leitura do arquivo
     */
    public static Set<String> analisar(String caminhoArquivoCsv) throws IOException {
        return analisarComMetricas(caminhoArquivoCsv).getSessoesInvalidas();
    }

    /**
     * Versão que imprime relatório detalhado no console.
     * Útil para debugging e análise exploratória.
     * 
     * @param caminhoArquivoCsv Caminho para o arquivo de logs
     * @throws IOException Se houver erro na leitura do arquivo
     */
    public static void analisarComRelatorio(String caminhoArquivoCsv) throws IOException {
        ResultadoSessoesInvalidas resultado = analisarComMetricas(caminhoArquivoCsv);
        System.out.println(resultado.gerarRelatorio());
    }

    /**
     * Identifica usuários com mais sessões inválidas.
     * Útil para identificar contas comprometidas.
     * 
     * @param caminhoArquivoCsv Caminho para o arquivo de logs
     * @param topN Quantos usuários retornar
     * @return Map com usuários e contagem de sessões inválidas, ordenado
     * @throws IOException Se houver erro na leitura do arquivo
     */
    public static Map<String, Integer> usuariosComMaisSessoesInvalidas(
            String caminhoArquivoCsv, int topN) throws IOException {
        
        Map<String, Integer> contagemPorUsuario = new HashMap<>();
        Set<String> sessoesInvalidas = analisar(caminhoArquivoCsv);
        
        // Ler logs novamente para mapear sessões -> usuários
        List<LogEntry> logs = CSVReader.lerTodosOsLogs(caminhoArquivoCsv);
        
        for (LogEntry log : logs) {
            if (sessoesInvalidas.contains(log.getSessionId())) {
                contagemPorUsuario.merge(log.getUserId(), 1, Integer::sum);
            }
        }
        
        // Ordenar por contagem decrescente e pegar top N
        return contagemPorUsuario.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topN)
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll);
    }

    /**
     * Analisa padrões temporais de sessões inválidas.
     * Agrupa por janelas de tempo para identificar períodos suspeitos.
     * 
     * @param caminhoArquivoCsv Caminho para o arquivo de logs
     * @param janelaTempo Tamanho da janela em segundos
     * @return Map com timestamp inicial da janela e contagem de inválidas
     * @throws IOException Se houver erro na leitura do arquivo
     */
    public static Map<Long, Integer> padraoTemporalInvalidas(
            String caminhoArquivoCsv, long janelaTempo) throws IOException {
        
        Map<Long, Integer> contagemPorJanela = new TreeMap<>();
        Set<String> sessoesInvalidas = analisar(caminhoArquivoCsv);
        
        List<LogEntry> logs = CSVReader.lerTodosOsLogs(caminhoArquivoCsv);
        
        for (LogEntry log : logs) {
            if (sessoesInvalidas.contains(log.getSessionId())) {
                long janela = (log.getTimestamp() / janelaTempo) * janelaTempo;
                contagemPorJanela.merge(janela, 1, Integer::sum);
            }
        }
        
        return contagemPorJanela;
    }
}
