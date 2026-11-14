# Spécification : Steam Lookup CLI

**Branche** : `001-steam-lookup-cli`

**Créée** : 2026-11-15

**Statut** : Clarifiée

**Description** : Créer un outil en ligne de commande qui consulte Steam et affiche le profil, la bibliothèque et l’activité récente d’un utilisateur.

## Clarifications

### Session 2026-11-15

- Q: Quel type d’identifiant Steam la commande doit-elle accepter ? → A: Un SteamID64 ou une URL/nom personnalisé du profil.
- Q: Combien de jeux récemment joués faut-il afficher dans la section Activité récente ? → A: Les 5 plus récents.
- Q: Si un appel Steam échoue mais que d’autres sections ont été récupérées, que doit afficher la commande ? → A: Afficher les sections disponibles, signaler l’échec et renvoyer un code non nul.
- Q: Que doit afficher la commande lorsqu’une ou plusieurs sections d’un profil Steam sont privées ? → A: Afficher les sections accessibles, signaler les sections privées et terminer avec un code nul.
- Q: Quelles informations faut-il afficher pour la bibliothèque et l’activité récente ? → A: La bibliothèque affiche le nombre de jeux possédés, le nombre lancé au moins une fois, le temps de jeu total et un tableau des 10 jeux les plus joués (rang, nom, heures totales, date de dernière partie). L’activité récente affiche jusqu’aux 5 jeux joués durant les 14 derniers jours et les heures jouées durant cette période.
- Q: Comment signaler une bibliothèque vide lorsque Steam ne permet pas de savoir si elle est privée ? → A: Signaler les données comme absentes ou masquées sans affirmer qu’elles sont privées ; signaler comme privée uniquement une section explicitement déclarée privée par Steam.

## User Scenarios & Testing

### User Story 1 - Consulter un utilisateur Steam (Priority: P1)

En tant qu’utilisateur, je veux fournir un identifiant Steam et consulter son profil, sa bibliothèque de jeux et son activité récente dans le terminal.

**Pourquoi cette priorité** : C’est l’usage principal et la valeur centrale de l’outil.

**Test indépendant** : Fournir l’identifiant d’un compte accessible et vérifier l’affichage des trois sections, des statistiques de bibliothèque et du classement.

**Scénarios d’acceptation** :

1. **Étant donné** un compte accessible, **quand** je fournis son SteamID64 et lance la commande, **alors** le terminal affiche les sections Profil, Bibliothèque et Activité récente.
2. **Étant donné** un compte accessible, **quand** je fournis son nom personnalisé ou l’URL publique de son profil, **alors** les mêmes sections et informations sont affichées.
3. **Étant donné** une recherche réussie, **alors** la bibliothèque affiche le nombre de jeux possédés, le nombre de jeux lancés au moins une fois, le temps de jeu total en heures et un classement des 10 jeux les plus joués avec rang, nom, heures totales et date de dernière partie.
4. **Étant donné** une recherche réussie, **alors** l’activité récente présente au plus les cinq jeux les plus récemment joués durant les 14 derniers jours, ainsi que le temps joué durant ces 14 jours pour chaque jeu.
5. **Étant donné** que Steam fournit ces informations, **alors** la section Profil affiche le nom du compte et l’adresse de son profil.
6. **Étant donné** qu’un jeu récent fourni par Steam ne comporte pas de date de dernière partie, **alors** la date est indiquée comme indisponible et le jeu est classé après les jeux datés.

### User Story 2 - Comprendre les erreurs d’utilisation et de configuration (Priority: P2)

En tant qu’utilisateur, je veux recevoir des instructions utiles lorsque la commande ou sa configuration est incorrecte.

**Pourquoi cette priorité** : Sans instructions claires, l’outil ne peut pas être utilisé ou dépanné correctement.

**Test indépendant** : Lancer la commande sans argument, avec plusieurs arguments, puis sans clé Steam configurée ; vérifier les messages et les codes de sortie.

**Scénarios d’acceptation** :

