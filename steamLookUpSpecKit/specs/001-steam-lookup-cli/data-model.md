# Modèle de données : Steam Lookup CLI

Les données sont construites pour une seule recherche, affichées puis abandonnées. Aucune
entité n’est persistée.

## SteamUserIdentifier

Représente l’argument fourni à la commande.

- `rawValue` : texte saisi.
- `kind` : SteamID64, nom personnalisé ou URL publique de profil.
- `steamId64` : identifiant résolu, présent après validation/résolution.
- Un identifiant mal formé ou introuvable produit une erreur de recherche.

## SteamProfile

Informations de profil renvoyées par Steam.

- `steamId64` : identifiant canonique du compte.
- `displayName` : nom affiché lorsqu’il est disponible.
- `profileUrl` : URL du profil lorsqu’elle est disponible.
- `visibility` : public, explicitement privé, ou inconnu.
- La valeur de visibilité est utilisée uniquement si elle est explicitement renvoyée par Steam.

## OwnedGame

Un jeu inclus dans la liste des jeux possédés renvoyée par Steam.

- `appId` : identifiant Steam de l’application.
- `name` : nom, lorsqu’il est renvoyé.
- `totalPlaytimeMinutes` : temps de jeu total en minutes.
- `lastPlayedAt` : date de dernière partie, éventuellement absente.
- Un jeu est considéré comme lancé au moins une fois si son temps total est supérieur à zéro.

## GameLibrary

Résumé calculé à partir de la liste des jeux possédés.

- `ownedGameCount` : nombre d’entrées de la liste reçue.
- `playedGameCount` : nombre d’entrées dont le temps total est supérieur à zéro.
- `totalPlaytimeMinutes` : somme des temps de jeu de toutes les entrées.
- `topPlayedGames` : au plus 10 jeux, par temps total décroissant, puis nom croissant.
- Les temps sont convertis en heures décimales arrondies à une décimale à l’affichage.
- Une réponse sans indicateur explicite de visibilité ne permet pas de conclure que la
  bibliothèque est privée.

## RecentGame et RecentActivity

Une activité récente est calculée sur la fenêtre définie dans la spécification.

- `appId` et `name` : identifient le jeu.
- `playtimeLast14DaysMinutes` : temps joué au cours des 14 derniers jours.
- `lastPlayedAt` : date de dernière partie fournie par Steam.
- `games` : au plus cinq jeux, triés par date de dernière partie décroissante ; les entrées
  sans date sont conservées après les entrées datées si elles sont encore retenues.
- La sélection se fait sur la fenêtre glissante des 14 jours précédant la recherche, à partir
  de `lastPlayedAt`.
- Une entrée sans date reçue dans la réponse de jeux récents reste candidate, est classée
  après les entrées datées et affiche une date indisponible.
- Les temps récents sont affichés séparément du temps total de la bibliothèque.

## LookupSection et LookupResult

Chaque partie du résultat possède un statut et éventuellement des données.

- Statuts : `AVAILABLE`, `PRIVATE`, `UNAVAILABLE_OR_HIDDEN`, `FAILED`.
- `PRIVATE` exige une indication explicite de Steam.
- `UNAVAILABLE_OR_HIDDEN` ne doit pas être interprété comme une bibliothèque vide ou privée
  avec certitude.
- `FAILED` représente un échec réseau, une réponse inutilisable ou un rejet de requête.
- `LookupResult` rassemble Profil, Bibliothèque, Activité récente et le résultat global de la
  commande. Toute section `FAILED` rend le code de sortie global non nul ; les autres statuts
  ne le rendent pas non nul à eux seuls.
