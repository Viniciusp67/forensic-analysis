package br.edu.icev.aed.forense.model;

import java.util.Set;

/**
 * Classe que encapsula o resultado detalhado do Desafio 1.
 * Além das sessões inválidas, fornece métricas e estatísticas úteis.
 * 
 * Esta é uma versão estendida OPCIONAL que pode ser usada para
 * análises mais profundas.
 */
public class ResultadoSessoesInvalidas {
    
    /**
     * Conjunto de IDs de sessões inválidas
     */
    private final Set<String> sessoesInvalidas;
    
    /**
     * Número de LOGINs aninhados detectados
     */
    private final int loginAninhados;
    
    /**
     * Número de LOGOUTs órfãos detectados (sem LOGIN correspondente)
     */
    private final int logoutOrfaos;
    
    /**
     * Número de sessões que ficaram abertas (sem LOGOUT)
     */
    private final int sessoesAbertas;
    
    /**
     * Número de LOGOUTs incorretos (não correspondem ao topo da pilha)
     */
    private final int logoutIncorretos;
    
    /**
     * Número total de usuários processados
     */
    private final int totalUsuarios;
    
    /**
     * Número total de eventos processados
     */
    private final int totalEventos;

    /**
     * Construtor completo
     */
    public ResultadoSessoesInvalidas(Set<String> sessoesInvalidas,
                                      int loginAninhados,
                                      int logoutOrfaos,
                                      int sessoesAbertas,
                                      int logoutIncorretos,
                                      int totalUsuarios,
                                      int totalEventos) {
        this.sessoesInvalidas = sessoesInvalidas;
        this.loginAninhados = loginAninhados;
        this.logoutOrfaos = logoutOrfaos;
        this.sessoesAbertas = sessoesAbertas;
        this.logoutIncorretos = logoutIncorretos;
        this.totalUsuarios = totalUsuarios;
        this.totalEventos = totalEventos;
    }

    // Getters
    
    public Set<String> getSessoesInvalidas() {
        return sessoesInvalidas;
    }

    public int getLoginAninhados() {
        return loginAninhados;
    }

    public int getLogoutOrfaos() {
        return logoutOrfaos;
    }

    public int getSessoesAbertas() {
        return sessoesAbertas;
    }

    public int getLogoutIncorretos() {
        return logoutIncorretos;
    }

    public int getTotalUsuarios() {
        return totalUsuarios;
    }

    public int getTotalEventos() {
        return totalEventos;
    }
    
    /**
     * Número total de sessões inválidas
     */
    public int getTotalInvalidas() {
        return sessoesInvalidas.size();
    }
    
    /**
     * Percentual de eventos que resultaram em sessões inválidas
     */
    public double getPercentualInvalidas() {
        if (totalEventos == 0) return 0.0;
        return (getTotalInvalidas() * 100.0) / totalEventos;
    }

    /**
     * Gera relatório textual detalhado
     */
    public String gerarRelatorio() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RELATÓRIO DE SESSÕES INVÁLIDAS ===\n\n");
        
        sb.append("Resumo Geral:\n");
        sb.append(String.format("  Total de eventos processados: %d\n", totalEventos));
        sb.append(String.format("  Total de usuários: %d\n", totalUsuarios));
        sb.append(String.format("  Total de sessões inválidas: %d (%.2f%%)\n\n", 
                  getTotalInvalidas(), getPercentualInvalidas()));
        
        sb.append("Tipos de Anomalias:\n");
        sb.append(String.format("  LOGINs aninhados: %d\n", loginAninhados));
        sb.append(String.format("  LOGOUTs órfãos: %d\n", logoutOrfaos));
        sb.append(String.format("  LOGOUTs incorretos: %d\n", logoutIncorretos));
        sb.append(String.format("  Sessões abertas (sem LOGOUT): %d\n\n", sessoesAbertas));
        
        sb.append("IDs das Sessões Inválidas:\n");
        if (sessoesInvalidas.isEmpty()) {
            sb.append("  Nenhuma sessão inválida detectada.\n");
        } else {
            for (String sessionId : sessoesInvalidas) {
                sb.append("  - ").append(sessionId).append("\n");
            }
        }
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("ResultadoSessoesInvalidas{invalidas=%d, LOGIN_aninhados=%d, " +
                           "LOGOUT_orfaos=%d, LOGOUT_incorretos=%d, sessoes_abertas=%d}",
                           getTotalInvalidas(), loginAninhados, logoutOrfaos, 
                           logoutIncorretos, sessoesAbertas);
    }
}
