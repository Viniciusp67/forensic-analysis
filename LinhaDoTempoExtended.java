package br.edu.icev.aed.forense.extended;

import br.edu.icev.aed.forense.LogEntry;
import br.edu.icev.aed.forense.util.CSVReader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Versão estendida do Desafio 2 com análises adicionais da linha do tempo.
 * 
 * Além de reconstruir a linha do tempo, oferece:
 * - Estatísticas da sessão
 * - Duração entre ações
 * - Padrões de comportamento
 * - Análise de frequência de ações
 * 
 * USO OPCIONAL: Para análises forenses mais profundas.
 */
public class LinhaDoTempoExtended {

    /**
     * Classe para representar uma linha do tempo com metadados
     */
    public static class LinhaDoTempoDetalhada {
        private final List<String> acoes;
        private final long timestampInicio;
        private final long timestampFim;
        private final long duracaoTotal;
        private final int totalAcoes;
        private final Map<String, Integer> frequenciaAcoes;
        private final List<Long> duracoesentreAcoes;
        
        public LinhaDoTempoDetalhada(List<String> acoes, long inicio, long fim,
                                     Map<String, Integer> frequencia,
                                     List<Long> duracoes) {
            this.acoes = acoes;
            this.timestampInicio = inicio;
            this.timestampFim = fim;
            this.duracaoTotal = fim - inicio;
            this.totalAcoes = acoes.size();
            this.frequenciaAcoes = frequencia;
            this.duracoesentreAcoes = duracoes;
        }
        
        // Getters
        public List<String> getAcoes() { return acoes; }
        public long getTimestampInicio() { return timestampInicio; }
        public long getTimestampFim() { return timestampFim; }
        public long getDuracaoTotal() { return duracaoTotal; }
        public int getTotalAcoes() { return totalAcoes; }
        public Map<String, Integer> getFrequenciaAcoes() { return frequenciaAcoes; }
        public List<Long> getDuracoesentreAcoes() { return duracoesentreAcoes; }
        
        /**
         * Calcula a duração média entre ações
         */
        public double getDuracaoMediaEntreAcoes() {
            if (duracoesentreAcoes.isEmpty()) return 0.0;
            return duracoesentreAcoes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
        }
        
        /**
         * Encontra a ação mais frequente
         */
        public String getAcaoMaisFrequente() {
            return frequenciaAcoes.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }
        
        /**
         * Gera relatório textual
         */
        public String gerarRelatorio() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== ANÁLISE DA LINHA DO TEMPO ===\n\n");
            
            sb.append("Resumo Geral:\n");
            sb.append(String.format("  Timestamp início: %d\n", timestampInicio));
            sb.append(String.format("  Timestamp fim: %d\n", timestampFim));
            sb.append(String.format("  Duração total: %d segundos\n", duracaoTotal));
            sb.append(String.format("  Total de ações: %d\n\n", totalAcoes));
            
            sb.append("Sequência de Ações:\n");
            for (int i = 0; i < acoes.size(); i++) {
                sb.append(String.format("  %d. %s\n", i + 1, acoes.get(i)));
            }
            sb.append("\n");
            
            sb.append("Frequência de Ações:\n");
            frequenciaAcoes.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(e -> sb.append(String.format("  %s: %d vezes\n", 
                                                         e.getKey(), e.getValue())));
            sb.append("\n");
            
            if (!duracoesentreAcoes.isEmpty()) {
                sb.append("Duração entre Ações:\n");
                sb.append(String.format("  Média: %.2f segundos\n", getDuracaoMediaEntreAcoes()));
                sb.append(String.format("  Mínima: %d segundos\n", 
                                       Collections.min(duracoesentreAcoes)));
                sb.append(String.format("  Máxima: %d segundos\n", 
                                       Collections.max(duracoesentreAcoes)));
            }
            
            return sb.toString();
        }
        
