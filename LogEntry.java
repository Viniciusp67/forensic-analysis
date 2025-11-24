package br.edu.icev.aed.forense;

/**
 * Classe que representa uma entrada (linha) do arquivo de log CSV.
 * Facilita o trabalho com os dados estruturados do log.
 */
public class LogEntry {
    private final long timestamp;
    private final String userId;
    private final String sessionId;
    private final String actionType;
    private final String targetResource;
    private final int severityLevel;
    private final long bytesTransferred;

    /**
     * Construtor completo de uma entrada de log
     * 
     * @param timestamp Timestamp Unix epoch
     * @param userId ID do usuário
     * @param sessionId ID da sessão
     * @param actionType Tipo da ação (LOGIN, LOGOUT, FILE_ACCESS, etc.)
     * @param targetResource Recurso alvo (caminho ou IP)
     * @param severityLevel Nível de severidade (1-10)
     * @param bytesTransferred Bytes transferidos
     */
    public LogEntry(long timestamp, String userId, String sessionId, 
                    String actionType, String targetResource, 
                    int severityLevel, long bytesTransferred) {
        this.timestamp = timestamp;
        this.userId = userId;
        this.sessionId = sessionId;
        this.actionType = actionType;
        this.targetResource = targetResource;
        this.severityLevel = severityLevel;
        this.bytesTransferred = bytesTransferred;
    }

    // Getters
    public long getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getActionType() {
        return actionType;
    }

    public String getTargetResource() {
        return targetResource;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }

    public long getBytesTransferred() {
        return bytesTransferred;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "timestamp=" + timestamp +
                ", userId='" + userId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", actionType='" + actionType + '\'' +
                ", targetResource='" + targetResource + '\'' +
                ", severityLevel=" + severityLevel +
                ", bytesTransferred=" + bytesTransferred +
                '}';
    }
}
