# Projet *Réservation de salles* : Analyse v1.5

## 1. Table des matières

\tableofcontents
\newpage

## 2. Objectif du document

Ce document contient une analyse orientée objet fondée sur l'expression des besoins du projet "Réservation de salles".
L'analyse suit le langage de modélisation UML et la méthodologie Arrington. Le but essentiel est d'établir la liste des classes métier nécessaires pour modéliser chaque cas d'utilisation, et de voir comment elles seront manipulées par l'application.

## 3. Sélection des cas d'utilisation.

*On commence par évaluer les use case selon les critères :*

- *risques*
- *pertinence*
- *compétence de l'équipe*

*Les risques sont* :
- *les performances* ;
- *la qualité de l'UI* ;
- *les difficultés de planning* ;
- *la difficulté de prendre en charge de nouvelles spécifications* ;

Pour les risques, on peut utiliser l'échelle :

1. notre équipe a déjà fait ça ;
2. une autre équipe de l'entreprise l'a déjà fait ;
3. c'est un problème bien documenté (outils, tutoriels) ;
4. c'est un problème à creuser, mais des exemples similaires existent ;
5. c'est innovant.

Pour la pertinence :

1. son absence serait à peine remarquée ;
2. le système reste cohérent sans cette fonctionnalité ;
3. la majeure partie du système peut fonctionner ;
4. une partie du système peut fonctionner ;
5. le système ne fonctionnera pas sans cette fonctionnalité.


| Groupe      | Cas                                                     | Risques | Pertinence | Prioritaire |
|-------------|---------------------------------------------------------|---------|------------|-------------|
| Compte      | Déposer une demande de création de compte               | 1       | 5          | oui         |
| Compte      | Consulter les demandes de création de compte en attente | 1       | 5          | oui         |
| Compte      | Valider une demande de création de compte               | 1       | 5          | oui         |
| Compte      | Refuser une demande de création de compte               | 1       | 4          | non         |
| Compte      | Modifier les informations d'un compte                   | 1       | 2          | non         |
| Compte      | Modifier un mot de passe                                | 3       | 2          | non         |
| Compte      | Demander la réinitialisation d'un mot de passe          | 3       | 2          | non         |
| Compte      | Modifier un mot de passe réinitialisé                   | 3       | 2          | non         |
| Compte      | Se connecter / S'authentifier                           | 1       | 5          | oui         |
| Salle       | Créer une salle                                         | 1       | 5          | oui         |
| Salle       | Créer un équipement                                     | 1       | 3          | oui         |
| Salle       | Ajouter une plage de disponibilité d'une salle          | 4       | 5          | oui         |
| Salle       | Consulter la liste des salles                           | 1       | 4          | oui         |
| Salle       | Modifier une salle                                      | 1       | 2          | non         |
| Salle       | Supprimer une salle                                     | 1       | 2          | non         |
| Salle       | Modifier un équipement                                  | 1       | 1          | non         |
| Salle       | Supprimer un équipement                                 | 1       | 1          | non         |
| Salle       | Modifier une plage de disponibilité d'une salle         | 3       | 2          | non         |
| Salle       | Supprimer une plage de disponibilité d'une salle        | 3       | 2          | non         |
| Réservation | Afficher la liste des salles                            | 1       | 5          | oui         |
| Réservation | Rechercher une salle selon différents critères          | 4       | 4          | oui         |
| Réservation | Réserver une salle                                      | 3       | 5          | oui         |
| Réservation | Consulter une réservation                               | 1       | 4          | oui         |
| Réservation | Annuler une réservation                                 | 3       | 4          | oui         |
| Réservation | Consulter les demandes de réservation en attente        | 1       | 5          | oui         |
| Réservation | Valider une demande de réservation                      | 3       | 5          | oui         |
| Réservation | Rejeter une demande de réservation                      | 1       | 4          | oui         |
| Réservation | Modifier une réservation                                | 3       | 3          | non         |

## 4. Cas d'utilisation

### 4.1. Groupe 1 : gestion des comptes

#### 4.1.1. Déposer une demande de création de compte

##### Classes candidates

```plantuml
@startuml
skin rose

class Compte <<entity>> {
  login : String
  motDePasse : String
  mail : String
}

enum EtatDemande {
  CREEE
  MAIL_ENVOYE
  MAIL_VALIDE
  VALIDEE
  REFUSEE
}

class DemandeCreationCompte <<lifecycle>> {
  login : String
  motDePasse : String
  mail : String
  etat : EtatDemande
  dateCreation : Date
}

class FormulaireDemandeCompte <<boundary>> {
  login : String
  motDePasse : String
  motDePasseConfirmation : String
  mail : String
}

class ServiceCompte <<control>> {
  validerDonnees()
  verifierLoginDisponible()
  creerDemande()
}

class ServiceNotification <<boundary>> {
  envoyerMailConfirmation()
}

FormulaireDemandeCompte ..> ServiceCompte
ServiceCompte ..> Compte
ServiceCompte ..> DemandeCreationCompte
ServiceCompte ..> ServiceNotification
DemandeCreationCompte -> EtatDemande : > etat

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Utilisateur as u
boundary FormulaireDemandeCompte as ui
control ServiceCompte as svc
entity Compte as compte
participant DemandeCreationCompte as dcm <<lifecycle>>
control ServiceNotification as notif

u -> ui : setLogin()
u -> ui : setMotDePasse()
u -> ui : setMotDePasseConfirmation()
u -> ui : setMail()
alt données incorrectes
  ui -> ui : verifierDonnees() : false
  ui --> u : message d'erreur
else données valides
  ui -> ui : verifierDonnees()
  ui -> svc : soumettreFormulaire()
end
alt login indisponible
  svc -> compte : verifierLoginDisponible(login)
  compte --> svc : false
  svc --> ui : login indisponible
  ui --> u : message d'erreur
else login disponible
  svc -> compte : verifierLoginDisponible(login)
  compte --> svc : true
  svc -> dcm : creerDemande(login, motDePasse, mail)
  note right : etat = CREEE
  svc -> notif : envoyerMailConfirmation(mail)
  svc -> dcm : majEtat()
  note right : etat = MAIL_ENVOYE
  svc --> ui : confirmation envoi mail
  ui --> u : confirmation envoi mail
end
@enduml
```

##### Validation du lien mail

```plantuml
@startuml
skin rose

actor Utilisateur as u
control ServiceCompte as svc
participant DemandeCreationCompte as dcm <<lifecycle>>

u -> svc : validerMail(lien)
svc -> dcm : majEtat()
note right : etat = MAIL_VALIDE
@enduml
```

#### 4.1.2. Consulter les demandes de création de compte en attente

##### Classes candidates

