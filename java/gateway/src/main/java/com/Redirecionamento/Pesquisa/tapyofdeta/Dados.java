package com.Redirecionamento.Pesquisa.tapyofdeta;

import java.time.LocalDateTime;

public class Dados {
    private String url;
    private String duracao;
    private String cpuUso;
    private String cpuDelta;
    private String ram;
    private String ramDelta;
    private LocalDateTime data;

    public Dados(
        String url,
        String duracao,
        String cpuUso,
        String cpuDelta,
        String ram,
        String ramDelta
    ){
        this.url = url;
        this.duracao = duracao;
        this.cpuUso = cpuUso;
        this.cpuDelta = cpuDelta;
        this.ram = ram;
        this.ramDelta = ramDelta;
        LocalDateTime agora = LocalDateTime.now();
        this.data = (agora);


    }

    public String getUrl() {
        return url;
    }

    public String getDuracao() {
        return duracao;
    }

    public String getCpuUso() {
        return cpuUso;
    }

    public String getCpuDelta() {
        return cpuDelta;
    }

    public String getRam() {
        return ram;
    }

    public String getRamDelta() {
        return ramDelta;
    }

    public LocalDateTime getData() {
        return data;
    }

}
