<!--
Rapport d’impact de synchronisation
Changement de version : 1.0.0 → 1.1.0
Principes modifiés : formulation normative et vérifiable des principes existants
Sections ajoutées : contraintes techniques et de sécurité ; processus de développement et qualité
Sections supprimées : aucune ; exigences réorganisées selon le modèle résolu
Suivis à effectuer : aucun
-->

# SteamLookUpSpecKit Constitution

## Core Principles

### I. Simplicité et responsabilités claires
Le projet DOIT rester petit et compréhensible. Chaque composant DOIT avoir une seule
responsabilité claire et ses dépendances DOIVENT être fournies par injection au constructeur.
Toute complexité supplémentaire DOIT être justifiée par une exigence concrète du projet.

### II. Application en ligne de commande
L’application DOIT fonctionner comme une application Spring Boot en ligne de commande sans
démarrer de serveur web. Elle DOIT accepter exactement un identifiant de compte Steam et
afficher le profil, la bibliothèque de jeux et l’activité récente dans des sections distinctes
du terminal. Les erreurs d’usage et d’exécution DOIVENT être clairement distinguées des
résultats récupérés.

### III. Appels API et coordination explicites
Les réponses de l’API Web Steam DOIVENT être traitées explicitement, y compris les données
absentes, mal formées ou indisponibles. Les requêtes dépendantes DOIVENT respecter leur ordre
de dépendance ; les requêtes indépendantes PEUVENT être exécutées en parallèle. Le parallélisme
NE DOIT PAS rendre les résultats ou les échecs trompeurs.

### IV. Configuration sûre et gestion des erreurs
La clé API Steam DOIT être fournie hors du code source au moyen de `STEAM_API_KEY` dans un
fichier `.env` local qui NE DOIT PAS être commité. La clé NE DOIT apparaître ni dans la sortie
du terminal, ni dans les journaux, ni dans les messages d’erreur. Les erreurs réseau, les
données privées et les données absentes DOIVENT être distinguées. Les erreurs d’usage ou
d’exécution DOIVENT produire un code de sortie non nul ; une exécution réussie DOIT produire
le code zéro.

### V. Comportement testé
Chaque fonctionnalité DOIT disposer de tests automatisés couvrant son comportement observable.
Les tests des échanges avec l’API Steam DOIVENT utiliser des réponses contrôlées plutôt que
dépendre du service Steam en direct. Les tests DOIVENT couvrir les cas pertinents de succès,
de données manquantes ou privées et d’échec.

## Contraintes techniques et sécurité

- Le projet DOIT utiliser Java, Gradle Kotlin DSL et Spring Boot, conformément au projet
  existant généré par Spring Initializr.
- Le point d’entrée en ligne de commande DOIT utiliser `ApplicationRunner` et
  `ApplicationArguments`.
- L’application NE DOIT PAS ajouter de serveur web.
- La clé API DOIT rester hors du contrôle de version ; `.env` DOIT rester ignoré par Git.

## Processus de développement et qualité

- Les spécifications, plans, tâches, implémentations et tests DOIVENT respecter cette
  constitution.
- Avant la planification d’une fonctionnalité, son comportement visible par l’utilisateur et
  ses cas limites importants DOIVENT être spécifiés et clarifiés.
- Les changements DOIVENT inclure des tests pour chaque comportement modifié et DOIVENT
  réussir la suite de tests du projet avant d’être considérés comme terminés.
- Les revues DOIVENT vérifier la protection de la clé API, les codes de sortie de l’interface
  CLI, le traitement des données privées ou absentes et l’injection des dépendances par
  constructeur, le cas échéant.

## Governance

Cette constitution régit les spécifications, plans, tâches, implémentations et revues des
fonctionnalités. Toute exception DOIT être documentée et justifiée dans le plan de la
fonctionnalité concernée et NE DOIT PAS affaiblir la protection de la clé API ni rendre les
erreurs trompeuses.

Toute modification de la constitution exige une décision explicite du projet, une mise à jour
de la version et de la date de modification, ainsi qu’une revue des spécifications, plans,
tâches et tests concernés. La version suit le versionnage sémantique : MAJOR pour les
changements incompatibles de principes, MINOR pour de nouveaux principes ou contraintes ou
leur extension substantielle, et PATCH pour les clarifications et changements rédactionnels
non sémantiques. Les revues de fonctionnalités DOIVENT vérifier le respect des exigences MUST
applicables.

**Version**: 1.1.0 | **Ratified**: 2026-11-14 | **Last Amended**: 2026-11-15
