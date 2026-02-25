# Projeto Vayziru: Estudo de Viabilidade de Microserviços em Hardware ARM Mobile

Este repositório contém a implementação técnica e a base metodológica para uma pesquisa científica acerca da viabilidade de utilização de dispositivos móveis (Android) como servidores web públicos.

## 🎯 Objetivo da Pesquisa
Avaliar o comportamento, a performance e a estabilidade do framework **Spring Boot** operando em uma arquitetura de **microserviços** dentro de um ambiente de hardware restrito (Smartphone Motorola Edge 30 Neo).

## 🚀 Tecnologias Utilizadas
* **Backend:** Java 17+ / Spring Boot 3.x
* **Infraestrutura:** Android (via Termux/Proot)
* **Rede:** Cloudflare Tunnel (Exposição segura sem IP fixo)
* **Arquitetura:** Microserviços
* **Banco de Dados:** SQLite / H2 (otimizados para baixo consumo de I/O)

## 📊 Metodologia em Desenvolvimento
A pesquisa está sendo estruturada para comparar este modelo com instâncias de VPS (Virtual Private Servers) tradicionais, observando as seguintes métricas:
1. **Consumo de Memória (Heap da JVM):** Em idle e sob carga.
2. **Eficiência Energética:** Consumo em Watts/hora.
3. **Latência e Throughput:** Capacidade de requisições simultâneas.
4. **Estabilidade Térmica:** Impacto do processamento prolongado no hardware ARM.

## 📝 Status do Projeto
- [x] Configuração de Ambiente (Android/Termux)
- [x] Implementação do Gateway de Microserviços
- [ ] Coleta de dados laboratoriais (Em andamento)
- [ ] Formatação e limpeza de variáveis de hardware (Próxima etapa)

---
**Nota:** Este projeto faz parte de uma iniciativa de produção de conhecimento acadêmico (TCC/Artigo Científico). O uso de hardware legado para Edge Computing visa a sustentabilidade digital e a redução de custos para micro-infraestruturas.

**Autor:** Marcos - Estudante de ADS (UCB)
