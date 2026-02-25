package br.com.Busca.processos;

public class ProcessoMarca {
    private int numnero;
    private String codigo;
    private String nome;
    private String nomeRazaoSocial;
    private String Pais;
    private String Uf;
    private String clase;
    private String especificao;
    private String estatos;
    private String procurado;

    public void setClase(String clase) {
        this.clase = clase;
    }
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    public void setEspecificao(String especificao) {
        this.especificao = especificao;
    }
    public void setEstatos(String estatos) {
        this.estatos = estatos;
    }
    public void setNomeRazaoSocial(String nomeRazaoSocial) {
        this.nomeRazaoSocial = nomeRazaoSocial;
    }
    public void setProcurado(String procurado) {
        this.procurado = procurado;
    }
    public void setNumnero(int numnero) {
        this.numnero = numnero;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public void setPais(String pais) {
        Pais = pais;
    }
    public void setUf(String uf) {
        Uf = uf;
    }
    public int getNumnero() {
        return numnero;
    }
    public String getCodigo() {
        return codigo;
    }
    public String getNome() {
        return nome;
    }
    public String getNomeRazaoSocial() {
        return nomeRazaoSocial;
    }
    public String getPais() {
        return Pais;
    }
    public String getUf() {
        return Uf;
    }
    public String getClase() {
        return clase;
    }
    public String getEspecificao() {
        return especificao;
    }
    public String getEstatos() {
        return estatos;
    }
    public String getProcurado() {
        return procurado;
    }
}
