import { Component, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-servicos',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './servicos.html',
  styleUrl: './servicos.scss'
})
export class Servicos {
  private http = inject(HttpClient);
  
  // IMPORTANTE: Substitua pelo seu código real do Formspree
  private formspreeUrl = 'https://formspree.io/f/xojnapyz';

   men = ""

  formData = {
    nome: '',
    email: '',
    servico: 'Não definido',
    mensagem: ''
  };

  onSubmit(form: NgForm) {

    this.men = ""

    if (this.formData.email == ""){
      this.men = 'E obrigatorio coloca o Email!'
    }
    if(this.formData.nome == ""){
      this.men = 'E obrigatorio coloca o nome!'
    }
    
    if (form.invalid && this.men === "") {
      this.men = 'Por favor, verifique os campos em vermelho.';
    }

    if (form.valid) {
      this.http.post(this.formspreeUrl, this.formData).subscribe({
        next: () => {
          alert('Mensagem enviada com sucesso!');
          form.resetForm({ servico: 'desenvolvimento-web' });
        },
        error: (err) => {
          console.error('Erro ao enviar:', err);
          alert('Ocorreu um erro ao enviar a mensagem. Tente novamente.');
        }
      });
    }
  }

  eviar(){

  }
}