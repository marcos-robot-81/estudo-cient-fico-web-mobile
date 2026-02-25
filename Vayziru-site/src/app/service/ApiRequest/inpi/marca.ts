import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

export interface marca {
  Pais: string;
  Uf: string;
  clase: string;
  codigo: string;
  especificao: string;
  estatos: string;
  nome: string;
  nomeRazaoSocial: string;
  numnero: number;
  procurado: string;


}


@Injectable({
  providedIn: 'root',
}) export class ApiMarca {

  private apiUrl = "/api/busca/marca/nome" ;

  constructor(private http: HttpClient){}


  busca(nome: string){
    const params = new HttpParams().set("nome", nome)
    return this.http.post<marca[]>(this.apiUrl , null, {params} );
  }

}
