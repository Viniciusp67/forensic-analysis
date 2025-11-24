package br.edu.icev.aed.forense;

import br.edu.icev.aed.forense.util.CSVReader;

import java.io.IOException;
import java.util.*;

/**
 * Implementação da interface AnaliseForenseAvancada.
 * Esta classe resolve os 5 desafios de análise forense usando estruturas de dados apropriadas.
 * 
 * IMPORTANTE: Esta é a classe que deve ser referenciada no README.txt
 * Nome completo: br.edu.icev.aed.forense.SolucaoForenseImpl
 * 
 * @author Seu Nome / Grupo
 * @version 1.0
 */
public class SolucaoForenseImpl implements AnaliseForenseAvancada {

    /**
     * Construtor público sem argumentos (obrigatório pela especificação)
     */
    public SolucaoForenseImpl() {
        // Construtor vazio conforme especificação
    }

    /**
     * Desafio 1: Encontrar Sessões Inválidas usando Stack
     * 
     * Algoritmo:
     * 1. Usa um Map<USER_ID, Stack<SESSION_ID>> para rastrear sessões ativas
     * 2. Para LOGIN: verifica se já há sessão ativa (inválido se sim)
     * 3. Para LOGOUT: verifica se a sessão no topo da pilha corresponde
     * 4. Sessões que restam na pilha ao final são inválidas (sem LOGOUT)
     * 
     * Complexidade: O(n) onde n é o número de linhas no log
     */
    @Override
    public Set<String> desafio1_encontrarSessoesInvalidas(String caminhoArquivoCsv) throws IOException {
        // Conjunto que armazenará as sessões inválidas
        Set<String> sessoesInvalidas = new HashSet<>();
        
        // Map: USER_ID -> Stack de SESSION_IDs
        Map<String, Stack<String>> pilhasPorUsuario = new HashMap<>();
        
        // Ler todos os logs do arquivo
        List<LogEntry> logs = CSVReader.lerTodosOsLogs(caminhoArquivoCsv);
        
        // Processar cada log em ordem cronológica
        for (LogEntry log : logs) {
            String userId = log.getUserId();
            String sessionId = log.getSessionId();
            String actionType = log.getActionType();
            
            // Garantir que o usuário tenha uma pilha
            pilhasPorUsuario.putIfAbsent(userId, new Stack<>());
            Stack<String> pilhaUsuario = pilhasPorUsuario.get(userId);
            
            if ("LOGIN".equals(actionType)) {
                // LOGIN: se já existe sessão ativa, é inválida (LOGIN aninhado)
                if (!pilhaUsuario.isEmpty()) {
                    sessoesInvalidas.add(sessionId);
                }
                // Empilhar a sessão atual de qualquer forma
                pilhaUsuario.push(sessionId);
                
            } else if ("LOGOUT".equals(actionType)) {
                // LOGOUT: verificar se há sessão ativa correspondente
                if (pilhaUsuario.isEmpty()) {
                    // LOGOUT sem LOGIN correspondente
                    sessoesInvalidas.add(sessionId);
                } else {
                    String sessaoTopo = pilhaUsuario.peek();
                    if (sessaoTopo.equals(sessionId)) {
                        // LOGOUT correto
                        pilhaUsuario.pop();
                    } else {
                        // LOGOUT não corresponde ao LOGIN no topo
                        sessoesInvalidas.add(sessionId);
                    }
                }
            }
        }
        
        // Após processar todos os logs, sessões restantes nas pilhas são inválidas
        // (LOGINs sem LOGOUT correspondente)
        for (Stack<String> pilha : pilhasPorUsuario.values()) {
            sessoesInvalidas.addAll(pilha);
        }
        
        return sessoesInvalidas;
    }

    /**
     * Desafio 2: Reconstruir Linha do Tempo usando Queue (FIFO)
     * 
     * Algoritmo:
     * 1. Filtra logs pela sessionId fornecida
     * 2. Adiciona ACTION_TYPEs a uma fila (mantém ordem cronológica)
     * 3. Desenfileira para construir a lista de resultado
     * 
     * Complexidade: O(n) onde n é o número de linhas no log
     */
    @Override
    public List<String> desafio2_reconstruirLinhaDoTempo(String caminhoArquivoCsv, 
                                                          String sessionId) throws IOException {
        // Fila para manter a ordem cronológica (FIFO)
        Queue<String> filaAcoes = new LinkedList<>();
        
        // Ler todos os logs
        List<LogEntry> logs = CSVReader.lerTodosOsLogs(caminhoArquivoCsv);
        
        // Filtrar logs da sessão específica e adicionar à fila
        for (LogEntry log : logs) {
            if (sessionId.equals(log.getSessionId())) {
                filaAcoes.offer(log.getActionType());
            }
        }
        
        // Construir lista de resultado desenfileirando
        List<String> linhaTempo = new ArrayList<>();
        while (!filaAcoes.isEmpty()) {
            linhaTempo.add(filaAcoes.poll());
        }
        
        return linhaTempo;
    }

