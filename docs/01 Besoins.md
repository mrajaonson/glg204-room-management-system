# Projet *Software Tickets* : Expression des besoins V 1.1


- [1. Objectif du document](#1-objectif-du-document)
- [2. Présentation](#2-présentation)
    - [2.1. Présentation du projet](#21-présentation-du-projet)
    - [2.2. Situation actuelle](#22-situation-actuelle)
    - [2.3. Les contraintes](#23-les-contraintes)
    - [2.4. Présentation de la société](#24-présentation-de-la-société)
- [3. Acteurs](#3-acteurs)
    - [3.1. Client](#31-client)
    - [3.2. Programmeur](#32-programmeur)
    - [3.3. Manager](#33-manager)
    - [3.4. Administrateur](#34-administrateur)
    - [3.5. Ordonnanceur](#35-ordonnanceur)
    - [3.6. Résumé des Acteurs](#36-résumé-des-acteurs)
- [4. Cas d’utilisation](#4-cas-dutilisation)
    - [4.1. Groupe 1 : Gestion des comptes](#41-groupe-1--gestion-des-comptes)
        - [4.1.1. Cas d'utilisation « Créer Compte »](#411-cas-dutilisation--créer-compte-)
        - [4.1.2. Cas d'utilisation « Se connecter »](#412-cas-dutilisation--se-connecter-)
    - [4.2. Groupe 2 : Gestion des tickets](#42-groupe-2--gestion-des-tickets)
        - [4.2.1. Créer un ticket](#421-créer-un-ticket)
        - [4.2.2. Visualiser un ticket](#422-visualiser-un-ticket)
        - [4.2.3. Valider un ticket](#423-valider-un-ticket)
        - [4.2.4. Créer un rapport d'avancement](#424-créer-un-rapport-davancement)
        - [4.2.5. Clore un ticket](#425-clore-un-ticket)
        - [4.2.6. Déléguer un ticket](#426-déléguer-un-ticket)
        - [4.2.7. Accepter ou refuser un ticket](#427-accepter-ou-refuser-un-ticket)
        - [4.2.8. Consulter le tableau de bord / Client](#428-consulter-le-tableau-de-bord--client)
        - [4.2.9. Consulter le tableau de bord / Manager](#429-consulter-le-tableau-de-bord--manager)
        - [4.2.10. Consulter le tableau de bord / Programmeur](#4210-consulter-le-tableau-de-bord--programmeur)
        - [4.2.10. Consulter le tableau de bord / Client](#4210-consulter-le-tableau-de-bord--client)
        - [4.2.11. Envoyer rappels](#4211-envoyer-rappels)
- [5. Annexes](#5-annexes)
    - [5.1. Terminologie](#51-terminologie)
    - [5.2. Historique](#52-historique)


## 1. Objectif du document

Ce document contient l'expression des besoins du projet **Software Tickets**.

Les besoins ont été exprimés selon le langage de modélisation UML. Les différentes catégories d'usagers du système ont été classés en différents types d' « acteurs ». Les interactions entre les usagers et le système ont été découpées en diagrammes de « cas d'utilisation » (use cases), chaque cas d'utilisation ayant à son tour un diagramme « d'activités » qui permet d'en modéliser la dynamique.

## 2. Présentation

### 2.1. Présentation du projet

**NiceSoft** édite un logiciel de publication assistée par ordinateur, et souhaite pouvoir permettre à ses clients de signaler des bugs et de faire des demandes de développements. Les clients en question sont enregistrés et l’entreprise leur est liée par contrat.

Il s’agit de créer un logiciel permettant :
-	à des clients de faire une demande d’intervention sur le logiciel (un « ticket ») ;
-	les utilisateurs peuvent ouvrir un ticket, qui peut être un signalement de bug ou une demande de fonctionnalité ;
-	le manager va faire un premier filtre, et acceptera ou rejettera le ticket. S'il l'accepte, il le confie à un programmeur ;
-	le programmeur va expliciter son avancement sur le ticket ;
-	un programmeur peut éventuellement essayer de confier un ticket à un autre programmeur, qui peut accepter ou refuser (ça n'est pas possible quand c'est le manager qui décide).
-	le client et/ou le manager sont avertis des modifications ;
-	si un ticket non clôt est inactif depuis plus d'une semaine, un message est envoyé au manager et au programmeur.

### 2.2. Situation actuelle
(rien n’existe)

### 2.3. Les contraintes

- Application web
- (pas grand-chose d’autre)

### 2.4. Présentation de la société

aucun intérêt

## 3. Acteurs

### 3.1. Client
Il s’agit d’un utilisateur enregistré du logiciel de PAO.  On connaît en particulier son mail, ce qui permet de le contacter simplement. Il a un compte enregistré quelque part. Dans un premier temps, on lui créera un compte spécifique pour SoftwareTicket.

### 3.2. Programmeur
C’est un développeur de SoftwareTicket ; il va prendre en charge un ticket accepté et renseigner les informations sur son avancement

### 3.3. Manager
C’est un employé de SoftwareTicket qui va pouvoir déléguer le travail à un développeur.

### 3.4. Administrateur

Employé responsable de la création des comptes.

### 3.5. Ordonnanceur

Système qui déclenche les tâches automatiques.

### 3.6. Résumé des Acteurs

~~~plantuml
@startuml
skin rose
:client:
:programmeur:
:manager:
:administrateur:
@enduml
~~~


## 4. Cas d’utilisation


### 4.1. Groupe 1 : Gestion des comptes

*normalement, ce groupe devrait être plus étendu. On le rend très très sommaire*

~~~plantuml
@startuml
skin rose
:administrateur: -> (créer compte)

:utilisateur: <|-- :client:

:utilisateur: <|-- :programmeur: 

:utilisateur: <|-- :manager: 

:utilisateur: <|-- :administrateur:

:utilisateur: -> (se connecter)

(créer compte) <.. :utilisateur:
@enduml
~~~

#### 4.1.1. Cas d'utilisation « Créer Compte »

**remarque** : dans la description des déroulements alternatifs, on peut aussi donner à chaque fois la description complète, au lieu de n'en reprendre qu'une partie.

##### Résumé
L'administrateur crée un compte

##### Acteurs
- un administrateur

##### Pré-conditions
- l'administrateur est connecté

##### Description
1. l'administrateur saisit
    - le login du compte
    - le mot de passe du compte (en double)
    - son type (manager,employé, client)
    - le mail associé
2. le système vérifie :
    - que le login n'est pas vide ;
    - que les mots de passe ne sont pas vides ;
    - que les deux mots de passe saisis  coïncident ;
    - que le mail est bien formé.
3. le système vérifie que le compte n'existe pas déjà
4. si le login n'est pas vide et que le compte n'existe pas déjà, le compte est créé ; un mail est envoyé à l'utilisateur, l'informant de son mot de passe.

##### Déroulement alternatif : données incorrectes

- 2.1 le login, le mot de passe ou le mail ne sont pas bien formés ;
- 2.2 le système affiche un message et l'utilisateur peut corriger

##### Déroulement alternatif : le compte existe déjà

- 3.1 le compte existe déjà
- 3.2 le système affiche un message

##### Post-conditions
- le compte est créé et l'utilisateur est averti

##### Exceptions

- La sauvegarde de l'utilisateur échoue (par exemple BD non disponible) : le système doit être remis dans l'état antérieur.

##### Diagramme d'activité

*diagramme d'un intérêt discutable*

~~~plantuml
@startuml
  skin rose
  title création d'un compte
  start  
  repeat :saisie des données;
  repeat while (données correctes?) is (non) not (oui)
    if (login existant?) then (non)
      :créer compte;
      :prévenir utilisateur;
      stop
    else (oui)
      :message d'erreur;
      end
    endif  
@enduml
~~~


##### Maquettes d'écran

C'est prématuré, et ici pas très utile... mais c'est juste pour montrer qu'on peut le faire avec **plantuml** :

~~~plantuml
@startsalt
{
  Identifiant | "turing"
  Mot de passe (1) | "***          "
  Mot de passe (2) | "***          "
  Courriel         | "turing@cambridge.uk"
  Type compte      | ^client             ^
  [créer compte]
}
@endsalt
~~~

##### Remarques

- Qui crée le compte administrateur ? L'idée est qu'il soit créé par défaut ; on ajoutera plus tard une fonctionnalité pour changer son mot de passe.

- Que se passe-t-il si l'email est bien formé mais incorrect ? Il pourrait être utile d'envisager une procédure de validation des comptes.


#### 4.1.2. Cas d'utilisation « Se connecter »

##### Résumé
Un utilisateur s'authentifie et est connecté

##### Acteurs
un utilisateur quelconque

##### Pré-conditions
L'utilisateur a été créé

##### Description

1. l'utilisateur entre son *login* et son mot de passe
2. le système vérifie que l'utilisateur existe
3. le système vérifie que le mot de passe est correct

##### Déroulement alternatif : utilisateur inexistant

2.1 l'utilisateur n'existe pas
2.2  le système prévient celui-ci qu'il n'a pas pu le connecter (sans plus d'information)

##### Déroulement alternatif : mot de passe incorrect

3.1 le mot de passe est incorrect
3.2 le système prévient l'utilisateur qu'il n'a pas pu le connecter (sans plus d'information)

##### Exceptions

En cas de panne technique, l'utilisateur est averti.

##### Post-conditions

En cas de succès du scénario nominal, l'utilisateur est connecté, avec les droits relatifs à son compte.

##### Remarques

- Il est important de ne pas spécifier à l'utilisateur la raison pour laquelle il n'a pas pu se connecter (compte inexistant **ou** mot de passe incorrect)
- il sera souhaitable (mais non prioritaire) de *logger* les tentatives (sans les mots de passe, bien entendu) pour aider au support et à la sécurité.

##### Diagramme d'activité

(optionnel)

~~~plantuml
@startuml
skin rose
start
:saisie login;
:saisie mot de passe;
if (login correct) then (oui)
  if (mot de passe correct) then (oui)
    :utilisateur connecté;
    end
  else (non)
    :message d'erreur;
    stop
  endif
else (non)
  :message d'erreur;
  stop
endif
@enduml
~~~


### 4.2. Groupe 2 : Gestion des tickets


~~~plantuml
@startuml
skin rose
left to right direction
title gestion des tickets
:client:  --> (créer un ticket)
:client:  --> (visualiser un ticket)
:programmeur:  --> (visualiser un ticket)
:manager:  --> (visualiser un ticket)

rectangle "gestion des utilisateurs" {
  (se connecter)
}
actor :ordonnanceur: <<système>>

(créer un ticket) ..> (se connecter) : include

:manager: --> (valider un ticket)

:client: <.. (valider un ticket)
(valider un ticket) ..> (se connecter) : include

usecase doc as "créer un rapport d'avancement"

doc ..> (se connecter) : include

:programmeur: --> doc
:programmeur: --> (clore un ticket)
:programmeur: --> (déléguer un ticket)
:programmeur: --> (accepter ou refuser un ticket)



(clore un ticket) ..> (se connecter) : include
(clore un ticket) ..> doc : extends

(envoyer rappel) <-- :ordonnanceur:

 :programmeur: <.. (envoyer rappel)

 :programmeur:  --> (consulter tableau de bord programmeur)
 :manager:  --> (consulter tableau de bord manager)
 :client:  --> (consulter tableau de bord client)

@enduml
~~~

*Note : il y a une version **Visual Paradigm** de ce schéma*

#### 4.2.1. Créer un ticket
##### Résumé

Un **Client** se connecte et signale, soit un bug, soit fait une demande d'amélioration pour le logiciel.

##### Acteurs

- un **Client**.

##### Pré-conditions
Le **Client** est connecté.

##### Description

- 1. le client sélectionne le type de ticket (bug/amélioration) qu'il souhaite ;
- 2. il donne un titre au ticket ;
- 3. il écrit une description du ticket.
- 4. le système vérifie que le ticket a un titre et une description
- 5. le ticket est enregistré

##### Post-conditions

- le ticket est enregistré dans l'état "à valider"

##### Remarques

- dans un second temps, l'ajout de fichiers (dump de log d'erreur ou photo d'écrans par exemple) pourrait être utiles.


#### 4.2.2. Visualiser un ticket

##### Résumé
Un utilisateur visualise les informations d'un ticket

##### Acteurs

Un utilisateur ayant accès à un ticket :

- un manager (a accès à tous les tickets)
- un programmeur : a accès à un ticket qu'il gère ;
- un client : a accès à un ticket qu'il a créé.

##### Pré-conditions
voir acteurs

##### Description

- 1. le système liste les tickets auxquels l'utilisateur a accès
- 2. il en choisit un
- 3. le système affiche toutes les informations sur le ticket :
    - son titre
    - sa description,
    - le client qui l'a créé,
    - sa date de création,
    - son statut,
    - le premier programmeur en charge du ticket,
    - sa date de validation ou d'invalidation ;
    - s'il a été invalidé, le texte correspondant ;
    - la liste, dans l'ordre chronologique, des actions sur le ticket :
        - rapports d'avancement
            - texte écrit par le développeur
            - nom du développeur
            - date
            - clôture éventuelle
        - demande de délégation (non affichée au client)
        - refus d'une demande de délégation (non affichée au client)
        - acceptation d'une demande de délégation (vue par le client comme un simple changement de programmeur)

#### 4.2.3. Valider un ticket
##### Résumé
Un manager valide ou invalide un nouveau ticket
##### Acteurs

Un manager.
##### Pré-conditions
Il existe un ticket à valider.

##### Description

- 1. le manager visualise la liste des nouveaux tickets à valider ;
- 2. il en choisit un ;
- 3. il consulte sa description ;
- 4. il choisit le programmeur en charge du ticket ;
- 5. le ticket passe à l'état validé ; l'information est enregistrée (et datée), avec en particulier le lien vers le programmeur choisi ;
- 6. le client est prévenu
##### Post-conditions
Le ticket est validé, et le programmeur verra qu'il doit le traiter.
##### Déroulement alternatifs...
- 3.1 le manager décide de refuser le ticket
- 3.2 il écrit un message pour l'expliquer
- 3.3 le message est enregistré dans l'historique du ticket
- 3.4 le client est prévenu
- 3.5 le ticket est clos.

#### 4.2.4. Créer un rapport d'avancement
##### Résumé
Un programmeur documente son avancement sur un ticket qu'il traite.
##### Acteurs
un programmeur
##### Pré-conditions
un ticket non clos est assigné à ce programmeur.
##### Description
- 1. le programmeur choisit un ticket à documenter
- 2. il entre les informations sur son action sous forme textuelle ;
- 3. ces informations sont ajoutées à l'historique du ticket (avec la date) ;
- 4. (point d'extension) : il peut décider de clore le ticket.

##### Post-conditions
Les informations sur la progression du ticket sont enregistrées dans son historique ; elles seront consultables par le client.

#### 4.2.5. Clore un ticket

##### Résumé
Un programmeur va clore un ticket
##### Acteurs
un programmeur
##### Pré-conditions
le programmeur est en charge du ticket
##### Description
- 1. le programmeur a créer un rapport d'avancement pour le ticket
- 2. il le marque comme clos
- 3. le ticket est clos ; la date de clôture est enregistrée.
##### Post-conditions

- Le ticket n'apparaît plus dans la liste des tickets du programmeur.
- l'événement est signalé au client et au manager dans leur tableau de bord

#### 4.2.6. Déléguer un ticket
##### Résumé
Un programmeur propose à un de ses collègue de prendre en charge un ticket
##### Acteurs
un programmeur

##### Pré-conditions
le programmeur est en charge d'un ticket

##### Description

- 1. il choisit un de ses collègue ;
- 2. il remplit un formulaire qui contient :
    - une partie publique qui sera ajoutée à l'historique visible par le client ;
    - une partie privée à destination uniquement interne ; elle peut être vide
- 3. il valide

##### Post-conditions

- la demande de délégation est enregistrée et ajoutée à l'historique ;
- elle sera visible sur le tableau de bord :
    - des deux programmeurs ;
    - des managers.

##### Remarque

Tant que le programmeur destinataire n'a pas accepté de gérer un ticket, ça n'est pas la peine de prévenir le client.

#### 4.2.7. Accepter ou refuser un ticket

##### Résumé

Un programmeur à qui un de ses collègues a demandé de gérer un ticket accepte de le faire

##### Acteurs

un programmeur

##### Pré-conditions
Le programmeur a reçu une demande de délégation pour un ticket

##### Description

1. il visualise l'historique du ticket ;
2. il accepte de le prendre en charge ;
3. l'information est ajoutée dans l'historique du ticket

##### Déroulement alternatif 1 : refus

2.1 il refuse de le prendre en charge ;
3. l'information est ajoutée dans l'historique du ticket

##### Post-conditions

- En cas d'acceptation :
    - le programmeur devient le gestionnaire du ticket ;
    - l'information est affichée sur les tableaux de bord du client, du programmeur qui a délégué le ticket, de celui qui l'a accepté, et du manager.
- En cas de refus :
    - l'information est affichée sur les tableaux de bord du programmeur qui a délégué le ticket, de celui qui l'a refusé, et du manager.


#### 4.2.8. Consulter le tableau de bord / Client

##### Résumé
On affiche les informations pertinentes pour le client
##### Acteurs
un Client
##### Description
- le client choisi éventuellement un ou plusieurs filtres ;
- il peut potentiellement voir tous les tickets qu'il a ouvert et leur historique (sauf pour les délégations refusées)

##### Remarques
- par défaut, on propose au client les événements récents sur les tickets qu'il a ouvert

#### 4.2.9. Consulter le tableau de bord / Manager

On a concentré le tableau de bord sur les informations minimales à afficher.

##### Résumé
On affiche les informations pertinentes pour le manager

##### Acteurs
un Manager

##### Description

Le tableau de bord affiche :
- la liste des tickets à valider ;
- la liste des tickets récemment fermés.

#### 4.2.10. Consulter le tableau de bord / Programmeur

##### Résumé
On affiche les informations pertinentes pour le programmeur

##### Acteurs
un programmeur

##### Description
- assignation d'un ticket par le manager ;
- demande de délégation par un autre développeur ;
- l'acceptation ou le refus de ses propres demandes de délégation
- rappels

#### 4.2.10. Consulter le tableau de bord / Client

##### Résumé
On affiche les informations pertinentes pour le client

##### Acteurs
un client

##### Description
- On affiche
    - les rejets ou acceptations de moins d'une semaine pour ses tickets.
    - les derniers événements sur tous ses tickets **ouverts** ;
    - les événements récents (moins d'une semaine) concernant ceux de ses tickets qui viennent d'être clôts

#### 4.2.11. Envoyer rappels

##### Résumé
En l'absence d'activité sur un ticket ouvert, un mail est expédié.
##### Acteurs
- l'ordonnanceur (le temps) : action automatique
- le système de messagerie

##### Description

- le système liste les tickets ouverts sans activité depuis plus de **n** jours (réglable dans les paramètres de l'application ?)
- il envoie un rappel au programmeur en charge du ticket.

##### Post-conditions

Le programmeur est prévenu.


## 5. Annexes
### 5.1. Terminologie

ticket
: demande faite par un client, et traitée par le logiciel


### 5.2. Historique

- lors de la rédaction des *use cases*, il est apparu que le mode de consultation des tickets était trop restrictif. Dans un premier temps, on n'envisageait de visualiser que les tickets actifs. Il est important que le programmeur et le manager puisse éventuellement voir la liste des tickets clos ;
- de même, il faut que les événements récents et pertinents soient soulignés. On introduit donc une notion de *tableau de bord* à consulter.
- V 1.1 : une première phase d'analyse a montré que la description des besoins pour le tableau de bord était un peu floue. On la précise donc.