```plantuml
@startuml
skin rose

class ListeDemandesCompteUI <<boundary>> {}

class ServiceCompte <<control>> {
  listerDemandesEnAttente()
}

class DemandeCreationCompte <<lifecycle>> {
  login : String
  motDePasse : String
  mail : String
  etat : EtatDemande
  dateCreation : Date
}

ListeDemandesCompteUI ..> ServiceCompte
ServiceCompte ..> DemandeCreationCompte

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Administrateur as a
boundary ListeDemandesCompteUI as ui
control ServiceCompte as svc
participant DemandeCreationCompte as dcm <<lifecycle>>

a -> ui : consulterDemandes()
ui -> svc : listerDemandesEnAttente()
svc -> dcm : findByEtat(MAIL_VALIDE)
dcm --> svc : liste des demandes
svc --> ui : liste des demandes
ui --> a : affiche la liste
@enduml
```

#### 4.1.3. Valider une demande de création de compte

##### Classes candidates

Aucune nouvelle classe. Les classes `ListeDemandesCompteUI`, `ServiceCompte`, `DemandeCreationCompte`, `Compte` et `ServiceNotification` sont définies dans les sections précédentes.

##### Séquence

```plantuml
@startuml
skin rose

actor Administrateur as a
boundary ListeDemandesCompteUI as ui
control ServiceCompte as svc
participant DemandeCreationCompte as dcm <<lifecycle>>
entity Compte as compte
control ServiceNotification as notif

a -> ui : validerDemande(demande)
ui -> svc : validerDemande(demande)
svc -> compte : creerCompte(login, motDePasse, mail)
svc -> dcm : majEtat()
note right : etat = VALIDEE
svc -> notif : envoyerMailValidation(mail)
svc --> ui : confirmation
ui --> a : confirmation
@enduml
```

#### 4.1.4. Refuser une demande de création de compte

##### Classes candidates

Aucune nouvelle classe. Les classes `ListeDemandesCompteUI`, `ServiceCompte`, `DemandeCreationCompte` et `ServiceNotification` sont définies dans les sections précédentes.

##### Séquence

```plantuml
@startuml
skin rose

actor Administrateur as a
boundary ListeDemandesCompteUI as ui
control ServiceCompte as svc
participant DemandeCreationCompte as dcm <<lifecycle>>
control ServiceNotification as notif

a -> ui : rejeterDemande(demande)
ui -> svc : rejeterDemande(demande)
svc -> dcm : majEtat()
note right : etat = REFUSEE
svc -> notif : envoyerMailRefus(mail)
svc --> ui : confirmation
ui --> a : confirmation
@enduml
```

#### 4.1.5. Modifier les informations d'un compte

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireModificationCompte <<boundary>> {
  login : String
  mail : String
}

class ServiceCompte <<control>> {
  modifierCompte()
}

class Compte <<entity>> {
  login : String
  motDePasse : String
  mail : String
}

FormulaireModificationCompte ..> ServiceCompte
ServiceCompte ..> Compte

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Utilisateur as u
boundary FormulaireModificationCompte as ui
control ServiceCompte as svc
entity Compte as compte

u -> ui : setLogin()
u -> ui : setMail()
alt données incorrectes
  ui -> ui : verifierDonnees() : false
  ui --> u : message d'erreur
else données valides
  ui -> ui : verifierDonnees()
  ui -> svc : modifierCompte()
  svc -> compte : majInfos(login, mail)
  svc --> ui : confirmation
  ui --> u : confirmation
end
@enduml
```

#### 4.1.6. Modifier un mot de passe

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireModificationMotDePasse <<boundary>> {
  motDePasseActuel : String
  nouveauMotDePasse : String
  nouveauMotDePasseConfirmation : String
}

class ServiceCompte <<control>> {
  modifierMotDePasse()
}

class Compte <<entity>> {
  login : String
  motDePasse : String
  mail : String
}

FormulaireModificationMotDePasse ..> ServiceCompte
ServiceCompte ..> Compte

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Utilisateur as u
boundary FormulaireModificationMotDePasse as ui
control ServiceCompte as svc
entity Compte as compte

u -> ui : setMotDePasseActuel()
u -> ui : setNouveauMotDePasse()
u -> ui : setNouveauMotDePasseConfirmation()
alt données incorrectes
  ui -> ui : verifierDonnees() : false
  ui --> u : message d'erreur
else données valides
  ui -> ui : verifierDonnees()
  ui -> svc : modifierMotDePasse()
end
alt mot de passe incorrect
  svc -> compte : verifierMotDePasse(motDePasseActuel)
  compte --> svc : false
  svc --> ui : mot de passe incorrect
  ui --> u : message d'erreur
else mot de passe correct
  svc -> compte : verifierMotDePasse(motDePasseActuel)
  compte --> svc : true
  svc -> compte : majMotDePasse(nouveauMotDePasse)
  svc --> ui : confirmation
  ui --> u : confirmation
end
@enduml
```

#### 4.1.7. Demander la réinitialisation d'un mot de passe

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireDemandeReinitialisation <<boundary>> {
  mail : String
}

FormulaireDemandeReinitialisation ..> ServiceCompte
ServiceCompte ..> Compte
ServiceCompte ..> ServiceNotification

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Utilisateur as u
boundary FormulaireDemandeReinitialisation as ui
control ServiceCompte as svc
entity Compte as compte
boundary ServiceNotification as notif

u -> ui : setMail()
alt données incorrectes
  ui -> ui : verifierDonnees() : false
  ui --> u : message d'erreur
else données valides
  ui -> ui : verifierDonnees()
  ui -> svc : demanderReinitialisation(mail)
end
alt compte introuvable
  svc -> compte : findByMail(mail)
  compte --> svc : null
  svc --> ui : compte introuvable
  ui --> u : message d'erreur
else compte trouvé
  svc -> compte : findByMail(mail)
  compte --> svc : compte
  svc -> notif : envoyerMailReinitialisation(mail)
  svc --> ui : confirmation envoi mail
  ui --> u : confirmation envoi mail
end
@enduml
```

#### 4.1.8. Modifier un mot de passe réinitialisé

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireNouveauMotDePasse <<boundary>> {
  nouveauMotDePasse : String
  nouveauMotDePasseConfirmation : String
}

FormulaireNouveauMotDePasse ..> ServiceCompte
ServiceCompte ..> Compte

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Utilisateur as u
boundary FormulaireNouveauMotDePasse as ui
control ServiceCompte as svc
entity Compte as compte

u -> ui : setNouveauMotDePasse()
u -> ui : setNouveauMotDePasseConfirmation()
alt données incorrectes
  ui -> ui : verifierDonnees() : false
  ui --> u : message d'erreur
else données valides
  ui -> ui : verifierDonnees()
  ui -> svc : reinitialiserMotDePasse(token, nouveauMotDePasse)
  svc -> compte : majMotDePasse(nouveauMotDePasse)
  svc --> ui : confirmation
  ui --> u : confirmation
end
@enduml
```

#### 4.1.9. Se connecter / S'authentifier

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireConnexion <<boundary>> {
  login : String
  motDePasse : String
}

class ServiceAuth <<control>> {
  authentifier(login : String, motDePasse : String)
  genererToken(compte : Compte)
}

class Compte <<entity>> {
  login : String
  motDePasseHash : String
  mail : String
  role : RoleCompte
}

FormulaireConnexion ..> ServiceAuth
ServiceAuth ..> Compte

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Utilisateur as u
boundary FormulaireConnexion as ui
control ServiceAuth as svc
entity Compte as compte

