# NFC-e Scanner App

Um aplicativo Android completo para leitura e gerenciamento de Notas Fiscais de Consumidor Eletrônicas (NFC-e) com recursos de escaneamento por câmera, armazenamento local e visualização de gráficos de gastos.

## 🚀 Funcionalidades

### ✅ Implementadas e Funcionais
- **Scanner de QR Code/Código de Barras**: Utiliza CameraX e ML Kit para leitura de códigos
- **Validação Completa de Chave**: Valida UF, data, CNPJ, modelo e dígito verificador (Módulo 11)
- **Consulta Online Robusta**: Parser HTML com 4 estratégias de extração (Jsoup)
- **Visualização de Itens**: Extrai e exibe todos os itens da nota fiscal
- **Armazenamento Local**: Banco de dados SQLite com Room para persistência
- **Feedback Visual em Tempo Real**: Mensagens de status durante todo o processo
- **Dashboard com Gráficos**: Análise visual dos gastos mensais usando MPAndroidChart
- **Lista de Notas**: Visualização de todas as notas escaneadas
- **Navegação Intuitiva**: Bottom Navigation com Material Design

### 🏗️ Arquitetura
- **MVVM Pattern**: ViewModel + LiveData + Repository
- **Room Database**: Para armazenamento local eficiente
- **Navigation Component**: Navegação entre telas
- **View Binding**: Binding seguro das views
- **Material Design 3**: Interface moderna e responsiva

## 📱 Telas

1. **Home**: Dashboard com estatísticas e gráficos de gastos
2. **Scanner**: Câmera para escaneamento de QR codes e códigos de barras
3. **Lista de Notas**: Todas as NFC-e salvas
4. **Detalhes da Nota**: Informações completas da nota fiscal com itens
5. **Consulta por URL**: Consulta manual via URL da SEFAZ

## 🛠️ Tecnologias Utilizadas

- **Kotlin**: Linguagem principal
- **Android Jetpack**:
  - Room (Database)
  - Navigation Component
  - ViewModel & LiveData
  - CameraX
- **ML Kit**: Barcode Scanning
- **Jsoup**: Parser HTML robusto para webscraping
- **MPAndroidChart**: Gráficos e visualizações
- **Retrofit**: Cliente HTTP
- **Material Design Components**

## 📋 Pré-requisitos

- Android Studio Arctic Fox ou superior
- SDK mínimo: API 24 (Android 7.0)
- SDK alvo: API 34 (Android 14)
- Permissão de câmera
- Conexão com internet (para consulta online)

## 🔧 Instalação

1. Clone o repositório:
```bash
git clone [URL_DO_REPOSITORIO]
```

2. Abra o projeto no Android Studio

3. Sincronize as dependências:
```bash
./gradlew build
```

4. Execute o aplicativo no dispositivo ou emulador

## 📊 Estrutura do Banco de Dados

### Tabela `nfce_table`
- `id`: ID único da nota
- `chaveAcesso`: Chave de acesso de 44 dígitos
- `numeroNota`: Número da nota fiscal
- `serie`: Série da nota
- `dataEmissao`: Data de emissão
- `cnpjEmitente`: CNPJ do emitente
- `nomeEmitente`: Nome do estabelecimento
- `valorTotal`: Valor total da compra
- `valorTributos`: Valor dos tributos (opcional)
- `status`: Status da nota (ATIVA, CANCELADA, PENDENTE, CONSULTANDO, etc.)

### Tabela `item_nfce_table`
- `id`: ID único do item
- `nfceId`: Referência à nota fiscal
- `codigo`: Código do produto
- `descricao`: Descrição do produto
- `quantidade`: Quantidade comprada
- `valorUnitario`: Preço unitário
- `valorTotal`: Valor total do item
- `ncm`: NCM do produto (opcional)
- `cfop`: CFOP do produto (opcional)

## 🎯 Como Usar

### 1. Escanear uma NFC-e
- Toque no botão "Escanear" ou no FAB
- Aponte a câmera para o QR code da nota
- Aguarde a validação automática
- A nota será automaticamente salva e exibida com todos os itens

### 2. Visualizar Estatísticas
- Na tela inicial, veja o resumo dos gastos
- Gráfico mostra evolução mensal dos gastos
- Cards mostram totais do mês e ano

### 3. Gerenciar Notas
- Acesse "Notas" para ver todas as NFC-e
- Toque em uma nota para ver detalhes completos
- Visualize itens, valores e informações do emitente

### 4. Consultar por URL
- Acesse "Consulta por URL"
- Cole a URL completa da NFC-e
- Acompanhe o progresso da consulta
- Veja os dados extraídos

## ✨ Melhorias Recentes (v1.0)

### Validação Completa de Chave
- ✅ Validação de tamanho (44 dígitos)
- ✅ Validação de formato (apenas números)
- ✅ Validação de UF (códigos válidos)
- ✅ Validação de data (ano/mês)
- ✅ Validação de CNPJ (14 dígitos)
- ✅ Validação de modelo (65 para NFC-e)
- ✅ **Cálculo e validação do dígito verificador (Módulo 11)**

### Parser HTML Robusto
- ✅ 4 estratégias de extração (Classes CSS → IDs → Labels → Tabelas)
- ✅ Extração de itens da nota fiscal
- ✅ Suporte a múltiplos formatos de diferentes estados
- ✅ Logs detalhados para debugging
- ✅ Fallback automático entre estratégias

### Feedback Visual Melhorado
- ✅ Mensagens de status em tempo real
- ✅ Ícones coloridos por status (✓, ⟳, ✗, ⚠, ⋯)
- ✅ Barra de progresso durante consulta
- ✅ Mensagens claras e informativas

## 📚 Documentação

- **[ANALISE_INTEGRACAO_CUPOM_FISCAL.md](ANALISE_INTEGRACAO_CUPOM_FISCAL.md)** - Análise completa do projeto
- **[MELHORIAS_IMPLEMENTADAS.md](MELHORIAS_IMPLEMENTADAS.md)** - Detalhes técnicos das melhorias
- **[GUIA_DE_USO.md](GUIA_DE_USO.md)** - Manual completo para o usuário
- **[COMO_EXECUTAR.md](COMO_EXECUTAR.md)** - Instruções de build e execução
- **[RESUMO_FINAL.md](RESUMO_FINAL.md)** - Visão geral e resultado final

## 🔮 Funcionalidades Futuras

- [ ] Integração com APIs oficiais da Receita Federal
- [ ] Exportação de relatórios (PDF/Excel)
- [ ] Categorização automática de gastos
- [ ] Sincronização em nuvem
- [ ] Notificações de gastos mensais
- [ ] Comparação de preços entre estabelecimentos
- [ ] Backup e restauração de dados
- [ ] Controles da câmera (flash, galeria)

## 🐛 Problemas Conhecidos

- A consulta online depende da disponibilidade dos servidores da SEFAZ
- Alguns estados podem ter formatos de página diferentes
- Controles de flash e galeria ainda não implementados

## 🤝 Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 👨‍💻 Desenvolvedor

**Portfólio desenvolvido por: Carlos Alessandro Semião**

Melhorias e documentação: Kiro AI Assistant

Desenvolvido com ❤️ usando Android + Kotlin

---

**Nota**: Este aplicativo é para fins educacionais e de demonstração. Para uso em produção, certifique-se de implementar as integrações necessárias com as APIs oficiais da Receita Federal.

**Status**: ✅ Pronto para uso - v1.0 (11/02/2026)
