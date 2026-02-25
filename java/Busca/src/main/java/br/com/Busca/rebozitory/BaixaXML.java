package br.com.Busca.rebozitory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClient;


public class BaixaXML {

    private int rm;
    
    //@Scheduled(cron = "0/1 0 0 ? 1/1 4/7 *")
    private void baixa(){

        try{
        File dados = new File("arquivo/ponto.txt");
        Scanner sca = new Scanner(dados);

            while (sca.hasNextLine()) {
                String linha = sca.nextLine().trim();

                if(linha.contains("marca")){
                    this.rm = Integer.parseInt(linha.substring(linha.indexOf("novo="), (linha.indexOf("novo=")+ 4))) + 1;
                }

                
            }
            sca.close();
        }catch(Exception e){
            System.err.print(e);
        }
        BaixaMarca("https://revistas.inpi.gov.br/txt/RM"+rm+".zip");

    }

    public void BaixaMarca(String url){

        RestClient restClient = RestClient.create(); 

        // baixa o quivo
        byte[] aquivoXml = restClient.get()
            .uri(url)
            .retrieve()
            .body(byte[].class);
        
        try{
            // Cria o diretorio caso não exista
            Files.createDirectories(Path.of("arquivo/marca"));
            // salva o quivo na pasta raiz do projeto
            Files.write(Path.of("arquivo/marca/xmlMarca.zip"),aquivoXml);

            String zip = "arquivo/marca/xmlMarca.zip";

            String diretorioSaida = "arquivo/marca";
            Files.createDirectories(Path.of(diretorioSaida));

            try (ZipFile zipFile = new ZipFile(zip)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();

                // Monta o caminho de destino para cada arquivo
                File arquivoSaida = new File(diretorioSaida, entry.getName());

                if (entry.isDirectory()) {
                    arquivoSaida.mkdirs();
                    continue;
                }
                
                // Garante que o diretório pai exista
                new File(arquivoSaida.getParent()).mkdirs();

                // Inicia o processo de criação e escrita do arquivo descompactado
                try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(arquivoSaida))) {

                    // Obtém o fluxo de dados da entrada ZIP
                    try (BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry))) {

                        // Define um buffer para leitura dos dados
                        byte[] buffer = new byte[1024];
                        int lidos;

                        // Leitura e escrita dos dados até o final do fluxo
                        while ((lidos = bis.read(buffer)) > 0) {
                            bos.write(buffer, 0, lidos);
                        }
                    }
                }
            }
            }
            

        } catch (Exception e){
            e.printStackTrace();
        }





    }
}
