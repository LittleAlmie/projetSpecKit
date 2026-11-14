# Recherche : Steam Lookup CLI

## Résolution de l’identifiant

- **Décision** : accepter un SteamID64 directement, ou extraire un nom personnalisé d’une
  URL `steamcommunity.com/id/...` ou d’un argument qui est lui-même ce nom. Résoudre ce
  dernier avec `ISteamUser/ResolveVanityURL`.
- **Raison** : la résolution est nécessaire avant les appels qui demandent un SteamID64.
- **Alternatives** : n’accepter que le SteamID64, ce qui contredirait le parcours demandé ;
  rechercher par nom affiché, ce que l’interface documentée de résolution ne garantit pas.
- **Source** : [Référence Steam ISteamUser](https://partner.steamgames.com/doc/webapi/ISteamUser)
  (`ResolveVanityURL`, `GetPlayerSummaries`).

## Consultation du profil et des jeux

- **Décision** : demander les résumés de profil, la bibliothèque possédée et les jeux
  récemment joués après avoir résolu l’identifiant. Demander tous les jeux récents disponibles
  (`count=0`), puis filtrer la fenêtre glissante de 14 jours et trier localement par date de
  dernière partie. Ces trois consultations sont indépendantes et peuvent être lancées en
  parallèle.
- **Raison** : les interfaces Steam les exposent comme des méthodes distinctes ; le projet
  doit aussi exercer la coordination d’appels indépendants prévue par la constitution.
- **Sources** :
  [ISteamUser](https://partner.steamgames.com/doc/webapi/ISteamUser) et
  [IPlayerService](https://partner.steamgames.com/doc/webapi/IPlayerService).

## Visibilité et données manquantes

- **Décision** : signaler le Profil comme privé uniquement quand l’état de visibilité du
  profil fourni par Steam le confirme explicitement. Cet état ne permet pas de qualifier la
  Bibliothèque ou l’Activité récente de privées. Si la réponse de bibliothèque ne permet pas
  de départager une bibliothèque vide d’une bibliothèque masquée, afficher « données absentes
  ou masquées » et ne pas affirmer que le compte est privé.
- **Raison** : `GetOwnedGames` ne renvoie une liste que lorsque les détails des jeux sont
  visibles ; un résultat sans liste ne constitue donc pas une preuve suffisante d’une
  bibliothèque vide ou privée. Le résumé de profil contient `communityvisibilitystate`,
  mais cela ne permet pas de conclure sur la visibilité indépendante des détails de jeux.
- **Source** : [Référence Steam IPlayerService](https://partner.steamgames.com/doc/webapi/IPlayerService)
  et [ISteamUser](https://partner.steamgames.com/doc/webapi/ISteamUser).

## Comptage des jeux

- **Décision** : baser le nombre de jeux possédés et le temps total sur la liste des jeux
  possédés renvoyée par Steam. Ne pas ajouter les jeux gratuits joués que Steam n’identifie
  pas comme possédés à ce décompte.
- **Raison** : Steam exclut les jeux gratuits par défaut de `GetOwnedGames` et fournit un
  paramètre distinct pour les inclure ; les assimiler aux jeux possédés rendrait le décompte
  ambigu.
- **Source** : [Référence Steam IPlayerService](https://partner.steamgames.com/doc/webapi/IPlayerService)
  (`GetOwnedGames`, paramètre `include_played_free_games`).

## Configuration et client HTTP

- **Décision** : utiliser le client HTTP synchrone Spring `RestClient`, configurer l’import
  du fichier local `.env` comme fichier de propriétés facultatif, et injecter la clé depuis
  `STEAM_API_KEY`. Limiter le délai de connexion à 5 secondes et celui de réponse à
  15 secondes ; traiter leur expiration comme une erreur réseau.
- **Raison** : le projet dépend déjà du starter RestClient de Spring Boot ; l’import facultatif
  permet le démarrage sans `.env` afin d’afficher le message d’aide fonctionnel plutôt qu’une
  erreur de configuration précoce.
- **Sources** :
  [Configuration externe Spring Boot](https://docs.spring.io/spring-boot/reference/features/external-config.html)
  et [Clients HTTP Spring](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html).

## Parallélisme et erreurs partielles

- **Décision** : résoudre d’abord le nom personnalisé si nécessaire, puis exécuter les trois
  consultations indépendantes en parallèle. Capturer le résultat de chaque consultation
  séparément afin qu’un échec n’efface pas les sections déjà obtenues.
- **Raison** : cela respecte les dépendances des requêtes tout en conservant les résultats
  partiels et le code de sortie non nul requis en cas d’échec.
- **Limite** : aucun objectif de latence ou politique de nouvelle tentative n’est défini ;
  les délais sont des bornes d’erreur réseau et non un engagement de performance. Ne pas
  ajouter de répétitions automatiques dans cette version.

## Références générales

- [Steamworks Web API Overview](https://partner.steamgames.com/doc/webapi_overview) :
  appels HTTPS et paramètres de clé d’authentification.
- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html) :
  import de fichiers et configuration externe.
- [Spring Framework REST Clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html) :
  usage du client `RestClient`.
