# ⛏️ Minecraft Clicker

Jogo *Cookie Clicker* temático de Minecraft para Android. O jogador clica no bloco, quebra minérios sorteados por sorte, ganha moedas, compra upgrades e disputa um ranking global. Um perfil **Admin** gerencia o catálogo de minérios em tempo real.

Desenvolvido em **Kotlin + Jetpack Compose**, com arquitetura **MVVM**, persistência local em **Room** e nuvem com **Firebase (Firestore)**.

---
## 📹 Vídeo de demonstração

> 🎬 **[Assistir à demonstração no Google Drive](https://drive.google.com/file/d/1TeNI_4i-PJXeUYqp75NUuV7PoPhjN2Kk/view?usp=sharing)**
>
> Também disponível no repositório: [`video_demonstrativo.webm`](documentacao/video_demonstrativo.webm)

---

## 🎮 Visão geral

| Perfil | O que pode fazer |
|---|---|
| **Jogador** | Minerar blocos, comprar upgrades, ver o ranking global e acompanhar suas estatísticas |
| **Admin** | Tudo o que o jogador faz + aba **Admin** com CRUD completo do catálogo de minérios |

Fluxo resumido:

```
Login / Criar conta ──► Minerar ──► Ganha moedas ──► Compra upgrades
                                          │
                                          ▼
                         Ranking global (Firestore) e Estatísticas (Room)

Admin ──► Catálogo de minérios (Firestore) ──► sincroniza com o sorteio do Clicker
```

---

## 📱 Telas

O app possui **7 telas**: Login, Criar conta, Minerar (Clicker), Upgrades, Ranking, Estatísticas e Admin. A navegação principal é feita por uma **Bottom Navigation Bar**; a aba **Admin só aparece para usuários com perfil Admin**.

### Login
Entrada com usuário e senha, validada no `AuthViewModel`, que também define o perfil (Jogador ou Admin). Há também a opção de criar conta.

<p align="center">
  <img src="documentacao\login.png" width="260" alt="Estatísticas - resumo">
  &nbsp;&nbsp;
  <img src="documentacao\cirar_conta.png" width="260" alt="Estatísticas - minérios minerados">
</p>

### Minerar (Clicker)
Mostra moedas, blocos quebrados, o minério atual com sua barra de **resistência** (vida do bloco) e os atributos ativos: **Dano**, **Sorte** e **Fortuna**. Cada clique causa dano ao bloco; ao zerar a vida, o bloco é quebrado, as moedas são creditadas e um novo minério é sorteado.

<p align="center"><img src="documentacao\minerar.png" width="260" alt="Tela do clicker"></p>

### Upgrades
Três upgrades com custo crescente e níveis: **Picareta** (dano por clique), **Sorte** (chance de minérios raros) e **Fortuna** (multiplicador de moedas). Cada cartão mostra o efeito atual, o próximo nível, o custo e desabilita a compra quando não há moedas suficientes.

<p align="center"><img src="documentacao\upgrades.png" width="260" alt="Tela de upgrades"></p>

### Ranking global
Ranking de todos os jogadores, lido do **Firestore** em tempo real. Exibe o top 10, destaca o usuário atual e mostra sua posição.

<p align="center"><img src="documentacao\ranking.png" width="260" alt="Tela de ranking"></p>

### Estatísticas
Dados lidos do **Room** via `Flow`: blocos quebrados, dinheiro acumulado, minério mais quebrado, upgrades comprados e a lista de minérios minerados (ordenada do mais ao menos quebrado).

<p align="center">
  <img src="documentacao\estatisticas.png" width="260" alt="Estatísticas - resumo">
  &nbsp;&nbsp;
  <img src="documentacao\estatisticas_minerios.png" width="260" alt="Estatísticas - minérios minerados">
</p>

### Admin — Catálogo de minérios
Disponível apenas para o perfil Admin. Lista o catálogo do **Firestore** com filtros por **raridade** (Todas, Comum, Incomum, Raro...) e ordenação por **nome** ou **valor**. Cada item pode ser editado ou excluído, e o botão **+** cria um novo minério.

<p align="center"><img src="documentacao\catalogo.png" width="260" alt="Catálogo de minérios"></p>

### Admin — Criar e editar minério
Formulário com nome, valor (dinheiro), chance (%), vida do bloco, raridade (Comum, Incomum, Raro, Épico, Lendário) e ícone.

<p align="center">
  <img src="documentacao\adicionar_minerio.png" width="260" alt="Novo minério">
  &nbsp;&nbsp;
  <img src="documentacao\editar_minerio.png" width="260" alt="Editar minério">
</p>

---

## ⚙️ Mecânica do jogo

### Upgrades

| Upgrade | Níveis | Efeito por nível | Custo para o próximo nível |
|---|---|---|---|
| ⛏️ **Picareta** | Madeira → Pedra → Metal → Diamante → Netherita | Dano por clique: 1 / 3 / 10 / 35 / 100 | 50 / 200 / 800 / 3000 |
| 🍀 **Sorte** | Básica → Sorte I → II → III → IV | Bônus de chance de raros: 0% / +5% / +10% / +15% / +25% | 100 / 350 / 1000 / 2500 |
| 💎 **Fortuna** | Fortuna I → II → III | Multiplicador de moedas por bloco: 1x / 2x / 3x | 500 / 2000 |

### Sorteio ponderado de minérios

O próximo minério é sorteado por **peso**. Cada minério tem uma `chance`; a **Sorte** multiplica o peso dos minérios de raridade **Raro ou superior** por `1 + bônus`, sem alterar o peso dos comuns:

```kotlin
fun oreWeight(ore: Ore, luck: Double): Double =
    ore.chance * if (ore.rarity >= Rarity.RARE) 1.0 + luck else 1.0
```

Minérios com chance ≤ 0, vida ≤ 0 ou valor negativo nunca são sorteados.

### Quebra de bloco e recompensa

1. Cada clique reduz a vida do bloco em `dano` (definido pela Picareta).
2. Ao chegar a 0, o jogador recebe `valor do minério × Fortuna` moedas.
3. O drop é gravado no histórico (usado nas estatísticas) e um novo minério é sorteado.
4. Tudo ocorre dentro de uma **transação Room**, garantindo consistência.

### Catálogo padrão (plano B)

O Clicker usa o catálogo do **Firestore**. Se estiver vazio ou offline, usa a lista padrão:

| Minério | Valor | Raridade | Chance | Vida |
|---|---:|---|---:|---:|
| Pedregulho | 1 | Comum | 50% | 5 |
| Carvão | 3 | Comum | 25% | 8 |
| Ferro | 8 | Incomum | 12% | 12 |
| Ouro | 20 | Raro | 6% | 16 |
| Redstone | 15 | Incomum | 4% | 14 |
| Lápis-lazúli | 25 | Raro | 2% | 18 |
| Diamante | 100 | Épico | 0,8% | 25 |
| Esmeralda | 250 | Lendário | 0,2% | 30 |

---

## 🏗️ Arquitetura

O projeto segue **MVVM** com fluxo de dados unidirecional: a UI observa um `UiState` (`StateFlow`) exposto pelo ViewModel, que consome os Repositories.

```
┌──────────────┐   eventos    ┌───────────────┐   chamadas   ┌──────────────┐
│  UI (Compose)│ ───────────► │   ViewModel   │ ───────────► │  Repository  │
│   Screens    │ ◄─────────── │ + UiState     │ ◄─────────── │ (try/catch)  │
└──────────────┘   StateFlow  └───────────────┘   Result/Flow└──────┬───────┘
                                                                     │
                                                  ┌──────────────────┴─────────┐
                                                  ▼                            ▼
                                        ┌──────────────────┐        ┌────────────────────┐
                                        │ data.local (Room)│        │ data.remote        │
                                        │ Entities + DAOs  │        │ (Firebase Firestore)│
                                        └──────────────────┘        └────────────────────┘
```

### Estrutura de pacotes

```
app/src/main/java/com/example/minecraftclicker/
├── MainActivity.kt
├── MinecraftClickerApplication.kt     # cria banco e repositories
├── model/                             # Ore, Rarity, Player, Role, Tab, UpgradeType, MiningRules...
├── ui/                                # telas Compose, navegação e tema
│   ├── App.kt                         # NavHost + Bottom Navigation
│   ├── LoginScreen.kt / SignUpScreen.kt
│   ├── ClickerScreen.kt
│   ├── UpgradesScreen.kt
│   ├── RankingScreen.kt
│   ├── AdminScreen.kt
│   ├── stats/                         # StatsScreen + StatsUiState
│   └── theme/
├── viewmodel/                         # ViewModels + AppViewModelFactory
├── data/
│   ├── local/                         # Room: entidades, DAOs, AppDatabase
│   ├── repository/                    # Repositories + RepositoryResult (safeCall)
│   └── remote/                        # Firestore: coleções e data source
```

### ViewModels e responsabilidades

| ViewModel | UiState | Tela | Fontes de dados |
|---|---|---|---|
| `AuthViewModel` | `AuthUiState` | Login / Criar conta | Room (`UserRepository`) |
| `GameViewModel` | `GameUiState` | Minerar | Room (`JogoRepository`, `UpgradeRepository`) + Firestore (`MinerioRepository`) |
| `UpgradesViewModel` | `UpgradesUiState` | Upgrades | Room (`UpgradeRepository`, `JogoRepository`) |
| `RankingViewModel` | `RankingUiState` | Ranking | Firestore (`RankingRepository`) + Room |
| `StatsViewModel` | `StatsUiState` | Estatísticas | Room (`StatsRepository`) |
| `AdminViewModel` | `AdminUiState` | Admin | Firestore (`OreRepository`) |

Todos são criados pela **`AppViewModelFactory`**, que injeta os repositories a partir da `MinecraftClickerApplication`.

### Tratamento de erros

Os Repositories usam `try-catch` por meio do helper `safeCall` (operações únicas) e `withErrorHandling` (Flows), convertendo falhas de Room e Firestore em `Result.failure`. O ViewModel traduz o resultado em um campo de erro do **UiState**, exibido na tela.

### Controle de perfil

O `AuthViewModel` expõe `visibleTabs`, que filtra `Tab.entries` removendo as abas `adminOnly` quando o usuário não é Admin. A rota Admin também fica oculta para jogadores.

---

## 🗄️ Banco de dados

### Room (local) — `minecraft_clicker.db`

| Entidade | Tabela | Campos principais |
|---|---|---|
| `User` | `users` | `id`, `name` (único), `password`, `role` |
| `ProgressoJogador` | `progresso_jogador` | `jogadorId`, `dinheiro`, `dinheiroTotalGanho`, `blocosQuebrados`, `cliques`, minério atual (`minerioId`, `minerioNome`, `minerioValor`), `vidaAtual`, `vidaMaxima` |
| `UpgradeEntity` | `upgrades` | `id`, `playerId`, `type` (Picareta/Sorte/Fortuna), `level` — índice único (`playerId`, `type`) |
| `HistoricoMineracao` | `historico_mineracao` | `id`, `jogadorId`, `minerioId`, `minerioNome`, `quantidade` |

- DAOs expõem **`Flow`** para leitura reativa (progresso, upgrades, estatísticas).
- O banco está na **versão 5**, com migrations `3→4` e `4→5`.
- Contas iniciais (`admin` e `jogador`) são criadas por um *callback* ao abrir o banco.

### Firebase Firestore (nuvem)

| Coleção | Uso | Quem escreve |
|---|---|---|
| `ores` | Catálogo de minérios (CRUD completo) | Admin |
| `ranking` | Pontuação (`score` = dinheiro total ganho), `playerName`, `localPlayerId`, `ownerUid`, `updatedAt` | Cada jogador, ao ganhar moedas |

O ranking usa **Firebase Authentication anônima** para obter um `uid`; o documento é identificado por `{uid}_{idLocalDoJogador}`.

---

## 🧰 Tecnologias

| Categoria | Tecnologia |
|---|---|
| Linguagem | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navegação | Navigation Compose |
| Arquitetura | MVVM, `ViewModel`, `StateFlow`, Coroutines, Flow |
| Banco local | Room (via KSP) |
| Nuvem | Firebase Firestore e Firebase Auth (anônimo) |
| Build | Gradle (Kotlin DSL) + Version Catalog |
| SDK | `minSdk` 24 |

---

## 🚀 Como executar

### Pré-requisitos

- Android Studio atualizado
- JDK 11 ou superior
- Emulador ou celular Android (API 24+)
- Um projeto no [Firebase Console](https://console.firebase.google.com/) com **Firestore** e **Authentication → Anônimo** habilitados

### Passo a passo

1. Clone o repositório:
   ```bash
   git clone <URL-DO-REPOSITORIO>
   cd Cookie_Clicker_Minecraft
   ```
2. Abra a pasta no **Android Studio** e aguarde o *Gradle Sync*.
3. Configure o Firebase:
   - Registre um app Android com o pacote `com.example.minecraftclicker`.
   - Baixe o `google-services.json` e coloque em `app/google-services.json`.
   - No Firestore, crie as coleções `ores` e `ranking` (ou deixe o app criar ao usar).
4. Rode o app (**Run ▶**) em um emulador ou dispositivo.
5. Como administrador, abra a aba **Admin** e importe/cadastre os minérios do catálogo.

---

## 🔑 Contas de teste

Criadas automaticamente na primeira execução:

| Usuário | Senha | Perfil |
|---|---|---|
| `admin` | `admin123` | Admin |
| `jogador` | `1234` | Jogador |

Novas contas podem ser criadas pela tela **Criar conta** (perfil Jogador).

---

### Uso de funções lambda

| Função | Onde é usada |
|---|---|
| `filter` | `GameViewModel` (minérios válidos), `AdminViewModel` (filtro de raridade), `AuthViewModel` (abas visíveis) |
| `sortedBy` | `AdminViewModel` (ordenar por nome ou valor) |
| `sortedByDescending` | `RankingViewModel` / `RankingScreen` (ranking), `StatsViewModel` (minérios minerados) |
| `take` | `RankingScreen` (top 10), `GameViewModel` (feed dos últimos drops) |
| `map` | `UpgradesViewModel`, `StatsViewModel`, `RankingRepository` |
| `groupBy` | `StatsViewModel` (agrupa histórico por minério) |
| `maxByOrNull` | `StatsViewModel` (minério mais quebrado) |
| `firstOrNull` / `any` | `AuthViewModel` (validação de credenciais e nome em uso) |

---

## 👥 Divisão de tarefas

| Membro | Responsabilidade | Principais arquivos |
|---|---|---|
| **Membro 1** | Base do projeto, Login e Navegação | `AuthViewModel`, `LoginScreen`, `SignUpScreen`, `App.kt`, `UserRepository`, `AppViewModelFactory` |
| **Membro 2** | Clicker e sorteio de minérios | `GameViewModel`, `ClickerScreen`, `JogoRepository`, `ProgressoJogador`, `MiningRules` |
| **Membro 3** | Upgrades | `UpgradesViewModel`, `UpgradesScreen`, `UpgradeRepository`, `UpgradeEntity` |
| **Membro 4** | Ranking e Estatísticas | `RankingViewModel`, `StatsViewModel`, `RankingRepository`, `StatsRepository` |
| **Membro 5** | Admin, catálogo e documentação | `AdminViewModel`, `AdminScreen`, `OreRepository`, `OreRemoteDataSource`, README |

Fluxo de trabalho: **branch por feature**, **Pull Request revisado por outro membro** e commits individuais e frequentes.

---

## 🧑‍🤝‍🧑 Equipe

<!-- Preencha com os nomes e links do GitHub -->

| Membro | Nome | GitHub |
|---|---|---|
| 1 | Bruno Viotto dos Santos | [@BrunoViotto18](https://github.com/BrunoViotto18) |
| 2 | Gabriel Arnold dos Santos | [@Gabriel-Arnold](https://github.com/Gabriel-Arnold) |
| 3 | Pedro Henrique Pagno Geraldo | [@PedroHPGeraldo](https://github.com/PedroHPGeraldo) |
| 4 | Michelle Andrea Bornes Noriega | [@MichelleBornes](https://github.com/MichelleBornes) |
| 5 | Gabriela Pedroso dos Santos Pontes | [@4rt3mis4D4](https://github.com/4rt3mis4D4) |
