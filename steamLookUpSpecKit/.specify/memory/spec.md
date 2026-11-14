# Spécification : Steam Lookup CLI

**Branche** : non définie
**Créée** : 2026-11-14
**Statut** : Brouillon
**Description** : Créer un outil CLI qui affiche le profil, la bibliothèque et l’activité récente d’un utilisateur Steam.

## Clarifications

### Session 2026-11-15

- Q: Quel type d’identifiant Steam la commande doit-elle accepter ? → A: Un SteamID64 ou une URL/nom personnalisé du profil.
- Q: Combien de jeux récemment joués faut-il afficher dans la section Activité récente ? → A: Les 5 plus récents.
- Q: Si un appel Steam échoue mais que d’autres sections ont été récupérées, que doit afficher la commande ? → A: Afficher les sections disponibles, signaler l’échec et renvoyer un code non nul.
- Q: Que doit afficher la commande lorsqu’une ou plusieurs sections d’un profil Steam sont privées ? → A: Afficher les sections accessibles, signaler les sections privées et terminer avec un code nul.
- Q: Quelles informations faut-il afficher pour la bibliothèque et l’activité récente ? → A: La bibliothèque affiche le nombre de jeux possédés, le nombre lancé au moins une fois, le temps de jeu total et un tableau des 10 jeux les plus joués (rang, nom, heures totales, date de dernière partie). L’activité récente affiche jusqu’aux 5 jeux joués durant les 14 derniers jours et les heures jouées durant cette période.
- Q: Comment signaler une bibliothèque vide lorsque Steam ne permet pas de savoir si elle est privée ? → A: Signaler les données comme absentes ou masquées sans affirmer qu’elles sont privées ; signaler comme privée uniquement une section explicitement déclarée privée par Steam.
- Q: Faut-il inclure les jeux gratuits joués que Steam ne retourne pas comme jeux possédés ? → A: Non, les statistiques de bibliothèque utilisent uniquement les jeux identifiés par Steam comme possédés.
- Q: Comment afficher un jeu récent sans date de dernière partie ? → A: Afficher la date comme indisponible et classer le jeu après les entrées datées.

## Scénarios utilisateur et tests

### Histoire 1 — Consulter un utilisateur Steam (Priorité : P1)

En tant qu’utilisateur, je veux fournir un identifiant Steam et consulter son profil, sa bibliothèque de jeux et son activité récente dans le terminal.

**Pourquoi cette priorité** : C’est l’usage principal et la valeur centrale de l’outil.

**Test indépendant** : Fournir l’identifiant d’un compte accessible et vérifier l’affichage des trois sections avec les données disponibles.

**Scénarios d’acceptation** :

1. **Étant donné** un identifiant valide et un compte accessible, **quand** je lance la commande, **alors** le terminal affiche les sections Profil, Bibliothèque et Activité récente, avec les statistiques de bibliothèque et le classement convenus.
2. **Étant donné** un identifiant qui ne correspond pas à un compte trouvé, **quand** je lance la commande, **alors** un message indique que le compte n’a pas été trouvé.
3. **Étant donné** que Steam fournit ces informations, **alors** la section Profil affiche le nom du compte et l’adresse de son profil.
4. **Étant donné** qu’un jeu récent fourni par Steam ne comporte pas de date de dernière partie, **alors** la date est indiquée comme indisponible et le jeu est classé après les jeux datés.

### Histoire 2 — Comprendre les erreurs d’utilisation et de configuration (Priorité : P2)

En tant qu’utilisateur, je veux recevoir un message utile lorsque la commande ou sa configuration est incorrecte.
**Pourquoi cette priorité** : Sans instructions claires, l’outil ne peut pas être utilisé ou dépanné correctement.

**Test indépendant** : Lancer la commande sans argument, avec plusieurs arguments, puis sans clé API configurée, et vérifier les messages ainsi que le résultat de la commande.

**Scénarios d’acceptation** :

1. **Étant donné** qu’aucun identifiant n’est fourni, **quand** je lance la commande, **alors** un message indique l’usage attendu et la commande se termine avec un code non nul.
2. **Étant donné** plusieurs arguments, **quand** je lance la commande, **alors** un message indique l’usage attendu et la commande se termine avec un code non nul.
3. **Étant donné** qu’aucune clé API Steam n’est configurée, **quand** je lance une recherche, **alors** un message explique où obtenir une clé et comment la définir dans `.env`, sans effectuer la recherche.

### Histoire 3 — Identifier les données privées ou indisponibles (Priorité : P3)

En tant qu’utilisateur, je veux savoir quand le profil est privé, qu’une section ne contient aucune donnée ou qu’une erreur réseau empêche la recherche.

**Pourquoi cette priorité** : Les données Steam ne sont pas toujours accessibles ; l’outil doit éviter les résultats trompeurs.

**Test indépendant** : Vérifier les résultats pour un profil privé, une bibliothèque vide et une erreur réseau simulée.

**Scénarios d’acceptation** :

1. **Étant donné** qu’une ou plusieurs sections du profil sont explicitement déclarées privées par Steam, **quand** je lance la commande, **alors** les sections accessibles sont affichées, les sections privées sont signalées et la commande se termine avec un code nul.
2. **Étant donné** un compte accessible sans jeu joué durant les 14 derniers jours, **quand** je lance la commande, **alors** l’activité récente indique clairement qu’aucun jeu n’a été joué durant cette période.
3. **Étant donné** une erreur réseau, **quand** je lance la commande, **alors** un message compréhensible décrit l’échec et la commande se termine avec un code non nul.
4. **Étant donné** qu’un appel échoue alors que d’autres sections ont été récupérées, **quand** je lance la commande, **alors** les sections disponibles sont affichées, l’échec est signalé et la commande se termine avec un code non nul.
5. **Étant donné** que Steam ne permet pas de distinguer une bibliothèque vide d’une bibliothèque masquée, **quand** la commande affiche cette section, **alors** elle indique que les données sont absentes ou masquées sans les qualifier de privées.

