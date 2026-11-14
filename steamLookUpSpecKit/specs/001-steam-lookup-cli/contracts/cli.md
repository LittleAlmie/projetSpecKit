# Contrat CLI : Steam Lookup

## Invocation

Depuis le module Gradle :

```sh
./gradlew bootRun --args="<steamid64|nom-personnalisé|url-de-profil>"
```

La commande accepte exactement un argument positionnel.

## Sortie standard

Une recherche affiche trois sections dans cet ordre :

1. **Profil** : nom affiché et URL de profil lorsqu’ils sont disponibles, ou un statut
   explicite si les données sont privées ou indisponibles.
2. **Bibliothèque** : nombre de jeux possédés, nombre lancé au moins une fois, heures totales,
   puis un tableau des 10 jeux les plus joués (rang, nom, heures totales, date de dernière
   partie).
3. **Activité récente** : au plus cinq jeux joués durant les 14 derniers jours, avec le temps
   joué pendant cette période.

Une date est affichée au format `AAAA-MM-JJ` en UTC. Les durées sont affichées en heures
décimales arrondies à une décimale. Une valeur que Steam ne fournit pas est indiquée comme
indisponible, jamais remplacée par une valeur inventée. Les jeux récents sans date sont
classés après les jeux datés.

## Erreurs et code de sortie

- **0** : recherche terminée ; cela inclut les sections explicitement privées ou absentes/
  masquées.
- **Non nul** : argument manquant ou supplémentaire, identifiant invalide/introuvable, clé
  absente/refusée, réponse inutilisable ou erreur réseau.
- En l’absence de clé, l’aide indique la page Steam `https://steamcommunity.com/dev/apikey`
  et la configuration de `STEAM_API_KEY` dans `.env`.
- En cas d’échec partiel, les sections disponibles restent affichées et les sections en échec
  sont signalées.
- Les erreurs sont émises sur la sortie d’erreur et ne contiennent jamais la clé Steam.
