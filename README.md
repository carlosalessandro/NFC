
# NFC-e Scanner App

Um aplicativo Android completo para leitura e gerenciamento de Notas Fiscais de Consumidor Eletrônicas (NFC-e) com recursos de escaneamento por câmera, armazenamento local e visualização de gráficos de gastos.

## 🚀 Funcionalidades

### ✅ Implementadas
- **Scanner de QR Code/Código de Barras**: Utiliza CameraX e ML Kit para leitura de códigos
- **Armazenamento Local**: Banco de dados SQLite com Room para persistência
- **Visualização de Notas**: Tela detalhada com informações completas da NFC-e
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
4. **Detalhes da Nota**: Informações completas da nota fiscal

## 🛠️ Tecnologias Utilizadas

- **Kotlin**: Linguagem principal
- **Android Jetpack**:
  - Room (Database)
  - Navigation Component
  - ViewModel & LiveData
  - CameraX
- **ML Kit**: Barcode Scanning
- **MPAndroidChart**: Gráficos e visualizações
- **Retrofit**: Cliente HTTP (preparado para APIs futuras)
- **Material Design Components**

## 📋 Pré-requisitos

- Android Studio Arctic Fox ou superior
- SDK mínimo: API 24 (Android 7.0)
- SDK alvo: API 34 (Android 14)
- Permissão de câmera

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
- `status`: Status da nota (ATIVA, CANCELADA, PENDENTE)

### Tabela `item_nfce_table`
- `id`: ID único do item
- `nfceId`: Referência à nota fiscal
- `codigo`: Código do produto
- `descricao`: Descrição do produto
- `quantidade`: Quantidade comprada
- `valorUnitario`: Preço unitário
- `valorTotal`: Valor total do item

## 🎯 Como Usar

1. **Escanear uma NFC-e**:
   - Toque no botão "Escanear" ou no FAB
   - Aponte a câmera para o QR code da nota
   - A nota será automaticamente salva e exibida

2. **Visualizar Estatísticas**:
   - Na tela inicial, veja o resumo dos gastos
   - Gráfico mostra evolução mensal dos gastos
   - Cards mostram totais do mês e ano

3. **Gerenciar Notas**:
   - Acesse "Notas" para ver todas as NFC-e
   - Toque em uma nota para ver detalhes completos
   - Visualize itens, valores e informações do emitente

## 🔮 Funcionalidades Futuras

- [ ] Integração com APIs oficiais da Receita Federal
- [ ] Exportação de relatórios (PDF/Excel)
- [ ] Categorização automática de gastos
- [ ] Sincronização em nuvem
- [ ] Notificações de gastos mensais
- [ ] Comparação de preços entre estabelecimentos
- [ ] Backup e restauração de dados

## 🐛 Problemas Conhecidos

- A consulta online das NFC-e requer integração com APIs específicas de cada estado
- Algumas funcionalidades do scanner (flash, galeria) estão em desenvolvimento
- Validação completa da chave de acesso pode ser aprimorada

## 🤝 Contribuição

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 👨‍💻 Desenvolvedor

Desenvolvido por: Carlos Alessandro semião

---

**Nota**: Este aplicativo é para fins educacionais e de demonstração. Para uso em produção, certifique-se de implementar as integrações necessárias com as APIs oficiais da Receita Federal.