    /**
     * Desafio 3: Priorizar Alertas usando PriorityQueue
     * 
     * Algoritmo:
     * 1. Cria PriorityQueue com comparador customizado (ordem decrescente de severidade)
     * 2. Adiciona todos os alertas à fila de prioridade
     * 3. Extrai os N primeiros elementos (maiores severidades)
     * 
     * Complexidade: O(n log n) onde n é o número de linhas no log
     */
    @Override
public List<Alerta> desafio3_priorizarAlertas(String caminhoArquivoCsv, int n) throws IOException {
    // Casos triviais
    if (n <= 0) {
        return new ArrayList<>();
    }

    // Wrapper para conseguir ordenar por severidade sem depender de campos da classe Alerta da API
    class AlertaWrapper {
        final Alerta alerta;
        final int severidade;

        AlertaWrapper(Alerta alerta, int severidade) {
            this.alerta = alerta;
            this.severidade = severidade;
        }
    }

    // PriorityQueue em ordem decrescente de severidade
    PriorityQueue<AlertaWrapper> filaPrioridade = new PriorityQueue<>(
        (w1, w2) -> Integer.compare(w2.severidade, w1.severidade)
    );

    // Ler todos os logs
    List<LogEntry> logs = CSVReader.lerTodosOsLogs(caminhoArquivoCsv);

    for (LogEntry log : logs) {
        int severity = log.getSeverityLevel();

        // Criar o Alerta usando o construtor da API:
        // (timestamp, userId, sessionId, actionType, targetResource, severityLevel, bytesTransferred)
        Alerta alerta = new Alerta(
            log.getTimestamp(),
            log.getUserId(),
            log.getSessionId(),
            log.getActionType(),
            log.getTargetResource(),
            severity,
            log.getBytesTransferred()
        );

        filaPrioridade.offer(new AlertaWrapper(alerta, severity));
    }

    // Extrair os N alertas mais severos
    List<Alerta> alertasPrioritarios = new ArrayList<>();
    while (!filaPrioridade.isEmpty() && alertasPrioritarios.size() < n) {
        alertasPrioritarios.add(filaPrioridade.poll().alerta);
    }

    return alertasPrioritarios;
}

    /**
     * Desafio 4: Encontrar Picos de Transferência usando Stack Monotônica
     * 
     * Algoritmo (Next Greater Element):
     * 1. Processa eventos em ordem reversa (do fim para o início)
     * 2. Mantém stack em ordem decrescente de bytes transferidos
     * 3. Para cada evento, encontra o próximo com mais bytes
     * 
     * Complexidade: O(n) onde n é o número de linhas no log
     */
    @Override
    public Map<Long, Long> desafio4_encontrarPicosDeTransferencia(String caminhoArquivoCsv) throws IOException {
        Map<Long, Long> resultado = new HashMap<>();
        
        // Ler todos os logs
        List<LogEntry> logs = CSVReader.lerTodosOsLogs(caminhoArquivoCsv);
        
        // Stack para implementar o algoritmo "Next Greater Element"
        // Armazena pares (timestamp, bytes)
        Stack<LogEntry> stack = new Stack<>();
        
        // Processar em ordem REVERSA (do fim para o início)
        for (int i = logs.size() - 1; i >= 0; i--) {
            LogEntry eventoAtual = logs.get(i);
            
            // Remover da pilha todos os eventos com bytes <= ao atual
            while (!stack.isEmpty() && 
                   stack.peek().getBytesTransferred() <= eventoAtual.getBytesTransferred()) {
                stack.pop();
            }
            
            // Se a pilha não está vazia, o topo é o "próximo maior"
            if (!stack.isEmpty()) {
                resultado.put(eventoAtual.getTimestamp(), stack.peek().getTimestamp());
            }
            
            // Empilhar o evento atual
            stack.push(eventoAtual);
        }
        
        return resultado;
    }

