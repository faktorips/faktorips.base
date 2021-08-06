# Faktor-IPS Archetype

This guide describes the basics, that have to be known in order to generate 
Faktor-IPS projects as Maven projects by using Maven-Archetypes.

## Table of contents
1. [Development](#development)
2. [User guide](#user-guide)
   
    a. [English](#english)
    
    b. [German](#german)

## Development

### Usage of static, templated files
The required files for creating archetypes are static and only adjustable by templates. 
Because of this, they have to be adjusted if Faktor-IPS changes.

There are two types of templates:

- replaced by ``mvn archetype:generate`` (format: _\${value}_)
- replaced by the post-processor groovy script (format: _$value$_)

### MANIFEST.MF
Take care that the MANIFEST.MF template ends with an empty line.
If that is not the case, the Maven-Jar-Plugin will build faulty JARs.

## User guide

### English

#### Requirements for using Archetypes
- Install the latest Faktor-IPS version
- Install Maven (at least version 3.5.0)

#### Adjustable parameters for creating an archetype

``mvn archetype:generate``:
- **groupId**: <br/> 
  the Group-ID of the Maven project
- **artifactId**: <br/> 
  the Artifact-ID of the Maven project
- **version**: <br/> 
  the version of the Maven project
- **package**: <br/> 
  the base package, e.g. _org.faktorips.example_
- **JavaVersion**: <br/> 
  the used Java version, e.g. _1.8_ or _11_
- **IPS-Language**: <br/> 
  the used language package (_en_ or _de_)
- **IPS-NamingConvention**: <br/> 
  the used naming convention, e.g. _FIPS_, _VAA_ or _PM_
- **IPS-IsModelProject**: <br/>
  _true_, if it is a model project, else _false_
- **IPS-IsProductDefinitionProject**: <br/>
  _true_, if it es a product definition project, else _false_
- **IPS-IsPersistenceSupport**: <br/>
  _true_, if persistence is supported, else _false_
- **IPS-TocXML**: <br/> 
  the name of the repository-TOC excluding the file format, e.g. _faktorips-repository-toc_
- **IPS-ValidationMessageBundle**: <br/> 
  the name of the used Validation-Message-Bundle, e.g. _validation-message_
- **IPS-SourceFolder**: <br/> 
  the source folder, e.g.: _model_
- **IPS-IsGroovySupport**: <br/>
  _true_, if Faktor-IPS Groovy should be supported, else _false_
- **runtime-ID-prefix**: <br/>
  the used runtime-ID-prefix, e.g. _lineOfBusiness._ 
  (**important**: do not forget the dot at the end)
- **IPS-ConfigureIpsBuild**: <br/>
  _true_, if the faktorips-maven-plugin should be configured to build the project, else _false_

_Postprocessor_:
- _(optional)_ **Persistence API** <br/>
  the used technology for implementing persistence support, 
  e.g. _EclipseLink 2.5_, _Generic JPA 2.0_ or _Generic JPA 2.1_ 
  (requires: _IPS-IsPersistenceSupport=true_)

#### Execution: 

1. Installation of the archetype (only for local applications):	
    1. Navigate to: **faktorips.base/org.faktorips.archetype**
    2. Execute: <br/> 
       ``mvn clean install``
2. Generation of the archetype:		
    1. Execute in another folder then the archetype-folder: <br/>
       ``mvn archetype:generate -DarchetypeGroupId=org.faktorips -DarchetypeArtifactId=faktorips-maven-archetype -DarchetypeVersion=<version>``
    2. Insert the described [parameters](#adjustable-parameters-for-creating-an-archetype)
3. Installation of the generated project:		
    1. Navigate to the generated Maven project
    2. Execute:	<br/>	
       ``mvn clean install``
       
#### Warning: Does not work in Eclipse

Due to a [bug in Eclipse](https://github.com/eclipse-m2e/m2e-core/issues/249) the archetype can currently only be used from the command line. The created project can then be imported in Eclipse.

### German

#### Voraussetzungen zur Verwendung
- Installation der neuesten Version von Faktor-IPS
- Installation von Maven (mindestens Version 3.5.0)

#### Setzbare Parameter bei der Erstellung eines Archetypes

``mvn:generate``:
- **groupId**: <br/>
  die Group-ID des Maven-Projekts
- **artifactId**: <br/>
  die Artifact-ID des Maven-Projekts
- **version**: <br/>
  die Version des Maven-Projekts
- **package**: <br/>
  die grundlegende Package Struktur, z.B. _org.faktorips.example_
- **JavaVersion**: <br/>
  die Java-Version, z.B. _1.8_ or _11_
- **IPS-Language**: <br/>
  das verwendete Sprachpaket (_en_ or _de_)
- **IPS-NamingConvention**: <br/>
  die verwendete Namenskonvention (_FIPS_, _VAA_ or _PM_)
- **IPS-IsModelProject**: <br/>
  _true_, wenn es ein Model-Projekt ist, sonst _false_
- **IPS-IsProductDefinitionProject**: <br/>
  _true_, wenn es ein ProduKt-Definition-Projekt ist, sonst _false_
- **IPS-IsPersistenceSupport**: <br/>
  _true_, wenn wenn Persistenz unterstützt werden soll, sonst _false_
- **IPS-TocXML**: <br/>
  der Dateiname des Repository-TOC ohne Dateiendung, z.B. _faktorips-repository-toc_
- **IPS-ValidationMessageBundle**: <br/>
  die Bezeichnung des zu verwendenden Validation-Message-Bundles, z.B. _validation-message_
- **IPS-SourceFolder**: <br/>
  der Source-Folder, z.B. _modell_
- **IPS-IsGroovySupport**: <br/>
  _true_, wenn Faktor-IPS Groovy unterstützt werden soll, sonst _false_
- **runtime-ID-prefix**: <br/>
  die verwendete Laufzeit-ID-Prefix, z.B. _hausrat._ 
  (**wichtig**: Punkt am Ende nicht vergessen)
- **IPS-ConfigureIpsBuild**: <br/>
  _true_, wenn das faktorips-maven-plugin zum Bauen des Projekts konfiguriert werden soll, sonst _false_
  
_Postprozessor_:
- _(optional)_ **Persistenz API** <br/>
  die zu verwendende Technologie zur Umsetzung der Persistenz,
  z.B. _EclipseLink 2.5_, _Generic JPA 2.0_ oder _Generic JPA 2.1_
  (nur, wenn _IPS-IsPersistenceSupport=true_)

#### Durchführung

1. Installation des Archetypes (nur für lokale Applikationen):
    1. Navigieren zu: **faktorips.base/org.faktorips.archetype**
    2. Ausführen: <br/>
       ``mvn clean install``
2. Generation of the archetype:
    1. Ausführen in einem anderen Ordner als dem Archetype-Ordner: <br/>
       ``mvn archetype:generate -DarchetypeGroupId=org.faktorips -DarchetypeArtifactId=faktorips-maven-archetype -DarchetypeVersion=<version>``
    2. Einfügen der beschriebenen [Parameter](#setzbare-parameter-bei-der-erstellung-eines-archetypes)
3. Installation des generierten Projekts:
    1. Navigieren in das generierte Maven-Projekt
    2. Ausführen:	<br/>
       ``mvn clean install``
       
#### Warnung: Funktioniert nicht in Eclipse

Wegen eines [Bugs in Eclipse](https://github.com/eclipse-m2e/m2e-core/issues/249) kann der Archetyp derzeit nur von der Kommandozeile ausgeführt werden. Das erstellte Projekt kann dann in Eclipse importiert werden.
       