u -> ui : setLogin()
u -> ui : setMotDePasse()
alt données incorrectes
  ui -> ui : verifierDonnees() : false
  ui --> u : message d'erreur
else données valides
  ui -> ui : verifierDonnees()
  ui -> svc : authentifier(login, motDePasse)
end
alt identifiants invalides
  svc -> compte : findByLogin(login)
  compte --> svc : null ou motDePasse incorrect
  svc --> ui : echec authentification
  ui --> u : message d'erreur générique
else identifiants valides
  svc -> compte : findByLogin(login)
  compte --> svc : compte
  svc -> svc : verifierMotDePasse(motDePasse, compte)
  svc -> svc : genererToken(compte)
  svc --> ui : token
  ui --> u : redirection vers l'application
end
@enduml
```

### 4.2. Groupe 2 : gestion des salles

#### 4.2.1. Créer une salle

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireCreationSalle <<boundary>> {
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean
}

class ServiceSalle <<control>> {
  creerSalle()
}

class Salle <<entity>> {
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean
}

enum TypeSalle {
  COURS
  TP
  REUNION
  AMPHI
}

FormulaireCreationSalle ..> ServiceSalle
ServiceSalle ..> Salle
Salle -> TypeSalle : > type

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Responsable as r
boundary FormulaireCreationSalle as ui
control ServiceSalle as svc
entity Salle as salle

r -> ui : setNom()
r -> ui : setLocalisation()
r -> ui : setCapacite()
r -> ui : setDescription()
r -> ui : setType()
r -> ui : setReservationAvecValidation()
ui -> ui : verifierDonnees()
ui -> svc : creerSalle()
svc -> salle : creerSalle(nom, localisation, capacite, description, type, reservationAvecValidation)
svc --> ui : confirmation
ui --> r : confirmation
@enduml
```

#### 4.2.2. Créer un équipement

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireCreationEquipement <<boundary>> {
  nom : String
  description : String
  salle : Salle
}

class ServiceSalle <<control>> {
  creerEquipement()
}

class Equipement <<entity>> {
  nom : String
  description : String
}

FormulaireCreationEquipement ..> ServiceSalle
ServiceSalle ..> Equipement
Salle o-- "*" Equipement

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Responsable as r
boundary FormulaireCreationEquipement as ui
control ServiceSalle as svc
entity Equipement as eq

r -> ui : setNom()
r -> ui : setDescription()
ui -> ui : verifierDonnees()
ui -> svc : creerEquipement(nom, description, salle)
svc -> eq : creerEquipement(nom, description)
svc --> ui : confirmation
ui --> r : confirmation
@enduml
```

#### 4.2.3. Ajouter une plage de disponibilité d'une salle

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireDisponibiliteSalle <<boundary>> {
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  salle : Salle
}

class ServiceSalle <<control>> {
  ajouterPlageDisponibilite()
  verifierConflit()
}

class PlageDisponibilite <<lifecycle>> {
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  statut : StatutPlage
  salle : Salle
}

enum StatutPlage {
  ACTIVE
  INACTIVE
}

class Heure <<value>> {
  heure : Integer
  minute : Integer
}

FormulaireDisponibiliteSalle ..> ServiceSalle
PlageDisponibilite -> StatutPlage : > statut
PlageDisponibilite --> Heure : > heureDebut
PlageDisponibilite --> Heure : > heureFin
PlageDisponibilite -> Salle : > salle

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Responsable as r
boundary FormulaireDisponibiliteSalle as ui
control ServiceSalle as svc
entity Salle as salle
participant PlageDisponibilite as pd <<lifecycle>>

r -> ui : setDateDebut()
r -> ui : setHeureDebut()
r -> ui : setDateFin()
r -> ui : setHeureFin()
r -> ui : selectSalle()
ui -> ui : verifierDonnees()
ui -> svc : ajouterPlageDisponibilite(formulaire)
svc -> pd : creerPlage(dateDebut, heureDebut, dateFin, heureFin)
note right : statut = ACTIVE
alt conflit de disponibilité
  svc -> svc : verifierConflit(salle, pd)
  svc --> ui : erreur conflit de disponibilité
  ui --> r : message d'erreur
else aucun conflit
  svc -> svc : verifierConflit(salle, pd)
  svc -> salle : ajouterPlageDisponibilite(pd)
  svc --> ui : confirmation
  ui --> r : confirmation
end
@enduml
```

#### 4.2.4. Consulter la liste des salles

##### Classes candidates

```plantuml
@startuml
skin rose

class ListeSallesUI <<boundary>> {
  sallesAffichees : Liste<Salle>
  filtre : Filtre
}

class ServiceSalle <<control>> {
  listerSalles()
  rechercherSalles(filtre : Filtre)
}

class Salle <<entity>> {
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean

}

ListeSallesUI ..> ServiceSalle
ServiceSalle ..> Salle

@enduml
```

Le filtrage de la liste s'appuie sur l'objet `Filtre` et la méthode unique `rechercherSalles(filtre)` ; il n'y a plus d'attributs de filtre dédiés ni de méthodes de filtrage spécifiques.

##### Séquence

```plantuml
@startuml
skin rose

actor Responsable as r
boundary ListeSallesUI as ui
control ServiceSalle as svc
entity Salle as salle

r -> ui : consulterListeSalles()
alt avec filtre
  r -> ui : renseignerFiltre()
  ui -> ui : construireFiltre()
  ui -> svc : rechercherSalles(filtre)
  note right : filtrage détaillé en 4.3.2
  svc --> ui : liste filtrée
  ui --> r : affiche la liste filtrée
else sans filtre
  ui -> svc : listerSalles()
  svc -> salle : findAll()
  salle --> svc : liste des salles
  svc --> ui : liste des salles
  ui --> r : affiche la liste
end
@enduml
```

#### 4.2.5. Modifier une salle

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireModificationSalle <<boundary>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean
}

class ServiceSalle <<control>> {
  modifierSalle()
  verifierDonnees()
}

class Salle <<entity>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean


  modifierNom(nom : String)
  modifierLocalisation(localisation : String)
  modifierCapacite(capacite : Integer)
  modifierDescription(description : String)
  modifierType(type : TypeSalle)
  modifierReservationAvecValidation(reservationAvecValidation : Boolean)
}

FormulaireModificationSalle ..> ServiceSalle
ServiceSalle ..> Salle

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Responsable as r
boundary FormulaireModificationSalle as ui
control ServiceSalle as svc
entity Salle as salle

