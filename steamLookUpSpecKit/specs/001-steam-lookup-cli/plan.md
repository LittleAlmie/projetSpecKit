# Implementation Plan: Steam Lookup CLI

**Branche** : `main` | **Date** : 2026-11-15 | **Spécification** : [spec.md](spec.md)

## Summary

Créer une application Spring Boot en ligne de commande qui reçoit un SteamID64, un nom
personnalisé ou une URL de profil. Si nécessaire, elle résout le nom personnalisé, puis
interroge Steam pour le profil, la bibliothèque possédée et les jeux récents. Les trois
consultations indépendantes sont lancées en parallèle ; chaque section conserve son propre
résultat pour permettre un affichage partiel en cas d’échec.

## Technical Context

**Langage/Version** : Java 25 (toolchain Gradle existante)

**Dépendances principales** : Spring Boot 4.1.2, `spring-boot-starter-restclient`,
Jackson 3 fourni par Spring Boot, Lombok déjà configuré dans le projet

**Stockage** : Aucun ; les résultats ne sont pas persistés

**Tests** : JUnit 5 et starter de test RestClient déjà configurés

**Plateforme cible** : CLI sur les environnements compatibles avec la toolchain Java du projet

**Type de projet** : Application Spring Boot en ligne de commande

**Objectif de performance** : Aucun seuil de latence n’est défini ; réduire le temps d’attente
en lançant en parallèle les consultations Steam indépendantes. Les connexions expirent après
5 secondes et les réponses après 15 secondes ; un délai dépassé est traité comme une erreur
réseau.

**Contraintes** : Aucun serveur web ; un seul compte par exécution ; clé Steam hors du code ;
sorties et codes de sortie conformes à `spec.md` ; aucune nouvelle tentative automatique
nécessaire dans cette version.

**Échelle/Périmètre** : Une recherche d’un utilisateur à la fois, sans stockage local des
bibliothèques.

## Constitution Check

### Avant la recherche

- **Simplicité et responsabilités claires** : conforme ; composants limités à l’exécution
  CLI, orchestration, client Steam et formatage.
- **Application CLI sans serveur web** : conforme ; le type d’application sera explicitement
  non-web.
- **Appels API et coordination** : conforme ; résolution du nom avant les appels dépendants,
  puis trois requêtes indépendantes en parallèle.
- **Configuration sûre et erreurs** : conforme ; import facultatif de `.env`, clé exclue des
  journaux et statut explicite par section.
- **Comportement testé** : conforme ; tests automatisés avec réponses HTTP contrôlées, sans
  dépendance au service réel.

**Décision de porte** : PASS. Aucun conflit avec les principes obligatoires n’a été relevé.

## Décisions d’architecture

1. Utiliser `ApplicationRunner` avec `ApplicationArguments` pour valider un seul argument
   positionnel et orchestrer une exécution. Le lanceur récupère le code via
   `SpringApplication.exit` puis termine explicitement le processus avec ce code.
2. Déclarer `spring.main.web-application-type: none` et ne pas ajouter de dépendance serveur.
3. Charger `.env` comme configuration externe facultative au format propriétés et exposer
   `STEAM_API_KEY` à une configuration typée. Le contrôle de présence de la clé est effectué
   après validation des arguments afin de conserver le message d’usage prioritaire.
4. Utiliser `RestClient` pour des appels HTTPS vers `api.steampowered.com`, avec un délai de
   connexion de 5 secondes et un délai de réponse de 15 secondes. Construire les paramètres de
   requête par encodage URI ; ne jamais journaliser les URI contenant la clé.
5. Pour une URL personnalisée, extraire le nom de profil attendu ; pour un nom brut, le
   transmettre directement au résolveur. Un SteamID64 ne nécessite pas de résolution.
6. Après résolution, lancer les requêtes Profil, Bibliothèque et Activité récente sur un
   exécuteur à threads virtuels et collecter chaque résultat séparément. Demander tous les
   résultats récents disponibles (`count=0`), puis filtrer la fenêtre glissante de 14 jours
   et retenir les cinq parties aux dates de dernière partie les plus récentes localement.
   Une entrée sans date est conservée après les entrées datées et sa date est affichée comme
   indisponible.
7. Afficher les sections récupérées et les statuts des autres sections. Le statut privé
   concerne uniquement le Profil si l’état de visibilité fourni par Steam l’indique ; les
   données absentes/masquées ne sont pas une panne réseau.
   Une erreur de requête produit un code non nul même si d’autres sections sont disponibles.
   Seul l’état explicite du profil peut qualifier le Profil de privé ; il ne suffit pas à
   qualifier la Bibliothèque de privée.
8. Convertir les minutes en heures décimales arrondies à une décimale. Afficher les dates de
   dernière partie au format ISO `AAAA-MM-JJ` en UTC afin que la sortie soit stable entre
   environnements ; afficher comme indisponible toute date absente.
9. Pour l’égalité de temps de jeu dans le top 10, trier ensuite par nom de jeu, sans ajouter
   de règle spécifique au produit.
10. Utiliser uniquement la liste que Steam identifie comme jeux possédés pour les statistiques
    de bibliothèque ; ne pas assimiler les jeux gratuits joués à des jeux possédés.

## Project Structure

### Documentation de la fonctionnalité

```text
specs/001-steam-lookup-cli/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── cli.md
│   └── steam-web-api.md
├── checklists/
│   └── requirements.md
└── tasks.md
```

### Code source

```text
steamLookUpSpecKit/
├── .gitignore
├── src/main/java/steamLookUpSpecKit/
│   ├── SteamLookUpSpecKitApplication.java
│   ├── cli/SteamLookupRunner.java
│   ├── cli/SteamLookupFormatter.java
│   ├── service/SteamLookupService.java
│   ├── service/SteamIdentifierResolver.java
│   ├── steam/SteamApiClient.java
│   ├── steam/SteamApiProperties.java
│   └── model/
│       ├── SteamProfile.java
│       ├── OwnedGame.java
│       ├── RecentGame.java
│       ├── GameLibrary.java
│       ├── LookupSection.java
│       └── LookupResult.java
├── src/main/resources/application.yaml
└── src/test/java/steamLookUpSpecKit/
    ├── cli/
    ├── service/
    └── steam/
```

**Choix de structure** : conserver le module Gradle existant et son package racine pour éviter
une refonte hors périmètre. Séparer seulement les responsabilités nécessaires au traitement,
à l’intégration Steam et au rendu CLI. Les tests suivent les mêmes domaines.

## Modèle de données

Voir [data-model.md](data-model.md) pour les champs, états et règles de calcul.

## Contrats

- [Interface CLI et sortie](contracts/cli.md)
- [Dépendance Web API Steam](contracts/steam-web-api.md)

## Validation de la constitution après conception

- La conception n’ajoute ni serveur web ni stockage.
- La clé reste fournie à l’exécution et n’est jamais incluse dans les sorties.
- Les erreurs de chaque section restent observables sans perdre les résultats partiels.
- La limite de visibilité de Steam est respectée : aucune bibliothèque n’est déclarée privée
  sans indication explicite.
- Le parallélisme porte uniquement sur les appels indépendants après résolution de l’identité.

**Décision de porte après conception** : PASS.

## Complexity Tracking

Aucune dérogation à la constitution ni aucun composant supplémentaire non justifié.
