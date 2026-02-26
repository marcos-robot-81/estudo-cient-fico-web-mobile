package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.time.LocalDateTime;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import dto.DtoDados;
import javax.sql.DataSource;

@ApplicationScoped
public class ServiceJDBCH2 {
    
    @Inject
    DataSource dataSource;

    // Fila para processamento assíncrono (Buffer)
    private final BlockingQueue<DtoDados> buffer = new LinkedBlockingQueue<>(50000);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @PostConstruct
    void onStart() {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("--- CONEXÃO H2 INICIADA ---");
            System.out.println("Versão do Driver: " + conn.getMetaData().getDriverVersion());
            System.out.println("Versão do Banco: " + conn.getMetaData().getDatabaseProductVersion());
            System.out.println("URL de Conexão: " + conn.getMetaData().getURL());
            System.out.println("---------------------------");
        } catch (SQLException e) {
            System.err.println("Erro ao verificar versão do H2: " + e.getMessage());
        }
        // Inicia a thread de processamento em background
        executor.submit(this::processarFila);
    }

    @PreDestroy
    void onStop() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    private void processarFila() {
        System.out.println("🚀 Thread de processamento de fila iniciada.");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Pega um item ou aguarda. Se tiver muitos, pega em lote.
                DtoDados item = buffer.poll(1, TimeUnit.SECONDS);
                if (item != null) {
                    List<DtoDados> lote = new ArrayList<>();
                    lote.add(item);
                    // Drena até 999 itens adicionais da fila para processar de uma vez
                    buffer.drainTo(lote, 999);
                    salvarLote(lote);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String gerarNomeTabela(String data) {
        if (data == null || data.trim().isEmpty()) {
            return "dados_geral";
        }
        // Substitui caracteres especiais (como / ou -) por _ para ser um nome de tabela válido
        return "dados_" + data.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private String gerarNomeTabelaMetricas(String data) {
        if (data == null || data.trim().isEmpty()) {
            return "metricas_geral";
        }
        // Substitui caracteres especiais (como / ou -) por _ para ser um nome de tabela válido
        return "metricas_" + data.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private void garantirTabela(Connection conn, String nomeTabela) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Cria a tabela com uma coluna primária se ela não existir.
            // Isso garante que a tabela exista para os comandos ALTER.
            stmt.execute("CREATE TABLE IF NOT EXISTS " + nomeTabela + " (id INT AUTO_INCREMENT PRIMARY KEY)");

            // Adiciona cada coluna individualmente se ela não existir.
            // Isso torna o schema retrocompatível, atualizando tabelas antigas.
            stmt.execute("ALTER TABLE " + nomeTabela + " ADD COLUMN IF NOT EXISTS URL TEXT");
            stmt.execute("ALTER TABLE " + nomeTabela + " ADD COLUMN IF NOT EXISTS duracao TEXT");
            stmt.execute("ALTER TABLE " + nomeTabela + " ADD COLUMN IF NOT EXISTS cpu_uso TEXT");
            stmt.execute("ALTER TABLE " + nomeTabela + " ADD COLUMN IF NOT EXISTS cpu_delta TEXT");
            stmt.execute("ALTER TABLE " + nomeTabela + " ADD COLUMN IF NOT EXISTS temperatura TEXT");
            stmt.execute("ALTER TABLE " + nomeTabela + " ADD COLUMN IF NOT EXISTS ram TEXT");
            stmt.execute("ALTER TABLE " + nomeTabela + " ADD COLUMN IF NOT EXISTS ram_delta TEXT");
            stmt.execute("ALTER TABLE " + nomeTabela + " ADD COLUMN IF NOT EXISTS data TEXT");
        }
    }

    private void garantirTabelaMetricas(Connection conn, String nomeTabela) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS " + nomeTabela + " (" +
                         "id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                         "data_operacao TEXT, " +
                         "tempo_ns BIGINT)";
            stmt.execute(sql);
        }
    }

    private void registrarMetrica(long tempoNs, String data) {
        String nomeTabela = gerarNomeTabelaMetricas(data);
        try (Connection conn = dataSource.getConnection()) {
            garantirTabelaMetricas(conn, nomeTabela);
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO " + nomeTabela + " (data_operacao, tempo_ns) VALUES (CURRENT_TIMESTAMP, ?)")) {
                stmt.setLong(1, tempoNs);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<DtoDados> getListDados(String data) {
        List<DtoDados> dados = new ArrayList<>();
        
        if (data == null || data.trim().isEmpty()) {
            System.out.println("🔍 Buscando dados de TODAS as tabelas...");
            // Se não informar data, busca de TODAS as tabelas que começam com "dados_"
            List<String> tabelas = listarTabelas();
            System.out.println("📋 Tabelas encontradas no banco: " + tabelas);
            
            for (String tabela : tabelas) {
                if (tabela.toUpperCase().startsWith("DADOS_")) {
                    List<DtoDados> dadosTabela = lerDadosTabela(tabela);
                    System.out.println("   -> Tabela " + tabela + ": " + dadosTabela.size() + " registros.");
                    dados.addAll(dadosTabela);
                }
            }
        } else {
            // Se informar data, busca apenas daquela data específica
            String nomeTabela = gerarNomeTabela(data);
            dados.addAll(lerDadosTabela(nomeTabela));
        }
        System.out.println("📊 Total de registros retornados para o CSV: " + dados.size());
        return dados;
    }

    private List<DtoDados> lerDadosTabela(String nomeTabela) {
        List<DtoDados> dados = new ArrayList<>();
        String sql = "SELECT URL, duracao, cpu_uso, cpu_delta, temperatura, ram, ram_delta, data FROM " + nomeTabela;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                DtoDados dado = new DtoDados(
                        rs.getString("URL"),
                        rs.getString("duracao"),
                        rs.getString("cpu_uso"),
                        rs.getString("cpu_delta"),
                        rs.getString("temperatura"),
                        rs.getString("ram"),
                        rs.getString("ram_delta"),
                        rs.getString("data")
                );
                dados.add(dado);
            }
        } catch (SQLException e) {
            // Se a tabela não existir (código 42102 no H2), retorna lista vazia sem erro
            if (e.getErrorCode() == 42102) {
                return dados;
            }
            System.err.println("Erro ao ler dados da tabela " + nomeTabela + ": " + e.getMessage());
            e.printStackTrace();
        }
        return dados;
    }

    private void salvarLote(List<DtoDados> lote) {
        // Agrupa os dados por tabela (data)
        Map<String, List<DtoDados>> porTabela = new HashMap<>();
        for (DtoDados d : lote) {
            String dataString = d.data();
            String datePart;
            // Extrai a parte da data (yyyy-MM-dd) da string para nomear a tabela
            if (dataString != null && dataString.length() >= 10) {
                datePart = dataString.substring(0, 10);
            } else {
                datePart = java.time.LocalDate.now().toString();
            }
            String nomeTabela = gerarNomeTabela(datePart);
            porTabela.computeIfAbsent(nomeTabela, k -> new ArrayList<>()).add(d);
        }

        // Processa cada tabela
        for (Map.Entry<String, List<DtoDados>> entry : porTabela.entrySet()) {
            String nomeTabela = entry.getKey();
            List<DtoDados> dadosTabela = entry.getValue();
            
            long inicio = System.nanoTime();
            try (Connection conn = dataSource.getConnection()) {
                garantirTabela(conn, nomeTabela);
                String sql = "INSERT INTO " + nomeTabela + " (URL, duracao, cpu_uso, cpu_delta, temperatura, ram, ram_delta, data) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    for (DtoDados dado : dadosTabela) {
                        stmt.setString(1, dado.url());
                        stmt.setString(2, dado.duracao());
                        stmt.setString(3, dado.cpuUso());
                        stmt.setString(4, dado.cpuDelta());
                        stmt.setString(5, dado.temperatura());
                        stmt.setString(6, dado.ram());
                        stmt.setString(7, dado.ramDelta());
                        // Salva a string de data original, ou a data/hora atual se for nula/vazia
                        String dataParaSalvar = (dado.data() != null && !dado.data().isEmpty()) ? dado.data() : LocalDateTime.now().toString();
                        stmt.setString(8, dataParaSalvar);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
                
                // Registra métrica do lote
                long fim = System.nanoTime();
                // Usa a data do primeiro item para definir onde salvar a métrica
                String dataStringForMetrica;
                String firstData = dadosTabela.get(0).data();
                if (firstData != null && firstData.length() >= 10) {
                    dataStringForMetrica = firstData.substring(0, 10);
                } else {
                    dataStringForMetrica = java.time.LocalDate.now().toString();
                }
                registrarMetrica(fim - inicio, dataStringForMetrica);
                
                System.out.println("✅ Lote de " + dadosTabela.size() + " itens salvo na tabela: " + nomeTabela);

            } catch (SQLException e) {
                System.err.println("❌ Erro ao salvar lote: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public boolean adicionarDado(DtoDados dado) {
        if (dado == null) {
            System.err.println("❌ Erro: Objeto de dados recebido é nulo.");
            return false;
        }

        // Log para monitorar o tamanho da fila
        if (buffer.size() > 1000) {
            System.out.println("⚠️ Alerta: Fila de gravação com " + buffer.size() + " itens pendentes.");
        }
        // Adiciona na fila e retorna imediatamente (Non-blocking)
        return buffer.offer(dado);
    }

    public List<String> listarTabelas() {
        List<String> tabelas = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC'")) {
            while (rs.next()) {
                tabelas.add(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tabelas;
    }

    public List<String> getDatabaseStatus() {
        List<String> status = new ArrayList<>();
        List<String> tabelas = listarTabelas();
        status.add("Total de tabelas encontradas: " + tabelas.size());
        
        for (String tabela : tabelas) {
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tabela)) {
                if (rs.next()) {
                    status.add("Tabela: " + tabela + " | Registros: " + rs.getInt(1));
                }
            } catch (SQLException e) {
                status.add("Tabela: " + tabela + " | Erro ao ler: " + e.getMessage());
            }
        }
        return status;
    }

    public String gerarCsv(String data) {
        List<DtoDados> dados = getListDados(data);
        StringBuilder csv = new StringBuilder();
        csv.append("URL,Duracao,CPU Uso,CPU Delta,Temperatura,RAM,RAM Delta,Data\n");
        
        for (DtoDados dado : dados) {
            csv.append(dado.url() != null ? dado.url() : "").append(",");
            csv.append(dado.duracao() != null ? dado.duracao() : "").append(",");
            csv.append(dado.cpuUso() != null ? dado.cpuUso() : "").append(",");
            csv.append(dado.cpuDelta() != null ? dado.cpuDelta() : "").append(",");
            csv.append(dado.temperatura() != null ? dado.temperatura() : "").append(",");
            csv.append(dado.ram() != null ? dado.ram() : "").append(",");
            csv.append(dado.ramDelta() != null ? dado.ramDelta() : "").append(",");
            csv.append(dado.data()).append("\n");
        }
        return csv.toString();
    }
}