r -> ui : chargerSalle(id)
ui -> ui : afficherDonnees()
r -> ui : setNom()
r -> ui : setLocalisation()
r -> ui : setCapacite()
r -> ui : setDescription()
r -> ui : setType()
r -> ui : setReservationAvecValidation()
ui -> ui : verifierDonnees()
ui -> svc : modifierSalle(id, donnees)
svc -> svc : verifierDonnees()
svc -> salle : rechercherId(id)
salle --> svc : salle
svc -> salle : modifierNom(nom)
svc -> salle : modifierLocalisation(localisation)
svc -> salle : modifierCapacite(capacite)
svc -> salle : modifierDescription(description)
svc -> salle : modifierType(type)
svc -> salle : modifierReservationAvecValidation(reservationAvecValidation)
svc --> ui : confirmation
ui --> r : confirmation
@enduml
```

#### 4.2.6. Supprimer une salle

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireSuppressionSalle <<boundary>> {
  salle : Salle
  confirmation : Boolean
}

class ServiceSalle <<control>> {
  supprimerSalle()
  verifierPossibiliteSuppression()
}

class Salle <<entity>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean


  supprimer()
}

FormulaireSuppressionSalle ..> ServiceSalle
ServiceSalle ..> Salle

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Responsable as r
boundary FormulaireSuppressionSalle as ui
control ServiceSalle as svc
entity Salle as salle

r -> ui : chargerSalle(salle)
ui -> ui : afficherDonneesSalle()
r -> ui : confirmerSuppression()
ui -> svc : supprimerSalle(salle)
svc -> svc : verifierPossibiliteSuppression(salle)
note right : vérifier absence de réservations
alt salle avec réservations actives
  svc --> ui : echec suppression
  ui --> r : message d'erreur : salle avec réservations actives
else suppression possible
  svc -> salle : supprimer()
  svc --> ui : confirmation
  ui --> r : confirmation
end
@enduml
```

#### 4.2.7. Modifier un équipement

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireModificationEquipement <<boundary>> {
  id : Integer
  nom : String
  description : String
}

class ServiceSalle <<control>> {
  modifierEquipement()
  verifierDonnees()
}

class Equipement <<entity>> {
  id : Integer
  nom : String
  description : String

  modifierNom(nom : String)
  modifierDescription(description : String)
}

class Salle <<entity>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean
  equipements : Liste<Equipement>
}

FormulaireModificationEquipement ..> ServiceSalle
ServiceSalle ..> Equipement
Salle o-- "*" Equipement

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Responsable as r
boundary FormulaireModificationEquipement as ui
control ServiceSalle as svc
entity Equipement as eq

r -> ui : chargerEquipement(id)
ui -> ui : afficherDonnees()
r -> ui : setNom()
r -> ui : setDescription()
alt données incorrectes
  ui -> ui : verifierDonnees() : false
  ui --> r : message d'erreur
else données valides
  ui -> ui : verifierDonnees()
  ui -> svc : modifierEquipement(id, donnees)
  svc -> svc : verifierDonnees()
  svc -> eq : rechercherId(id)
  eq --> svc : equipement
  svc -> eq : modifierNom(nom)
  svc -> eq : modifierDescription(description)
  svc --> ui : confirmation
  ui --> r : confirmation
end
@enduml
```

#### 4.2.8. Supprimer un équipement

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireSuppressionEquipement <<boundary>> {
  equipement : Equipement
  confirmation : Boolean
}

class ServiceSalle <<control>> {
  supprimerEquipement()
  verifierPossibiliteSuppression()
}

class Equipement <<entity>> {
  id : Integer
  nom : String
  description : String

  supprimer()
}

class Salle <<entity>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean
  equipements : Liste<Equipement>
}

FormulaireSuppressionEquipement ..> ServiceSalle
ServiceSalle ..> Equipement
Salle o-- "*" Equipement

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Responsable as r
boundary FormulaireSuppressionEquipement as ui
control ServiceSalle as svc
entity Equipement as eq

r -> ui : chargerEquipement(equipement)
ui -> ui : afficherDonneesEquipement()
r -> ui : confirmerSuppression()
ui -> svc : supprimerEquipement(equipement)
svc -> svc : verifierPossibiliteSuppression(equipement)
alt échec suppression
  svc --> ui : echec suppression
  ui --> r : message d'erreur
else suppression possible
  svc -> eq : supprimer()
  svc --> ui : confirmation
  ui --> r : confirmation
end
@enduml
```

#### 4.2.9. Modifier une plage de disponibilité d'une salle

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireModificationDisponibilite <<boundary>> {
  id : Integer
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  statut : StatutPlage
}

class ServiceSalle <<control>> {
  modifierPlageDisponibilite()
  verifierConflit()
}

class PlageDisponibilite <<lifecycle>> {
  id : Integer
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  statut : StatutPlage

  modifierDateDebut(date : Date)
  modifierHeureDebut(heure : Heure)
  modifierDateFin(date : Date)
  modifierHeureFin(heure : Heure)
  modifierStatut(statut : StatutPlage)
}

class Salle <<entity>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean

}

class Heure <<value>> {
  heure : Integer
  minute : Integer
}

FormulaireModificationDisponibilite ..> ServiceSalle
ServiceSalle ..> PlageDisponibilite
PlageDisponibilite -> Salle : > salle
PlageDisponibilite --> Heure : > heureDebut
PlageDisponibilite --> Heure : > heureFin

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Responsable as r
boundary FormulaireModificationDisponibilite as ui
control ServiceSalle as svc
participant PlageDisponibilite as pd <<lifecycle>>

r -> ui : chargerPlage(id)
ui -> ui : afficherDonnees()
r -> ui : setDateDebut()
r -> ui : setHeureDebut()
r -> ui : setDateFin()
r -> ui : setHeureFin()
r -> ui : setStatut()
ui -> ui : verifierDonnees()
ui -> svc : modifierPlageDisponibilite(id, donnees)
svc -> svc : verifierConflit(donnees)
alt conflit détecté
  svc -> pd : chercherConflit()
  pd --> svc : conflit détecté
  svc --> ui : erreur : plage en conflit
  ui --> r : message d'erreur
else aucun conflit
  svc -> pd : rechercherId(id)
  pd --> svc : plage
  svc -> pd : modifierDateDebut(dateDebut)
  svc -> pd : modifierHeureDebut(heureDebut)
  svc -> pd : modifierDateFin(dateFin)
  svc -> pd : modifierHeureFin(heureFin)
  svc -> pd : modifierStatut(statut)
  svc --> ui : confirmation
  ui --> r : confirmation
end
@enduml
```

#### 4.2.10. Supprimer une plage de disponibilité d'une salle

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireSuppressionDisponibilite <<boundary>> {
  plage : PlageDisponibilite
  salle : Salle
  confirmation : Boolean
}

class ServiceSalle <<control>> {
  supprimerPlageDisponibilite()
  verifierPossibiliteSuppression()
}

class PlageDisponibilite <<lifecycle>> {
  id : Integer
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  statut : StatutPlage

  supprimer()
}

class Salle <<entity>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean


  retirerPlageDisponibilite(plage : PlageDisponibilite)
}

FormulaireSuppressionDisponibilite ..> ServiceSalle
ServiceSalle ..> PlageDisponibilite
PlageDisponibilite -> Salle : > salle

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Responsable as r
boundary FormulaireSuppressionDisponibilite as ui
control ServiceSalle as svc
participant PlageDisponibilite as pd <<lifecycle>>
entity Salle as salle

r -> ui : chargerPlage(plage)
ui -> ui : afficherDonneesPlage()
r -> ui : confirmerSuppression()
ui -> svc : supprimerPlageDisponibilite(plage)
svc -> svc : verifierPossibiliteSuppression(plage)
alt plage avec réservations associées
  svc --> ui : echec suppression
  ui --> r : message d'erreur : plage avec réservations associées
else suppression possible
  svc -> pd : supprimer()
  svc -> salle : retirerPlageDisponibilite(plage)
  svc --> ui : confirmation
  ui --> r : confirmation
end
@enduml
```

