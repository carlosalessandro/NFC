# NFC-e Scanner App

Um aplicativo Android completo para leitura e gerenciamento de Notas Fiscais de Consumidor Eletrônicas (NFC-e) com recursos de escaneamento por câmera, armazenamento local e visualização de gráficos de gastos.

**Importante:** Substitua `assets/image_093f48.png` pelo caminho real da sua imagem no seu repositório.

![Captura de Tela do Aplicativo NFC-e Scanner](assets/image_093f48.png)

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