        @Override
        public String toString() {
            return String.format("LinhaDoTempo{acoes=%d, duração=%ds, mais_frequente=%s}",
                               totalAcoes, duracaoTotal, getAcaoMaisFrequente());
        }
    }

    /**
     * Reconstrói a linha do tempo com análise detalhada
     */
    public static LinhaDoTempoDetalhada analisarDetalhado(
            String caminhoArquivoCsv, String sessionId) throws IOException {
        
        List<LogEntry> logs = CSVReader.lerTodosOsLogs(caminhoArquivoCsv);
        
        // Filtrar logs da sessão
        List<LogEntry> logsSession = logs.stream()
                .filter(log -> sessionId.equals(log.getSessionId()))
                .sorted(Comparator.comparingLong(LogEntry::getTimestamp))
                .collect(Collectors.toList());
        
        if (logsSession.isEmpty()) {
            return new LinhaDoTempoDetalhada(
                new ArrayList<>(), 0, 0, new HashMap<>(), new ArrayList<>()
            );
        }
        
        // Construir lista de ações
        List<String> acoes = logsSession.stream()
                .map(LogEntry::getActionType)
                .collect(Collectors.toList());
        
        // Calcular timestamps
        long inicio = logsSession.get(0).getTimestamp();
        long fim = logsSession.get(logsSession.size() - 1).getTimestamp();
        
        // Calcular frequência de ações
        Map<String, Integer> frequencia = new HashMap<>();
        for (String acao : acoes) {
            frequencia.merge(acao, 1, Integer::sum);
        }
        
        // Calcular durações entre ações
        List<Long> duracoes = new ArrayList<>();
        for (int i = 1; i < logsSession.size(); i++) {
            long duracao = logsSession.get(i).getTimestamp() - 
                          logsSession.get(i - 1).getTimestamp();
            duracoes.add(duracao);
        }
        
        return new LinhaDoTempoDetalhada(acoes, inicio, fim, frequencia, duracoes);
    }

    /**
     * Versão simples que retorna apenas a lista de ações
     */
    public static List<String> reconstruir(String caminhoArquivoCsv, String sessionId) 
            throws IOException {
        return analisarDetalhado(caminhoArquivoCsv, sessionId).getAcoes();
    }

    /**
     * Compara linhas do tempo de múltiplas sessões
     */
    public static Map<String, LinhaDoTempoDetalhada> compararSessoes(
            String caminhoArquivoCsv, List<String> sessionIds) throws IOException {
        
        Map<String, LinhaDoTempoDetalhada> resultado = new LinkedHashMap<>();
        
        for (String sessionId : sessionIds) {
            resultado.put(sessionId, analisarDetalhado(caminhoArquivoCsv, sessionId));
        }
        
        return resultado;
    }

    /**
     * Identifica padrões suspeitos na linha do tempo
     */
    public static List<String> identificarPadroesSuspeitos(
            String caminhoArquivoCsv, String sessionId) throws IOException {
        
        LinhaDoTempoDetalhada analise = analisarDetalhado(caminhoArquivoCsv, sessionId);
        List<String> padroesSuspeitos = new ArrayList<>();
        
        List<String> acoes = analise.getAcoes();
        
        // Padrão 1: Muitos FILE_ACCESS consecutivos (possível exfiltração)
        int fileAccessConsecutivos = 0;
        for (String acao : acoes) {
            if ("FILE_ACCESS".equals(acao)) {
                fileAccessConsecutivos++;
                if (fileAccessConsecutivos >= 5) {
                    padroesSuspeitos.add("Múltiplos FILE_ACCESS consecutivos (>= 5)");
                    break;
                }
            } else {
                fileAccessConsecutivos = 0;
            }
        }
        
        // Padrão 2: COMMAND_EXEC após FILE_ACCESS (possível script malicioso)
        for (int i = 0; i < acoes.size() - 1; i++) {
            if ("FILE_ACCESS".equals(acoes.get(i)) && 
                "COMMAND_EXEC".equals(acoes.get(i + 1))) {
                padroesSuspeitos.add("FILE_ACCESS seguido de COMMAND_EXEC");
                break;
            }
        }
        
        // Padrão 3: DATA_TRANSFER sem LOGOUT (sessão de exfiltração contínua)
        if (acoes.contains("DATA_TRANSFER") && !acoes.contains("LOGOUT")) {
            padroesSuspeitos.add("DATA_TRANSFER sem LOGOUT subsequente");
        }
        
        // Padrão 4: Sessão muito longa (>100 ações)
        if (acoes.size() > 100) {
            padroesSuspeitos.add(String.format("Sessão muito longa (%d ações)", acoes.size()));
        }
        
        // Padrão 5: Duração média entre ações muito curta (possível automação)
        if (analise.getDuracaoMediaEntreAcoes() < 1.0 && acoes.size() > 10) {
            padroesSuspeitos.add(String.format("Ações muito rápidas (média: %.2fs)", 
                                             analise.getDuracaoMediaEntreAcoes()));
        }
        
        return padroesSuspeitos;
    }

    /**
     * Gera visualização ASCII da linha do tempo
     */
    public static String gerarVisualizacaoASCII(String caminhoArquivoCsv, String sessionId) 
            throws IOException {
        
        LinhaDoTempoDetalhada analise = analisarDetalhado(caminhoArquivoCsv, sessionId);
        StringBuilder sb = new StringBuilder();
        
        sb.append("┌────────────────────────────────────────────────────────┐\n");
        sb.append("│         LINHA DO TEMPO - ").append(sessionId).append("\n");
        sb.append("└────────────────────────────────────────────────────────┘\n\n");
        
        List<String> acoes = analise.getAcoes();
        long inicio = analise.getTimestampInicio();
        
        sb.append("Tempo → \n");
        
        for (int i = 0; i < acoes.size(); i++) {
            long relativo = 0;
            if (i > 0) {
                // Tempo relativo ao início (simplificado)
                sb.append("  ↓\n");
            }
            sb.append(String.format("  [%d] %s\n", i + 1, acoes.get(i)));
        }
        
        sb.append("\n");
        sb.append(String.format("Duração total: %d segundos\n", analise.getDuracaoTotal()));
        sb.append(String.format("Total de ações: %d\n", acoes.size()));
        
        return sb.toString();
    }
}