### 4.3. Groupe 3 : gestion des réservations

#### 4.3.1. Afficher la liste des salles

##### Classes candidates

```plantuml
@startuml
skin rose

class ListeSallesReservationUI <<boundary>> {
  sallesAffichees : Liste<Salle>
  filtre : Filtre
}

class ServiceSalle <<control>> {
  listerSalles()
  rechercherSalles(filtre : Filtre)
}

class Salle <<entity>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean

  equipements : Liste<Equipement>
}

ListeSallesReservationUI ..> ServiceSalle
ServiceSalle ..> Salle

@enduml
```

Le filtrage de la liste s'appuie sur l'objet `Filtre` et la méthode unique `rechercherSalles(filtre)` ; il n'y a plus d'attributs de filtre dédiés ni de méthodes de filtrage spécifiques.

##### Séquence

```plantuml
@startuml
skin rose

actor Utilisateur as u
boundary ListeSallesReservationUI as ui
control ServiceSalle as svc
entity Salle as salle

u -> ui : afficherListeSalles()
alt avec filtre
  u -> ui : renseignerFiltre()
  ui -> ui : construireFiltre()
  ui -> svc : rechercherSalles(filtre)
  note right : filtrage détaillé en 4.3.2
  svc --> ui : liste filtrée
  ui --> u : affiche la liste filtrée
else sans filtre
  ui -> svc : listerSalles()
  svc -> salle : findAll()
  salle --> svc : liste des salles
  svc --> ui : liste des salles
  ui --> u : affiche la liste
end
@enduml
```

#### 4.3.2. Rechercher une salle selon différents critères

Les critères de recherche sont regroupés dans un **objet unique `Filtre`** passé en paramètre à `ServiceSalle.rechercherSalles(filtre)`. Ce regroupement évite de multiplier les paramètres et rend la recherche **évolutive** : ajouter un critère revient à enrichir `Filtre`, sans changer la signature du service. Le formulaire construit ce `Filtre` à partir des saisies de l'utilisateur.

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireRechercheSalle <<boundary>> {
  nom : String
  localisation : String
  capaciteMin : Integer
  capaciteMax : Integer
  type : TypeSalle
  equipements : Liste<String>
  disponibilite : Boolean
  dateDebut : Date
  dateFin : Date
  heureDebut : Heure
  heureFin : Heure
}

class ServiceSalle <<control>> {
  rechercherSalles(filtre : Filtre)
  appliquerFiltres()
}

class Filtre <<value>> {
  nom : String
  localisation : String
  capaciteMin : Integer
  capaciteMax : Integer
  type : TypeSalle
  equipements : Liste<String>
  disponibilite : Boolean
  dateDebut : Date
  dateFin : Date
  heureDebut : Heure
  heureFin : Heure
}

class FiltreRechercheSalle <<entity>> {
  creerFiltre(filtre : Filtre)
  appliquerFiltre(salle : Salle) : Boolean
}

class Salle <<entity>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean
  equipements : Liste<Equipement>
}

FormulaireRechercheSalle ..> ServiceSalle
ServiceSalle ..> FiltreRechercheSalle
ServiceSalle ..> Salle
FiltreRechercheSalle ..> Filtre
FiltreRechercheSalle ..> Salle : > appliquerFiltre

@enduml
```

##### Cas nominal : recherche par nom

```plantuml
@startuml
skin rose

actor Utilisateur as u
boundary FormulaireRechercheSalle as ui
control ServiceSalle as svc
entity FiltreRechercheSalle as filtre
participant Filtre as f <<value>>
entity Salle as salle

u -> ui : setNom()
u -> ui : setLocalisation()
ui -> ui : verifierDonnees()
ui -> f : new Filtre(nom, localisation)
f --> ui : filtre
ui -> svc : rechercherSalles(filtre)
svc -> filtre : creerFiltre(filtre)
svc -> salle : findAll()
salle --> svc : toutes les salles
svc -> filtre : appliquerFiltre(salles)
filtre --> svc : salles filtrees
svc --> ui : liste des salles
ui --> u : affiche la liste
@enduml
```

##### Déroulement alternatif : recherche par disponibilité

```plantuml
@startuml
skin rose

actor Utilisateur as u
boundary FormulaireRechercheSalle as ui
control ServiceSalle as svc
entity FiltreRechercheSalle as filtre
participant Filtre as f <<value>>
entity Salle as salle
participant PlageDisponibilite as pd <<lifecycle>>

u -> ui : setCapaciteMin()
u -> ui : setType()
u -> ui : setDisponible()
u -> ui : setDate()
u -> ui : setHeureDebut()
u -> ui : setHeureFin()
ui -> ui : verifierDonnees()
ui -> f : new Filtre(capaciteMin, type, disponibilite, dateDebut, dateFin, heureDebut, heureFin)
f --> ui : filtre
ui -> svc : rechercherSalles(filtre)
svc -> filtre : creerFiltre(filtre)
svc -> salle : findByCapaciteAndType(capaciteMin, type)
salle --> svc : salles candidates
svc -> filtre : filtrerByDisponibilite(salles, filtre)
filtre -> pd : verifierDisponibilite(plage, filtre)
pd --> filtre : disponible / non disponible
filtre --> svc : salles disponibles
svc --> ui : liste des salles
ui --> u : affiche la liste
@enduml
```

#### 4.3.3. Réserver une salle

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireReservationSalle <<boundary>> {
  salle : Salle
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  motif : String
  demandeur : Compte
}

class ServiceReservation <<control>> {
  creerReservation()
  verifierDisponibilite()
  creerDemande()
}

class ServiceNotification <<boundary>> {
  preparerNotificationValidation()
  envoyerNotificationValidation()
}

class Reservation <<entity>> {
  id : Integer
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  motif : String
  demandeur : Compte
  salle : Salle
  statut : EtatReservation
}

class DemandeReservation <<lifecycle>> {
  reservation : Reservation
  dateCreation : Date
  etat : EtatDemandeReservation
}

class Compte <<entity>> {
  id : Integer
  login : String
  mail : String
}

class Salle <<entity>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean

  equipements : Liste<Equipement>
}

class PlageDisponibilite <<lifecycle>> {
  id : Integer
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  statut : StatutPlage
  salle : Salle
}

enum EtatReservation {
  EN_ATTENTE
  CONFIRMEE
  REFUSEE
  ANNULEE
}

enum EtatDemandeReservation {
  CREEE
  VALIDEE
  REJETEE
}

FormulaireReservationSalle ..> ServiceReservation
ServiceReservation ..> Reservation
ServiceReservation ..> DemandeReservation
ServiceReservation ..> ServiceNotification
Reservation -> Compte : > demandeur
Reservation -> Salle : > salle
Reservation -> EtatReservation : > statut
DemandeReservation --> Reservation : > reservation
DemandeReservation -> EtatDemandeReservation : > etat
PlageDisponibilite -> Salle : > salle

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Utilisateur as u
boundary FormulaireReservationSalle as ui
control ServiceReservation as svc
entity Reservation as res
participant DemandeReservation as dr <<lifecycle>>
entity Compte as compte
control ServiceNotification as notif

u -> ui : setSalle()
u -> ui : setDateDebut()
u -> ui : setHeureDebut()
u -> ui : setDateFin()
u -> ui : setHeureFin()
u -> ui : setMotif()
ui -> ui : verifierDonnees()
ui -> svc : creerReservation(formulaire)
svc -> svc : verifierDisponibilite(salle, dateDebut, dateFin, heureDebut, heureFin)
svc -> compte : rechercherId(demandeurId)
compte --> svc : compte
svc -> res : creerReservation(dateDebut, dateFin, heureDebut, heureFin, motif, demandeur, salle)
note right : statut = EN_ATTENTE
svc -> res : sauvegarder()
alt validation requise
  svc -> dr : creerDemande(reservation, dateCreation)
  note right : etat = CREEE
  svc -> notif : preparerNotificationValidation(reservation, compte)
  notif --> svc : notification preparée
  svc -> notif : envoyerNotificationValidation(compte, reservation)
  svc --> ui : confirmation en attente
  ui --> u : demande envoyee pour validation
else sans validation
  svc --> ui : confirmation
  ui --> u : confirmation reservation
end
@enduml
```

