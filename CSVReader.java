package br.edu.icev.aed.forense.util;

import br.edu.icev.aed.forense.LogEntry;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitária para leitura eficiente de arquivos CSV de logs.
 * Implementa padrões de boas práticas como uso de BufferedReader e try-with-resources.
 */
public class CSVReader {
    
    // Índices das colunas no CSV
    private static final int TIMESTAMP_INDEX = 0;
    private static final int USER_ID_INDEX = 1;
    private static final int SESSION_ID_INDEX = 2;
    private static final int ACTION_TYPE_INDEX = 3;
    private static final int TARGET_RESOURCE_INDEX = 4;
    private static final int SEVERITY_LEVEL_INDEX = 5;
    private static final int BYTES_TRANSFERRED_INDEX = 6;
    
    // Separador CSV
    private static final String CSV_SEPARATOR = ",";

    /**
     * Lê todas as entradas de log de um arquivo CSV
     * 
     * @param caminhoArquivo Caminho para o arquivo CSV
     * @return Lista de objetos LogEntry contendo todos os logs
     * @throws IOException Se houver erro na leitura do arquivo
     */
    public static List<LogEntry> lerTodosOsLogs(String caminhoArquivo) throws IOException {
        List<LogEntry> logs = new ArrayList<>();
        
        // try-with-resources garante o fechamento automático do arquivo
        try (BufferedReader reader = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha;
            boolean primeiraLinha = true;
            
            while ((linha = reader.readLine()) != null) {
                // Pular o cabeçalho
                if (primeiraLinha) {
                    primeiraLinha = false;
                    continue;
                }
                
                // Ignorar linhas vazias
                if (linha.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    LogEntry entry = parsearLinha(linha);
                    logs.add(entry);
                } catch (Exception e) {
                    // Log de erro (pode ser substituído por logging adequado)
                    System.err.println("Erro ao parsear linha: " + linha);
                    System.err.println("Erro: " + e.getMessage());
                }
            }
        }
        
        return logs;
    }

    /**
     * Converte uma linha CSV em um objeto LogEntry
     * 
     * @param linha Linha do CSV
     * @return Objeto LogEntry com os dados parseados
     * @throws IllegalArgumentException Se a linha não tiver o formato esperado
     */
    private static LogEntry parsearLinha(String linha) {
        String[] campos = linha.split(CSV_SEPARATOR);
        
        if (campos.length < 7) {
            throw new IllegalArgumentException("Linha CSV inválida: número insuficiente de campos");
        }
        
        try {
            long timestamp = Long.parseLong(campos[TIMESTAMP_INDEX].trim());
            String userId = campos[USER_ID_INDEX].trim();
            String sessionId = campos[SESSION_ID_INDEX].trim();
            String actionType = campos[ACTION_TYPE_INDEX].trim();
            String targetResource = campos[TARGET_RESOURCE_INDEX].trim();
            int severityLevel = Integer.parseInt(campos[SEVERITY_LEVEL_INDEX].trim());
            long bytesTransferred = Long.parseLong(campos[BYTES_TRANSFERRED_INDEX].trim());
            
            return new LogEntry(timestamp, userId, sessionId, actionType, 
                              targetResource, severityLevel, bytesTransferred);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Erro ao converter valores numéricos: " + e.getMessage());
        }
    }

    /**
     * Valida se um arquivo CSV existe e é legível
     * 
     * @param caminhoArquivo Caminho do arquivo
     * @return true se o arquivo é válido, false caso contrário
     */
    public static boolean validarArquivo(String caminhoArquivo) {
        if (caminhoArquivo == null || caminhoArquivo.trim().isEmpty()) {
            return false;
        }
        
        java.io.File arquivo = new java.io.File(caminhoArquivo);
        return arquivo.exists() && arquivo.isFile() && arquivo.canRead();
    }
}