1. **Étant donné** qu’aucun identifiant n’est fourni, **quand** je lance la commande, **alors** un message indique l’usage attendu et la commande se termine avec un code non nul.
2. **Étant donné** plusieurs arguments, **quand** je lance la commande, **alors** un message indique l’usage attendu et la commande se termine avec un code non nul.
3. **Étant donné** qu’aucune clé Steam n’est configurée, **quand** je lance une recherche, **alors** un message explique où obtenir une clé et comment la configurer dans `.env`, sans effectuer la recherche.

### User Story 3 - Identifier les données privées ou indisponibles (Priority: P3)

En tant qu’utilisateur, je veux distinguer les sections privées, les sections sans données et les erreurs qui empêchent la consultation.

**Pourquoi cette priorité** : Steam ne rend pas toujours toutes les données accessibles ; les résultats doivent décrire fidèlement ce qui a pu être consulté.

**Test indépendant** : Vérifier les résultats pour un profil avec des sections privées, une activité vide et un échec de consultation simulé.

**Scénarios d’acceptation** :

1. **Étant donné** qu’une ou plusieurs sections du profil sont explicitement déclarées privées par Steam, **quand** je lance la commande, **alors** les sections accessibles sont affichées, les sections privées sont signalées et la commande se termine avec un code nul.
2. **Étant donné** un compte accessible sans jeu joué durant les 14 derniers jours, **quand** je lance la commande, **alors** l’activité récente indique clairement qu’aucun jeu n’a été joué durant cette période.
3. **Étant donné** un compte accessible avec moins de 10 jeux dans la bibliothèque, **quand** je lance la commande, **alors** le classement affiche tous les jeux disponibles sans inventer d’entrées.
4. **Étant donné** qu’une section échoue alors que d’autres ont été récupérées, **quand** je lance la commande, **alors** les sections disponibles sont affichées, l’échec est signalé et la commande se termine avec un code non nul.
5. **Étant donné** une erreur réseau ou une réponse inutilisable, **quand** je lance la commande, **alors** un message compréhensible indique l’échec et la commande se termine avec un code non nul.
6. **Étant donné** que Steam ne permet pas de distinguer une bibliothèque vide d’une bibliothèque masquée, **quand** la commande affiche cette section, **alors** elle indique que les données sont absentes ou masquées sans les qualifier de privées.

### Cas limites

- L’identifiant est inconnu, vide ou mal formé.
- Une ou plusieurs sections du profil sont privées alors que d’autres restent accessibles.
- Une section vide ou non renvoyée par Steam ne permet pas toujours de déterminer si ses données sont privées.
- Aucun jeu n’a été joué durant les 14 derniers jours.
- Un jeu récent fourni par Steam ne comporte pas de date de dernière partie.
- La bibliothèque contient moins de 10 jeux possédés ou lancés.
- Steam ne fournit pas le temps de jeu ou la date de dernière partie pour un jeu.
- Une réponse est vide, incomplète ou inutilisable.
- Une requête échoue alors que d’autres sections ont été récupérées.
- La clé est absente ou refusée.
- La clé ne doit jamais apparaître dans les résultats ni dans les messages d’erreur.

## Requirements

### Functional Requirements

