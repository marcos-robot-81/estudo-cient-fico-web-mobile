package controller;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;
import java.util.List;
import dto.DtoDados;
import service.ServiceJDBCH2;


@Path("/sql")
public class SQLliteController {

    @Inject
    ServiceJDBCH2 service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DtoDados> getLista(@QueryParam("data") String data) {
        return service.getListDados(data);
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response SetDados(DtoDados dado){
        System.out.println("📥 Recebendo dados: " + dado);
        if (service.adicionarDado(dado)) {
            return Response.ok(dado).build(); // Retorna o objeto DtoDados que foi recebido
        }
        System.err.println("Erro ao salvar dados: Falha na operação de inserção.");
        return Response.serverError().entity("Erro ao salvar dados. Verifique o log do servidor.").build();
    }

    @GET
    @Path("/tabelas")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getTabelas() {
        return service.listarTabelas();
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getStatusBanco() {
        return service.getDatabaseStatus();
    }

    @GET
    @Path("/csv")
    @Produces("text/csv")
    public Response downloadCsv(@QueryParam("data") String data) {
        String csv = service.gerarCsv(data);
        String filename = "dados_" + (data != null && !data.isEmpty() ? data : "geral") + ".csv";
        return Response.ok(csv)
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .build();
    }
}