#### 4.3.4. Consulter une réservation

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireConsultationReservation <<boundary>> {
  reservation : Reservation
  detailAffiche : Boolean
}

class ServiceReservation <<control>> {
  consulterReservation()
  afficherDetails()
}

class Reservation <<entity>> {
  id : Integer
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  motif : String
  demandeur : Compte
  salle : Salle
  statut : EtatReservation
}

class DemandeReservation <<lifecycle>> {
  reservation : Reservation
  dateCreation : Date
  etat : EtatDemandeReservation
}

class Salle <<entity>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean

  equipements : Liste<Equipement>
}

enum EtatReservation {
  EN_ATTENTE
  CONFIRMEE
  REFUSEE
  ANNULEE
}

enum EtatDemandeReservation {
  CREEE
  VALIDEE
  REJETEE
}

FormulaireConsultationReservation ..> ServiceReservation
ServiceReservation ..> Reservation
Reservation -> Salle : > salle
Reservation -> EtatReservation : > statut
DemandeReservation --> Reservation : > reservation
DemandeReservation -> EtatDemandeReservation : > etat

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Utilisateur as u
boundary FormulaireConsultationReservation as ui
control ServiceReservation as svc
entity Reservation as res
entity Salle as salle

u -> ui : consulterReservation(id)
ui -> svc : consulterReservation(id)
svc -> res : rechercherId(id)
res --> svc : reservation
svc -> res : obtenirSalle()
res --> svc : salle
svc --> ui : reservation completee
ui --> u : affiche reservation detaillee
@enduml
```

#### 4.3.5. Modifier une réservation

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireModificationReservation <<boundary>> {
  idReservation : Integer
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  motif : String
}

class ServiceReservation <<control>> {
  modifierReservation()
  verifierDisponibilite()
}

class Reservation <<entity>> {
  id : Integer
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  motif : String
  demandeur : Compte
  salle : Salle
  statut : EtatReservation
}

class Salle <<entity>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean

  equipements : Liste<Equipement>
}

class PlageDisponibilite <<lifecycle>> {
  id : Integer
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  statut : StatutPlage
  salle : Salle
}

enum EtatReservation {
  EN_ATTENTE
  CONFIRMEE
  REFUSEE
  ANNULEE
}

class Compte <<entity>> {
  id : Integer
  login : String
  mail : String
}

class Heure <<value>> {
  heure : Integer
  minute : Integer
}

FormulaireModificationReservation ..> ServiceReservation
ServiceReservation ..> Reservation
Reservation -> Compte : > demandeur
Reservation -> Salle : > salle
Reservation -> EtatReservation : > statut
PlageDisponibilite -> Salle : > salle

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Utilisateur as u
boundary FormulaireModificationReservation as ui
control ServiceReservation as svc
entity Reservation as res

u -> ui : chargerReservation(id)
ui -> ui : afficherDonnees()
u -> ui : setDates()
u -> ui : setHeures()
u -> ui : setMotif()
ui -> ui : verifierDonnees()
ui -> svc : modifierReservation(id, modifications)
svc -> res : rechercherId(id)
res --> svc : reservation
svc -> svc : verifierDisponibilite(nouvellesDates)
alt conflit de disponibilité
  svc -> PlageDisponibilite : detectConflit(salle, dates)
  PlageDisponibilite --> svc : conflit trouve
  svc --> ui : echec reservation
  ui --> u : message d'erreur : nouvelle plage indisponible
else aucun conflit
  svc -> res : modifier(dates, heures, motif)
  res --> svc : reservation modifiee
  svc -> res : sauvegarder()
  svc --> ui : confirmation
  ui --> u : confirmation modification
end
@enduml
```

#### 4.3.6. Annuler une réservation

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireAnnulationReservation <<boundary>> {
  reservation : Reservation
  motifAnnulation : String
  confirmation : Boolean
}

class ServiceReservation <<control>> {
  annulerReservation()
  verifierPossibiliteAnnulation()
  envoyerNotification()
}

class Reservation <<entity>> {
  id : Integer
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  motif : String
  demandeur : Compte
  salle : Salle
  statut : EtatReservation

  annuler()
}

class Salle <<entity>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean

  equipements : Liste<Equipement>
}

class Compte <<entity>> {
  id : Integer
  login : String
  mail : String
}

class ServiceNotification <<boundary>> {
  preparerNotificationAnnulation()
  envoyerNotificationAnnulation()
}

FormulaireAnnulationReservation ..> ServiceReservation
ServiceReservation ..> Reservation
ServiceReservation ..> ServiceNotification
Reservation -> Compte : > demandeur
Reservation -> Salle : > salle

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Utilisateur as u
boundary FormulaireAnnulationReservation as ui
control ServiceReservation as svc
entity Reservation as res
entity Salle as salle
control ServiceNotification as notif

u -> ui : chargerReservation(reservation)
ui -> ui : afficherDetailsReservation()
u -> ui : setMotifAnnulation()
u -> ui : confirmerAnnulation()
ui -> svc : annulerReservation(reservation, motifAnnulation)
svc -> svc : verifierPossibiliteAnnulation(reservation)
alt annulation impossible (dates trop proches)
  svc --> ui : echec annulation
  ui --> u : message d'erreur : annulation impossible (dates trop proches)
