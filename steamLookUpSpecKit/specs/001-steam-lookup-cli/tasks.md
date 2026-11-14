---

description: "Tâches d’implémentation de Steam Lookup CLI"
---

# Tasks: Steam Lookup CLI

**Input**: Documents de conception dans `/specs/001-steam-lookup-cli/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/`

**Tests**: Obligatoires selon la constitution du projet ; les tests d’API utilisent des réponses contrôlées.

**Organization**: Tâches regroupées par user story pour permettre une implémentation et une validation incrémentales.

## Format: `[ID] [P?] [Story] Description`

- **[P]** : tâche parallélisable sans dépendance sur une tâche inachevée et portant sur des fichiers distincts.
- **[Story]** : user story concernée (`US1`, `US2`, `US3`).
- Chaque tâche indique les chemins des fichiers concernés depuis la racine du dépôt.

## Phase 1: Setup

**Purpose**: Configurer le démarrage CLI et l’import facultatif du fichier de configuration local.

- [X] T001 [P] Désactiver le serveur web et importer `.env` comme configuration facultative dans `steamLookUpSpecKit/src/main/resources/application.yaml`.

---

## Phase 2: Foundational

**Purpose**: Mettre en place les composants communs nécessaires avant les user stories.

- [X] T002 [P] Créer `SteamApiProperties` pour injecter `STEAM_API_KEY` sans rendre son absence bloquante au démarrage dans `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/steam/SteamApiProperties.java`.
- [X] T003 [P] Créer les types communs `LookupSection` et `LookupResult` avec les statuts `AVAILABLE`, `PRIVATE`, `UNAVAILABLE_OR_HIDDEN` et `FAILED` dans `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/model/LookupSection.java` et `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/model/LookupResult.java`.
- [X] T004 [P] Ajouter un `ExitCodeGenerator` injectable et faire terminer le point d’entrée avec le code obtenu par `SpringApplication.exit` dans `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/cli/LookupExitCode.java` et `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/SteamLookUpSpecKitApplication.java`.

**Checkpoint**: Configuration, types de résultat communs et mécanisme de code de sortie disponibles.

---

## Phase 3: User Story 1 - Rechercher et afficher profil, bibliothèque et activité (Priority: P1) 🎯 MVP

**Goal**: Afficher les trois sections à partir d’un SteamID64, d’un nom personnalisé ou d’une URL de profil.

**Independent Test**: Simuler les réponses Steam et vérifier les informations du profil, les statistiques et le top 10 de la bibliothèque, ainsi que les cinq jeux les plus récemment joués sur 14 jours.

### Tests for User Story 1

- [X] T005 [P] [US1] Tester la résolution des identifiants, les requêtes et le décodage des réponses de profil, jeux possédés et jeux récents, ainsi que les délais HTTP configurés dans `steamLookUpSpecKit/src/test/java/steamLookUpSpecKit/steam/SteamApiClientTest.java`, `steamLookUpSpecKit/src/test/java/steamLookUpSpecKit/steam/SteamApiClientConfigurationTest.java` et `steamLookUpSpecKit/src/test/java/steamLookUpSpecKit/service/SteamIdentifierResolverTest.java`.
- [X] T006 [P] [US1] Tester les calculs de bibliothèque, le classement top 10, la fenêtre récente de 14 jours, la sélection top 5 et le classement en fin de liste des entrées sans date dans `steamLookUpSpecKit/src/test/java/steamLookUpSpecKit/service/SteamLookupServiceTest.java`.
- [X] T007 [P] [US1] Tester le parcours réussi du runner et les trois sections de sortie dans `steamLookUpSpecKit/src/test/java/steamLookUpSpecKit/cli/SteamLookupRunnerTest.java`.

### Implementation for User Story 1

- [X] T008 [P] [US1] Créer les modèles `SteamProfile`, `OwnedGame`, `RecentGame` et `GameLibrary` dans `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/model/SteamProfile.java`, `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/model/OwnedGame.java`, `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/model/RecentGame.java` et `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/model/GameLibrary.java`.
- [X] T009 [US1] Implémenter le client `RestClient` avec des délais de connexion de 5 secondes et de réponse de 15 secondes, résoudre les noms, appeler les trois méthodes Steam après résolution, récupérer tous les jeux récents avec `count=0`, ne pas inclure les jeux gratuits joués comme possédés et ne jamais journaliser la clé ou les URI qui la contiennent dans `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/steam/SteamApiClient.java`.
- [X] T010 [US1] Implémenter la validation du SteamID64, l’extraction du nom depuis une URL `steamcommunity.com/id/<nom>` et sa résolution via le client dans `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/service/SteamIdentifierResolver.java`.
- [X] T011 [US1] Orchestrer les appels indépendants en parallèle et calculer le nombre de jeux possédés et lancés, le temps total, le top 10 (temps décroissant puis nom croissant) et les cinq parties récentes (dates manquantes après les dates connues) dans `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/service/SteamLookupService.java`.
- [X] T012 [P] [US1] Formater les trois sections, les heures décimales à une décimale et les dates UTC `AAAA-MM-JJ` dans `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/cli/SteamLookupFormatter.java`.
- [X] T013 [US1] Implémenter le parcours réussi avec `ApplicationRunner` et `ApplicationArguments`, afficher le résultat formaté et dériver le code de sortie des statuts des sections dans `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/cli/SteamLookupRunner.java`.

**Checkpoint**: Une recherche réussie fonctionne indépendamment avec des réponses Steam simulées.

---

## Phase 4: User Story 2 - Comprendre les erreurs d’utilisation et de configuration (Priority: P2)

