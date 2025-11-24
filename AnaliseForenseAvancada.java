package br.edu.icev.aed.forense;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Contrato para a solução dos desafios avançados de análise forense.
 * Sua classe deve implementar esta interface e possuir um construtor público sem argumentos.
 */
public interface AnaliseForenseAvancada {
    
    /**
     * Desafio 1 (Pilha): Encontra sessões de usuário que foram corrompidas ou
     * deixadas abertas, indicando uma possível falha ou ataque. Uma sessão é
     * inválida se um usuário tenta um novo LOGIN antes de um LOGOUT correspondente,
     * ou se termina sem LOGOUT correspondente.
     * 
     * @param caminhoArquivoCsv O caminho para o arquivo de logs.
     * @return Um Set contendo os IDs de todas as sessões (SESSION_ID) inválidas.
     * @throws IOException Se ocorrer um erro de leitura do arquivo.
     */
    Set<String> desafio1_encontrarSessoesInvalidas(String caminhoArquivoCsv) throws IOException;

    /**
     * Desafio 2 (Fila): Reconstrói a sequência exata de ações de um usuário dentro
     * de uma sessão específica, da primeira à última ação.
     * 
     * @param caminhoArquivoCsv O caminho para o arquivo de logs.
     * @param sessionId O ID da sessão a ser reconstruída.
     * @return Uma List<String> contendo os ACTION_TYPE na ordem cronológica em que
     *         ocorreram dentro da sessão. Retorna uma lista vazia se a sessão não for encontrada.
     * @throws IOException Se ocorrer um erro de leitura do arquivo.
     */
    List<String> desafio2_reconstruirLinhaDoTempo(String caminhoArquivoCsv, 
                                                   String sessionId) throws IOException;

    /**
     * Desafio 3 (Fila de Prioridade): Identifica os N eventos de maior risco
     * para que a equipe de resposta a incidentes possa priorizar. O risco é
     * determinado pelo campo SEVERITY_LEVEL.
     * 
     * @param caminhoArquivoCsv O caminho para o arquivo de logs.
     * @param n O número de eventos de maior risco a serem retornados.
     * @return Uma List<Alerta> contendo os 'n' alertas mais severos, ordenados do
     *         mais severo para o menos severo. Em caso de empate na severidade, a ordem
     *         não importa.
     * @throws IOException Se ocorrer um erro de leitura do arquivo.
     */
    List<Alerta> desafio3_priorizarAlertas(String caminhoArquivoCsv, int n) throws IOException;

    /**
     * Desafio 4 (Pilha Monotônica): Detecta anomalias em transferências de dados.
     * Para cada evento de transferência, encontra o próximo evento no tempo que
     * envolveu uma transferência de dados MAIOR. Isso ajuda a identificar
     * escalamentos súbitos na exfiltração de dados.
     * 
     * @param caminhoArquivoCsv O caminho para o arquivo de logs.
     * @return Um Map<Long, Long> onde a chave é o TIMESTAMP de um evento e o valor
     *         é o TIMESTAMP do próximo evento com BYTES_TRANSFERRED maior. Se não houver
     *         um evento maior subsequente, a chave não deve estar no mapa.
     * @throws IOException Se ocorrer um erro de leitura do arquivo.
     */
    Map<Long, Long> desafio4_encontrarPicosDeTransferencia(String caminhoArquivoCsv) throws IOException;

    /**
     * Desafio 5 (Grafo): Mapeia o caminho de contaminação do invasor através do
     * sistema, mostrando como ele se moveu de um recurso para outro. O caminho é
     * a sequência mais curta de recursos acessados entre o ponto de entrada e o alvo final.
     * 
     * @param caminhoArquivoCsv O caminho para o arquivo de logs.
     * @param recursoInicial O ponto de entrada do ataque (e.g., "/usr/bin/sshd").
     * @param recursoAlvo O alvo final do ataque (e.g., "/var/secrets/key.dat").
     * @return Um Optional<List<String>> contendo a sequência de recursos que formam
     *         o caminho mais curto. Retorna Optional.empty() se não houver caminho.
     * @throws IOException Se ocorrer um erro de leitura do arquivo.
     */
    Optional<List<String>> desafio5_rastrearContaminacao(String caminhoArquivoCsv,
                                                          String recursoInicial, 
                                                          String recursoAlvo) throws IOException;
}
