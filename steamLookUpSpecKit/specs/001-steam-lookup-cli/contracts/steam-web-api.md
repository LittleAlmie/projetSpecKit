# Contrat d’intégration : Web API Steam

## Hôte et authentification

- Hôte public : `https://api.steampowered.com`.
- Les appels utilisent HTTPS et une clé Web API Steam fournie par `STEAM_API_KEY`.
- La clé n’est jamais écrite dans les journaux ni intégrée aux messages d’erreur.
- Le délai de connexion est de 5 secondes et le délai de réponse de 15 secondes ; leur
  expiration est traitée comme une erreur réseau.

## Résolution et appels

1. Pour un SteamID64, utiliser directement l’identifiant.
2. Pour un nom personnalisé ou une URL `steamcommunity.com/id/<nom>`, appeler
   `ISteamUser/ResolveVanityURL/v1/` afin d’obtenir le SteamID64.
3. Une fois l’identifiant établi, lancer indépendamment :
   - `ISteamUser/GetPlayerSummaries/v2/` pour le profil ;
   - `IPlayerService/GetOwnedGames/v1/` pour les jeux possédés et leur temps total ;
   - `IPlayerService/GetRecentlyPlayedGames/v1/` avec `count=0` pour tous les jeux récents
     renvoyés par Steam, puis filtrer les 14 derniers jours et retenir les cinq dernières
     parties.

Les consultations des trois sections peuvent être exécutées en parallèle. Chaque réponse et
chaque erreur est traitée séparément.

## Visibilité

- Utiliser l’état de visibilité du profil uniquement lorsqu’il est présent dans le résumé
  Steam, et ne l’appliquer qu’à la section Profil.
- Une liste de jeux absente ou vide sans indicateur explicite ne prouve pas que le compte est
  privé ; afficher « données absentes ou masquées ».
- Les données privées n’entraînent pas à elles seules un code de sortie non nul.

## Erreurs

- Réponse non-2xx, erreur de résolution, réponse JSON invalide ou erreur réseau : signaler la
  section en échec sans divulguer la clé.
- Si plusieurs sections sont appelées en parallèle, préserver les résultats obtenus pour les
  autres sections.