**Goal**: Signaler les arguments incorrects et l’absence de clé avec des instructions compréhensibles et un code non nul.

**Independent Test**: Exécuter les tests du runner sans argument, avec plusieurs arguments et sans clé configurée ; vérifier le message d’usage, l’aide sur la clé et le code de sortie.

### Tests for User Story 2

- [X] T014 [P] [US2] Tester les arguments absents ou supplémentaires, l’absence de `STEAM_API_KEY`, un identifiant invalide ou introuvable, les messages d’aide et les codes de sortie dans `steamLookUpSpecKit/src/test/java/steamLookUpSpecKit/cli/SteamLookupRunnerValidationTest.java`.

### Implementation for User Story 2

- [X] T015 [US2] Valider exactement un argument avant tout appel Steam, contrôler la clé, traiter les identifiants invalides ou introuvables et afficher où obtenir une clé (`https://steamcommunity.com/dev/apikey`) et comment la placer dans `.env` dans `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/cli/SteamLookupRunner.java`.

**Checkpoint**: Les erreurs d’usage et de configuration sont distinguées d’une recherche et terminent avec un code non nul.

---

## Phase 5: User Story 3 - Identifier les données privées ou indisponibles (Priority: P3)

**Goal**: Préserver les données de sections accessibles et distinguer les sections explicitement privées, incertaines ou en échec.

**Independent Test**: Simuler un statut privé explicite, des données absentes ou masquées, ainsi qu’un échec partiel ; vérifier les statuts affichés et le code de sortie.

### Tests for User Story 3

- [X] T016 [P] [US3] Tester le statut privé limité au Profil, les données de bibliothèque absentes ou masquées, les échecs partiels et l’absence de fuite de clé dans `steamLookUpSpecKit/src/test/java/steamLookUpSpecKit/service/SteamLookupServiceFailureTest.java`, `steamLookUpSpecKit/src/test/java/steamLookUpSpecKit/cli/SteamLookupRunnerTest.java`, `steamLookUpSpecKit/src/test/java/steamLookUpSpecKit/cli/SteamLookupFormatterStatusTest.java` et `steamLookUpSpecKit/src/test/java/steamLookUpSpecKit/steam/SteamApiClientTest.java`.

### Implementation for User Story 3

- [X] T017 [US3] Classer le Profil comme privé uniquement sur indication explicite de Steam et préserver les sections disponibles tout en retournant `FAILED` pour les échecs dans `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/service/SteamLookupService.java`.
- [X] T018 [US3] Afficher les statuts `PRIVATE`, `UNAVAILABLE_OR_HIDDEN` et `FAILED` sans divulguer la clé dans `steamLookUpSpecKit/src/main/java/steamLookUpSpecKit/cli/SteamLookupFormatter.java`.

**Checkpoint**: Les résultats partiels restent visibles ; seules les erreurs de requête imposent un code de sortie non nul.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Vérifier l’ensemble de la fonctionnalité et sa documentation d’utilisation.

- [X] T019 Exécuter les tests Gradle et valider les commandes et résultats attendus de `steamLookUpSpecKit/specs/001-steam-lookup-cli/quickstart.md`.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Aucun prérequis.
- **Foundational (Phase 2)**: Dépend de Setup et bloque les user stories.
- **User Stories**: Dépendent de Foundational ; US2 et US3 s’appuient aussi sur le parcours CLI de US1.
- **Polish (Phase 6)**: Après les user stories souhaitées.

### User Story Dependencies

- **US1 (P1)**: Après Foundational ; MVP indépendant.
- **US2 (P2)**: Après US1, car la validation et les messages complètent le runner CLI.
- **US3 (P3)**: Après US1, car la gestion des statuts complète le service et le formatage de résultat.

### Within Each User Story

- Les tests sont écrits avant l’implémentation.
- Les modèles précèdent les services ; le client précède le résolveur et l’orchestration.
- Après les modèles, le service et le formateur sont indépendants ; le runner dépend des deux.
- US2 et US3 peuvent ensuite être développées en parallèle dans leurs fichiers de test et composants distincts.

### Parallel Opportunities

- Setup et tâches fondatrices T002–T004 portent sur des fichiers distincts.
- T005–T007 sont des tests indépendants de US1.
- T011 et T012 peuvent être réalisés en parallèle après T008 ; le formateur ne dépend pas du service.
- T014 peut être préparée indépendamment du code de validation.
- Les tests T016 couvrent des fichiers de test distincts.
- Après US1, les changements de validation US2 et de statuts US3 ciblent des composants distincts.

## Parallel Example: User Story 1

```text
T005 -> steamLookUpSpecKit/src/test/java/steamLookUpSpecKit/steam/SteamApiClientTest.java
T006 -> steamLookUpSpecKit/src/test/java/steamLookUpSpecKit/service/SteamLookupServiceTest.java
T007 -> steamLookUpSpecKit/src/test/java/steamLookUpSpecKit/cli/SteamLookupRunnerTest.java
```

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Compléter Setup et Foundational.
2. Implémenter US1 et ses tests avec des réponses HTTP contrôlées.
3. Valider indépendamment les trois sections et les calculs attendus.
4. Ajouter US2 puis US3 sans perdre la capacité de livraison indépendante de US1.

### Incremental Delivery

1. Setup + Foundational : configuration et mécanismes communs.
2. US1 : recherche réussie, MVP.
3. US2 : diagnostics d’usage et de configuration.
4. US3 : visibilité, résultats partiels et erreurs de sections.
5. Valider la suite Gradle et le quickstart.
