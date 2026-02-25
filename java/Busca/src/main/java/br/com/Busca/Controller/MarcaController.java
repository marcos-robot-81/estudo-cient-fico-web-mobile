package br.com.Busca.Controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.Busca.rebozitory.LerXml;
import br.com.Busca.processos.ProcessoMarca;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/api/busca/marca")
public class MarcaController {
    
    @PostMapping("/nome")
    @ResponseBody
    public List<ProcessoMarca> busca(@RequestParam String nome){

        LerXml ler = new LerXml();
        
        return ler.lerMarcaXml(nome);


    }
}