else annulation possible
  svc -> res : verifierStatut(reservation)
  res --> svc : statut = EN_ATTENTE or CONFIRMEE
  svc -> res : annuler()
  note right : statut = ANNULEE
  svc -> salle : retirerPlageDisponibilite(plage)
  svc -> notif : preparerNotificationAnnulation(reservation)
  notif --> svc : notification preparée
  svc -> notif : envoyerNotificationAnnulation(reservation)
  svc --> ui : confirmation
  ui --> u : reservation annulee
end
@enduml
```

#### 4.3.7. Consulter les demandes de réservation en attente

##### Classes candidates

```plantuml
@startuml
skin rose

class ListeDemandesReservationUI <<boundary>> {
  demandesAffichees : Liste<DemandeReservation>
  filtreStatut : EtatDemandeReservation
}

class ServiceReservation <<control>> {
  listerDemandesEnAttente()
  filtrerDemandes()
}

class DemandeReservation <<lifecycle>> {
  id : Integer
  reservation : Reservation
  dateCreation : Date
  etat : EtatDemandeReservation
}

class Reservation <<entity>> {
  id : Integer
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  motif : String
  demandeur : Compte
  salle : Salle
  statut : EtatReservation
}

class Salle <<entity>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean

  equipements : Liste<Equipement>
}

class Compte <<entity>> {
  id : Integer
  login : String
  mail : String
}

enum EtatDemandeReservation {
  CREEE
  VALIDEE
  REJETEE
}

ListeDemandesReservationUI ..> ServiceReservation
ServiceReservation ..> DemandeReservation
DemandeReservation --> Reservation : > reservation
DemandeReservation -> EtatDemandeReservation : > etat
Reservation -> Compte : > demandeur
Reservation -> Salle : > salle

@enduml
```

##### Cas nominal

```plantuml
@startuml
skin rose

actor Responsable as r
boundary ListeDemandesReservationUI as ui
control ServiceReservation as svc
participant DemandeReservation as dr <<lifecycle>>
entity Reservation as res
entity Salle as salle

r -> ui : listerDemandesEnAttente()
ui -> svc : listerDemandesEnAttente()
svc -> dr : findByEtat(CREEE)
dr --> svc : liste des demandes
svc -> dr : chargerReservation()
dr --> svc : reservation completee
svc -> res : chargerSalle()
res --> svc : salle
svc --> ui : liste des demandes completees
ui --> r : affiche la liste
@enduml
```

#### 4.3.8. Valider une demande de réservation

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireValidationDemande <<boundary>> {
  demande : DemandeReservation
  reservation : Reservation
  validationsAccompliees : Integer
  validateurActuel : Compte
}

class ServiceReservation <<control>> {
  validerDemande()
  verifierConditionValidation()
  mettreAJourStatutReservation()
}

class DemandeReservation <<lifecycle>> {
  id : Integer
  reservation : Reservation
  dateCreation : Date
  etat : EtatDemandeReservation

  validerDemande()
}

class Reservation <<entity>> {
  id : Integer
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  motif : String
  demandeur : Compte
  salle : Salle
  statut : EtatReservation
}

class Compte <<entity>> {
  id : Integer
  login : String
  mail : String
}

class Salle <<entity>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean

  equipements : Liste<Equipement>
}

class ServiceNotification <<boundary>> {
  envoyerNotificationValidation()
}

FormulaireValidationDemande ..> ServiceReservation
ServiceReservation ..> DemandeReservation
ServiceReservation ..> ServiceNotification
DemandeReservation --> Reservation : > reservation
Reservation -> Compte : > demandeur
Reservation -> Salle : > salle

@enduml
```

##### Séquence

```plantuml
@startuml
skin rose

actor Responsable as r
boundary FormulaireValidationDemande as ui
control ServiceReservation as svc
participant DemandeReservation as dr <<lifecycle>>
entity Reservation as res
entity Compte as demandeur
control ServiceNotification as notif

r -> ui : chargerDemande(demande)
ui -> ui : afficherDetailsDemande()
r -> ui : validerDemande()
ui -> svc : validerDemande(demande, validateur)
svc -> svc : verifierConditionValidation(demande)
svc -> dr : validerDemande()
note right : etat = VALIDEE
svc -> res : mettreAJourStatut(EN_ATTENTE -> CONFIRMEE)
note right : statut = CONFIRMEE
svc -> notif : preparerNotificationValidation(reservation, demandeur)
notif --> svc : notification preparée
svc -> notif : envoyerNotificationValidation(demandeur, reservation)
svc --> ui : confirmation
ui --> r : demande validée
@enduml
```

#### 4.3.9. Rejeter une demande de réservation

##### Classes candidates

```plantuml
@startuml
skin rose

class FormulaireRejetDemande <<boundary>> {
  demande : DemandeReservation
  reservation : Reservation
  motifRejet : String
  confirmation : Boolean
}

FormulaireRejetDemande ..> ServiceReservation

@enduml
```

Les classes `ServiceReservation`, `DemandeReservation`, `Reservation` et `ServiceNotification` sont définies dans les sections précédentes.

##### Séquence

```plantuml
@startuml
skin rose

actor Responsable as r
boundary FormulaireRejetDemande as ui
control ServiceReservation as svc
participant DemandeReservation as dr <<lifecycle>>
entity Reservation as res
entity Compte as demandeur
control ServiceNotification as notif

r -> ui : chargerDemande(demande)
ui -> ui : afficherDetailsDemande()
r -> ui : setMotifRejet()
r -> ui : confirmerRejet()
ui -> svc : rejeterDemande(demande, motifRejet)
svc -> svc : verifierPossibiliteRejet(demande)
svc -> dr : obtenirEtat()
dr --> svc : etat = CREEE
svc -> dr : rejeterDemande(motifRejet)
note right : etat = REJETEE
svc -> res : mettreAJourStatut(EN_ATTENTE -> REFUSEE)
note right : statut = REFUSEE
svc -> notif : preparerNotificationRefus(reservation, demandeur, motifRejet)
notif --> svc : notification preparée
svc -> notif : envoyerNotificationRefus(demandeur, reservation, motifRejet)
svc --> ui : confirmation
ui --> r : demande rejetée
@enduml
```

## 5. Regroupement des classes

### 5.1. Groupe domaine

