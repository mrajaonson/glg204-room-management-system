# GLG204 - Room Management System

Application web de gestion de réservations de salles pour un établissement d'enseignement.

## Présentation

Le système permet :
- aux **appariteurs** de gérer les salles (création, modification, suppression), leurs équipements et leurs plages de disponibilité ;
- aux **utilisateurs** (enseignants, appariteurs) de rechercher des salles disponibles selon divers critères (date, capacité, équipements) et d'effectuer des réservations ;
- aux **responsables** de valider ou rejeter les demandes de réservation ;
- aux **administrateurs** de gérer les comptes utilisateurs.

Les utilisateurs sont notifiés par mail à chaque étape clé (validation de compte, confirmation ou annulation de réservation).

## Structure du dépôt

```
.
├── backend/        # API REST Spring Boot (Java 21)
├── frontend/       # Interface Vue.js 3 + Vite (TypeScript)
├── docs/           # Documentation du projet
│   ├── 01-Besoins.md       # Expression des besoins (use cases, acteurs)
│   ├── 02-Analyse.md       # Analyse orientée objet (classes métier, UML)
│   └── 03-Conception.md    # Conception et choix techniques (architecture)
└── pandoc/         # Outillage de génération PDF depuis Markdown
```

## Stack technique

| Couche      | Technologie                                     |
|-------------|-------------------------------------------------|
| Backend     | Java 21, Spring Boot 4.x, Gradle                |
| Frontend    | Vue.js 3, Vite 8, TypeScript, Pinia, Vue Router |
| Tests       | JUnit 5 (backend), Vitest + Cypress (frontend)  |
| Lint/Format | ESLint, oxlint, Prettier                        |

## Prérequis

- **Java 21+**
- **Node.js 22.18+ ou 24.12+**

## Lancement

### Backend

```bash
cd backend
./gradlew bootRun
```

L'API est disponible sur `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

L'interface est disponible sur `http://localhost:5174`.

## Tests

### Backend

```bash
cd backend
./gradlew test
```

### Frontend

```bash
cd frontend
# Tests unitaires
npm run test:unit

# Tests e2e (nécessite un build préalable)
npm run test:e2e
```

## Documentation

La documentation du projet suit la méthodologie Arrington avec des diagrammes UML (PlantUML) et est organisée en trois phases :

| Document                                    | Contenu                                                                    |
|---------------------------------------------|----------------------------------------------------------------------------|
| [`01-Besoins.md`](docs/01-Besoins.md)       | Expression des besoins : acteurs, cas d'utilisation, diagrammes d'activité |
| [`02-Analyse.md`](docs/02-Analyse.md)       | Analyse : sélection des use cases, classes métier, diagrammes de séquence  |
| [`03-Conception.md`](docs/03-Conception.md) | Conception : architecture, choix techniques, design du système             |

Les PDF correspondants sont générés via Pandoc (voir [`pandoc/`](pandoc/)).

## Licence

[MIT](LICENSE)