### Cas limites

- L’identifiant est vide, inconnu ou mal formé.
- Une ou plusieurs sections du profil peuvent être privées alors que d’autres restent accessibles.
- Une section vide ou non renvoyée par Steam ne permet pas toujours de déterminer si ses données sont privées.
- Aucun jeu n’a été joué durant les 14 derniers jours.
- Un jeu récent fourni par Steam ne comporte pas de date de dernière partie.
- La bibliothèque contient moins de 10 jeux possédés ou lancés.
- La réponse distante est vide, incomplète ou inexploitable.
- La requête échoue ou n’aboutit pas à cause du réseau.
- Un appel de section échoue alors que les autres sections ont été récupérées.
- La clé est absente ou refusée.
- La clé ne doit jamais apparaître dans les résultats ni dans les messages d’erreur.

## Exigences

### Exigences fonctionnelles

- **FR-001** : Le système DOIT accepter exactement un identifiant Steam en argument : un SteamID64, un nom personnalisé ou une URL publique du profil.
- **FR-002** : Le système DOIT afficher séparément le profil, la bibliothèque et l’activité récente. La bibliothèque DOIT afficher le nombre de jeux que Steam identifie comme possédés, le nombre de ces jeux lancés au moins une fois, le temps de jeu total en heures et un tableau des 10 jeux les plus joués, avec leur rang, leur nom, leurs heures de jeu totales et la date de leur dernière partie. Les jeux gratuits joués que Steam n’identifie pas comme possédés NE DOIVENT PAS être ajoutés à ces statistiques. L’activité récente DOIT couvrir les 14 derniers jours, afficher au maximum les 5 jeux les plus récemment joués dans cette période et indiquer les heures jouées durant cette période pour chaque jeu.
- **FR-003** : Le système DOIT résoudre un nom personnalisé ou une URL de profil en compte Steam, puis récupérer les informations auprès du service Web Steam et signaler les données absentes ou inaccessibles.
- **FR-004** : En cas d’argument manquant ou supplémentaire, le système DOIT afficher l’usage attendu et terminer avec un code non nul.
- **FR-005** : Si la clé API est absente, le système DOIT indiquer où en obtenir une et comment la configurer dans `.env`.
- **FR-006** : En cas d’erreur réseau ou de réponse inexploitable, le système DOIT afficher les sections récupérées, signaler les sections en échec avec un message compréhensible et terminer avec un code non nul.
- **FR-007** : Le système DOIT signaler une section comme privée uniquement lorsque Steam l’indique explicitement. Il DOIT afficher les autres sections accessibles et terminer avec un code nul si aucun autre échec ne survient. Si Steam ne permet pas de distinguer une bibliothèque vide de données masquées, le système DOIT signaler que les données sont absentes ou masquées, sans affirmer qu’elles sont privées.
- **FR-008** : Le système NE DOIT PAS exposer la clé API dans ses sorties ou messages d’erreur.
- **FR-009** : Une recherche terminée sans erreur fatale DOIT se terminer avec un code nul.
- **FR-010** : La section Profil DOIT afficher le nom du compte et l’adresse de son profil lorsque Steam les fournit.

### Entités clés

- **Compte Steam** : Compte identifié par l’argument fourni à la commande.
- **Profil** : Informations de présentation accessibles pour le compte.
- **Jeu** : Jeu de la bibliothèque du compte, avec son temps de jeu total et la date de sa dernière partie lorsqu’ils sont disponibles.
- **Activité récente** : Jeux auxquels le compte a joué durant les 14 derniers jours et temps de jeu associé à cette période.
- **Clé API** : Secret nécessaire à l’accès au service, configuré hors du code source.

## Critères de réussite

- **SC-001** : Chaque recherche réussie affiche les trois sections attendues ; le nom et l’URL du profil sont affichés lorsqu’ils sont fournis par Steam.
- **SC-002** : Les statistiques et le classement de toute bibliothèque consultable correspondent aux données des jeux identifiés par Steam comme possédés ; le classement contient au maximum 10 jeux.
- **SC-003** : L’activité récente couvre 14 jours, affiche au maximum cinq jeux, distingue leur temps récent du temps total et classe les entrées sans date après les entrées datées.
- **SC-004** : Les erreurs d’argument, de clé, de consultation, les données privées et les données absentes ou masquées produisent les statuts et codes de sortie définis ; aucune donnée n’est qualifiée de privée sans indication explicite de Steam.
- **SC-005** : La clé API n’apparaît dans aucune sortie produite par les scénarios testés.

## Hypothèses

- L’argument désigne un SteamID64 ou une URL/nom personnalisé du profil permettant de retrouver le compte ; il ne s’agit pas nécessairement du nom affiché du profil.
- La clé API est fournie localement dans `.env`, lequel ne doit pas être ajouté au dépôt.
- La visibilité des données dépend des paramètres de confidentialité Steam du compte.
- L’activité récente désigne les jeux joués durant les 14 derniers jours ; elle affiche au plus les 5 plus récents.
- Les durées sont affichées en heures décimales arrondies à une décimale et les dates au format `AAAA-MM-JJ` en UTC.