```plantuml
@startuml
skin rose

class Salle <<entity>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean

  equipements : Liste<Equipement>
}

class Equipement <<entity>> {
  id : Integer
  nom : String
  description : String
}

class PlageDisponibilite <<lifecycle>> {
  id : Integer
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  statut : StatutPlage
  salle : Salle
}

class Reservation <<entity>> {
  id : Integer
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  motif : String
  demandeur : Compte
  salle : Salle
  statut : EtatReservation
}

class Compte <<entity>> {
  id : Integer
  login : String
  mail : String
}

class Heure <<value>> {
  heure : Integer
  minute : Integer
}

class Filtre <<value>> {
  nom : String
  localisation : String
  capaciteMin : Integer
  capaciteMax : Integer
  type : TypeSalle
  equipements : Liste<String>
  disponibilite : Boolean
  dateDebut : Date
  dateFin : Date
  heureDebut : Heure
  heureFin : Heure
}

class FiltreRechercheSalle <<entity>> {
  creerFiltre(filtre : Filtre)
  appliquerFiltre(salle : Salle) : Boolean
}

enum TypeSalle {
  COURS
  TP
  REUNION
  AMPHI
}

enum StatutPlage {
  ACTIVE
  INACTIVE
}

enum EtatReservation {
  EN_ATTENTE
  CONFIRMEE
  REFUSEE
  ANNULEE
}

Salle o-- "*" Equipement
PlageDisponibilite -> Salle : > salle
Reservation -> Compte : > demandeur
Reservation -> Salle : > salle
Reservation -> EtatReservation : > statut
Salle -> TypeSalle : > type
PlageDisponibilite -> StatutPlage : > statut
PlageDisponibilite --> Heure : > heureDebut
PlageDisponibilite --> Heure : > heureFin
FiltreRechercheSalle ..> Filtre
FiltreRechercheSalle ..> Salle : > appliquerFiltre

@enduml
```

### 5.2. Groupe domaine et cycle de vie

```plantuml
@startuml
skin rose

class DemandeReservation <<lifecycle>> {
  id : Integer
  reservation : Reservation
  dateCreation : Date
  etat : EtatDemandeReservation
}

enum EtatDemandeReservation {
  CREEE
  VALIDEE
  REJETEE
}

class DemandeCreationCompte <<lifecycle>> {
  login : String
  motDePasse : String
  mail : String
  etat : EtatDemande
  dateCreation : Date
}

enum EtatDemande {
  CREEE
  MAIL_ENVOYE
  MAIL_VALIDE
  VALIDEE
  REFUSEE
}

DemandeReservation -> EtatDemandeReservation : > etat
DemandeCreationCompte -> EtatDemande : > etat

@enduml
```

### 5.3. Groupe Service

```plantuml
@startuml
skin rose

class ServiceCompte <<control>> {
  validerDonnees()
  verifierLoginDisponible()
  creerDemande()
  validerDemande()
  rejeterDemande()
  modifierCompte()
  modifierMotDePasse()
  demanderReinitialisation()
  reinitialiserMotDePasse()
}

class ServiceSalle <<control>> {
  listerSalles()
  rechercherSalles(filtre : Filtre)
  creerSalle()
  modifierSalle()
  supprimerSalle()
  creerEquipement()
  ajouterEquipement()
  modifierEquipement()
  supprimerEquipement()
  ajouterPlageDisponibilite()
  modifierPlageDisponibilite()
  supprimerPlageDisponibilite()
  verifierPossibiliteSuppression()
}

class ServiceReservation <<control>> {
  listerDemandesEnAttente()
  filtrerDemandes()
  creerReservation()
  consulterReservation()
  modifierReservation()
  annulerReservation()
  validerDemande()
  rejeterDemande()
  verifierDisponibilite()
  verifierConditionValidation()
  envoyerNotification()
}

class ServiceNotification <<boundary>> {
  envoyerMailConfirmation()
  envoyerMailValidation()
  envoyerMailRefus()
  envoyerMailReinitialisation()
  preparerNotificationValidation()
  envoyerNotificationValidation()
  preparerNotificationRefus()
  envoyerNotificationRefus()
  preparerNotificationAnnulation()
  envoyerNotificationAnnulation()
}

class ServiceAuth <<control>> {
  authentifier(login : String, motDePasse : String)
  genererToken(compte : Compte)
}

@enduml
```

### 5.4. Groupe interface utilisateur et système

```plantuml
@startuml
skin rose

class FormulaireConnexion <<boundary>> {
  login : String
  motDePasse : String
}

class ListeDemandesCompteUI <<boundary>> {}

class FormulaireDemandeCompte <<boundary>> {
  login : String
  motDePasse : String
  motDePasseConfirmation : String
  mail : String
}

class FormulaireModificationCompte <<boundary>> {
  login : String
  mail : String
}

class FormulaireModificationMotDePasse <<boundary>> {
  motDePasseActuel : String
  nouveauMotDePasse : String
  nouveauMotDePasseConfirmation : String
}

class FormulaireDemandeReinitialisation <<boundary>> {
  mail : String
}

class FormulaireNouveauMotDePasse <<boundary>> {
  nouveauMotDePasse : String
  nouveauMotDePasseConfirmation : String
}

class FormulaireCreationEquipement <<boundary>> {
  nom : String
  description : String
  salle : Salle
}

class FormulaireDisponibiliteSalle <<boundary>> {
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  salle : Salle
}

class ListeSallesUI <<boundary>> {
  sallesAffichees : Liste<Salle>
  filtre : Filtre
}

class ListeSallesReservationUI <<boundary>> {
  sallesAffichees : Liste<Salle>
  filtre : Filtre
}

class ListeDemandesReservationUI <<boundary>> {
  demandesAffichees : Liste<DemandeReservation>
  filtreStatut : EtatDemandeReservation
}

class FormulaireCreationSalle <<boundary>> {
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean
}

class FormulaireSuppressionSalle <<boundary>> {
  salle : Salle
  confirmation : Boolean
}

class FormulaireModificationSalle <<boundary>> {
  id : Integer
  nom : String
  localisation : String
  capacite : Integer
  description : String
  type : TypeSalle
  reservationAvecValidation : Boolean
}

class FormulaireModificationEquipement <<boundary>> {
  id : Integer
  nom : String
  description : String
}

class FormulaireSuppressionEquipement <<boundary>> {
  equipement : Equipement
  confirmation : Boolean
}

class FormulaireModificationDisponibilite <<boundary>> {
  id : Integer
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  statut : StatutPlage
}

class FormulaireSuppressionDisponibilite <<boundary>> {
  plage : PlageDisponibilite
  salle : Salle
  confirmation : Boolean
}

class FormulaireRechercheSalle <<boundary>> {
  nom : String
  localisation : String
  capaciteMin : Integer
  capaciteMax : Integer
  type : TypeSalle
  equipements : Liste<String>
  disponibilite : Boolean
  dateDebut : Date
  dateFin : Date
  heureDebut : Heure
  heureFin : Heure
}

class FormulaireReservationSalle <<boundary>> {
  salle : Salle
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  motif : String
  demandeur : Compte
}

class FormulaireConsultationReservation <<boundary>> {
  reservation : Reservation
  detailAffiche : Boolean
}

class FormulaireModificationReservation <<boundary>> {
  idReservation : Integer
  dateDebut : Date
  heureDebut : Heure
  dateFin : Date
  heureFin : Heure
  motif : String
}

class FormulaireAnnulationReservation <<boundary>> {
  reservation : Reservation
  motifAnnulation : String
  confirmation : Boolean
}

class FormulaireValidationDemande <<boundary>> {
  demande : DemandeReservation
  reservation : Reservation
  validationsAccompliees : Integer
  validateurActuel : Compte
}

class FormulaireRejetDemande <<boundary>> {
  demande : DemandeReservation
  reservation : Reservation
  motifRejet : String
  confirmation : Boolean
}

@enduml
```