    /**
     * Desafio 5: Rastrear Contaminação usando BFS (Busca em Largura)
     * 
     * Algoritmo:
     * 1. Construir grafo de acessos (recursoA -> recursoB)
     * 2. Executar BFS a partir do recurso inicial
     * 3. Reconstruir caminho usando mapa de predecessores
     * 
     * Complexidade: O(V + E) onde V é o número de recursos e E é o número de arestas
     */
    @Override
    public Optional<List<String>> desafio5_rastrearContaminacao(String caminhoArquivoCsv,
                                                                 String recursoInicial,
                                                                 String recursoAlvo) throws IOException {
        // Ler todos os logs
        List<LogEntry> logs = CSVReader.lerTodosOsLogs(caminhoArquivoCsv);
        
        // FASE 1: Construir o grafo de acessos
        Map<String, List<String>> grafo = construirGrafo(logs);
        
        // Verificar se os recursos existem no grafo
        if (!grafo.containsKey(recursoInicial)) {
            return Optional.empty();
        }
        
        // Caso especial: recurso inicial é igual ao alvo
        if (recursoInicial.equals(recursoAlvo)) {
            return Optional.of(Collections.singletonList(recursoInicial));
        }
        
        // FASE 2: Executar BFS
        Queue<String> fila = new LinkedList<>();
        Set<String> visitados = new HashSet<>();
        Map<String, String> predecessor = new HashMap<>();
        
        fila.offer(recursoInicial);
        visitados.add(recursoInicial);
        predecessor.put(recursoInicial, null);
        
        boolean encontrado = false;
        
        while (!fila.isEmpty() && !encontrado) {
            String atual = fila.poll();
            
            // Verificar se chegamos ao alvo
            if (atual.equals(recursoAlvo)) {
                encontrado = true;
                break;
            }
            
            // Explorar vizinhos
            List<String> vizinhos = grafo.getOrDefault(atual, Collections.emptyList());
            for (String vizinho : vizinhos) {
                if (!visitados.contains(vizinho)) {
                    visitados.add(vizinho);
                    predecessor.put(vizinho, atual);
                    fila.offer(vizinho);
                }
            }
        }
        
        // Se não encontrou caminho, retornar Optional vazio
        if (!encontrado) {
            return Optional.empty();
        }
        
        // FASE 3: Reconstruir o caminho
        List<String> caminho = reconstruirCaminho(predecessor, recursoInicial, recursoAlvo);
        
        return Optional.of(caminho);
    }

    /**
     * Método auxiliar: Constrói o grafo de acessos a partir dos logs
     * Agrupa por sessão e cria arestas entre recursos acessados consecutivamente
     */
    private Map<String, List<String>> construirGrafo(List<LogEntry> logs) {
        Map<String, List<String>> grafo = new HashMap<>();
        
        // Agrupar logs por SESSION_ID
        Map<String, List<LogEntry>> logsPorSessao = new HashMap<>();
        for (LogEntry log : logs) {
            logsPorSessao.putIfAbsent(log.getSessionId(), new ArrayList<>());
            logsPorSessao.get(log.getSessionId()).add(log);
        }
        
        // Para cada sessão, criar arestas entre recursos consecutivos
        for (List<LogEntry> sessao : logsPorSessao.values()) {
            // Ordenar por timestamp (já deve estar ordenado, mas garantir)
            sessao.sort(Comparator.comparingLong(LogEntry::getTimestamp));
            
            // Criar arestas entre recursos consecutivos
            for (int i = 0; i < sessao.size() - 1; i++) {
                String recursoOrigem = sessao.get(i).getTargetResource();
                String recursoDestino = sessao.get(i + 1).getTargetResource();
                
                grafo.putIfAbsent(recursoOrigem, new ArrayList<>());
                // Evitar duplicatas
                if (!grafo.get(recursoOrigem).contains(recursoDestino)) {
                    grafo.get(recursoOrigem).add(recursoDestino);
                }
            }
        }
        
        return grafo;
    }

    /**
     * Método auxiliar: Reconstrói o caminho a partir do mapa de predecessores
     */
    private List<String> reconstruirCaminho(Map<String, String> predecessor, 
                                            String inicio, String fim) {
        LinkedList<String> caminho = new LinkedList<>();
        String atual = fim;
        
        // Reconstruir de trás para frente
        while (atual != null) {
            caminho.addFirst(atual);
            atual = predecessor.get(atual);
        }
        
        return caminho;
    }
}