- **FR-001**: Le système DOIT accepter exactement un identifiant : un SteamID64, un nom personnalisé ou une URL publique de profil utilisant ce nom.
- **FR-002**: Le système DOIT afficher séparément le profil, la bibliothèque et l’activité récente. La bibliothèque DOIT afficher le nombre de jeux que Steam identifie comme possédés, le nombre de ces jeux lancés au moins une fois, le temps de jeu total en heures et un tableau des 10 jeux les plus joués, avec leur rang, leur nom, leurs heures de jeu totales et la date de leur dernière partie. Les jeux gratuits joués que Steam n’identifie pas comme possédés NE DOIVENT PAS être ajoutés à ces statistiques. L’activité récente DOIT couvrir les 14 derniers jours, afficher au maximum les 5 jeux les plus récemment joués dans cette période et indiquer les heures jouées durant cette période pour chaque jeu.
- **FR-003**: Le système DOIT utiliser le service Steam pour retrouver le compte associé à l’identifiant fourni et consulter les informations demandées.
- **FR-004**: En cas d’argument manquant ou supplémentaire, le système DOIT afficher l’usage attendu et terminer avec un code non nul.
- **FR-005**: Si la clé Steam est absente, le système DOIT indiquer où en obtenir une et comment la configurer dans `.env`, sans lancer la recherche.
- **FR-006**: En cas d’erreur réseau ou de réponse inutilisable, le système DOIT afficher les sections récupérées, signaler les sections en échec avec un message compréhensible et terminer avec un code non nul.
- **FR-007**: Le système DOIT signaler une section comme privée uniquement lorsque Steam l’indique explicitement. Il DOIT afficher les autres sections accessibles et terminer avec un code nul si aucun autre échec ne survient. Si Steam ne permet pas de distinguer une bibliothèque vide de données masquées, le système DOIT signaler que les données sont absentes ou masquées, sans affirmer qu’elles sont privées.
- **FR-008**: Le système NE DOIT PAS exposer la clé Steam dans ses sorties ou messages d’erreur.
- **FR-009**: Une recherche terminée sans erreur fatale DOIT se terminer avec un code nul.
- **FR-010**: La section Profil DOIT afficher le nom du compte et l’adresse de son profil lorsque Steam les fournit.

### Entités clés

- **Compte Steam** : Compte retrouvé à partir du SteamID64, du nom personnalisé ou de l’URL publique personnalisée.
- **Profil** : Informations de présentation accessibles pour le compte.
- **Bibliothèque** : Jeux possédés et lancés, temps de jeu total et date de dernière partie lorsqu’ils sont fournis.
- **Activité récente** : Jeux auxquels le compte a joué durant les 14 derniers jours et temps joué pendant cette période.
- **Clé Steam** : Secret requis pour consulter les informations du compte.

## Success Criteria

### Résultats mesurables

- **SC-001** : Chaque recherche réussie affiche exactement les trois sections attendues : Profil, Bibliothèque et Activité récente ; le nom et l’URL du profil sont affichés lorsqu’ils sont fournis par Steam.
- **SC-002** : Pour toute bibliothèque consultable, le nombre de jeux que Steam identifie comme possédés et lancés, le temps total et le classement affiché correspondent aux données fournies par Steam ; les jeux gratuits joués non identifiés comme possédés sont exclus et le classement contient au maximum 10 jeux.
- **SC-003** : L’activité récente couvre une période de 14 jours, affiche au maximum cinq jeux et distingue clairement le temps joué durant cette période du temps total.
- **SC-004** : Les scénarios testés d’argument invalide, de clé absente, de section explicitement privée, de données vides ou masquées et d’échec de consultation produisent le message et le code de sortie spécifiés ; aucune donnée n’est qualifiée de privée sans indication explicite de Steam.
- **SC-005** : La clé Steam n’apparaît dans aucune sortie produite par les scénarios testés.

## Hypothèses

- Les informations affichées dépendent des données que Steam rend accessibles pour le compte.
- Les heures et dates affichées correspondent aux valeurs fournies par Steam ; les données que Steam ne fournit pas sont signalées comme indisponibles.
- Les durées sont affichées en heures décimales arrondies à une décimale et les dates de dernière partie au format `AAAA-MM-JJ` en UTC.
- Pour l’activité récente, une entrée sans date fournie par Steam est conservée après les entrées datées, avec la date indiquée comme indisponible ; elle ne peut pas être classée devant une entrée datée.
- La clé Steam est configurée localement dans `.env`, qui ne doit pas être ajouté au dépôt.
- La commande consulte un seul compte par exécution et ne modifie pas les données Steam ni ne conserve de bibliothèque locale.
- L’activité récente désigne les jeux joués pendant les 14 jours précédant la consultation ; seuls les cinq plus récents sont affichés.
