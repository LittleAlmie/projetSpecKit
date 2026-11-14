# Démarrage rapide : Steam Lookup CLI

## Prérequis

- Java 25 disponible pour la toolchain Gradle du projet.
- Une clé Web API Steam.
- Un SteamID64, un nom personnalisé ou l’URL publique d’un profil.

## Configuration locale

Depuis le module `steamLookUpSpecKit`, créer un fichier `.env` ignoré par Git :

```properties
STEAM_API_KEY=<votre-cle-steam>
```

Ne pas ajouter ce fichier au dépôt ni partager sa clé.

## Exécution

```sh
cd steamLookUpSpecKit
./gradlew bootRun --args="<steamid64-ou-nom-de-profil>"
```

La sortie présente Profil, Bibliothèque et Activité récente. Si une section est privée ou
indisponible, elle est identifiée comme telle ; une section en échec n’efface pas les autres.

## Vérification automatisée

```sh
cd steamLookUpSpecKit
./gradlew test
```

Les tests utilisent des réponses HTTP contrôlées et ne nécessitent ni clé Steam ni accès au
service réel. Les tests d’exécution manuelle avec Steam nécessitent une clé locale valide.

## Résultats attendus des erreurs

- Argument absent ou plusieurs arguments : usage affiché, code de sortie non nul.
- Clé absente : aide sur l’obtention et la configuration de la clé, code non nul.
- Données privées explicites : section signalée, code nul si aucune autre section n’échoue.
- Données absentes ou masquées impossibles à départager : statut incertain explicite, sans
  prétendre que le compte est privé.
- Erreur réseau : erreur explicite, autres résultats conservés et code de sortie non nul.
