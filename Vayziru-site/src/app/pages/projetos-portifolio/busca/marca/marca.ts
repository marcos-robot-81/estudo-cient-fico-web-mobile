import { Component, inject } from '@angular/core';
import { ApiMarca, marca } from '../../../../service/ApiRequest/inpi/marca';
import {FormGroup, FormControl} from '@angular/forms';
import {ReactiveFormsModule} from '@angular/forms';


@Component({
  selector: 'pmarca',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './marca.html',
  styleUrl: './marca.scss'
})
export class pmarca {
    private apiMarca = inject(ApiMarca);
    b = 0 ;
    e = false;
    loading = false;
    errorMessage = '';
    resultados: marca[] = [];

    formt = new FormGroup({
      nome: new FormControl("")
    })

    busca() {
      this.loading = true;
      this.b = 0;
      this.errorMessage = '';
      this.apiMarca.busca(this.formt.value.nome || '').subscribe({
        next: (valor) => {
          this.resultados = valor;
          this.loading = false;
          this.b = 1;
          console.log(valor);
        },
        error: (err) => {
          console.error(err);
          this.loading = false;
          this.errorMessage = 'Erro de conexão (CORS): Verifique se o backend está rodando na porta 8001.';
        }
      });

    }


}
