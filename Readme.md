# Neo Modeling Framework (NMF)
Neo Modeling Framework (NMF) is an open-source set of tools primarily designed to manipulate ultra-large datasets in the [Neo4j database](https://neo4j.com).
NMF implements Object Graph Mapping (OGM) technique that allows a remote client to operate on data directly in the remote storage.
Hence, a client application can delegate to the database handling of large amounts of data, which could potentially exceed RAM capacity.
Therefore, the client can select and load to memory (i.e., READ) from the storage only needed elements, but not an entire dataset.
Also, NMF optimizes writing operations (i.e., CREATE, UPDATE, and DELETE), previously grouping the changes in the cache and persisting them to the storage later in a transactional way.
These features optimize I/O performance while editing large datasets providing better efficiency and data scalability.

Inspired by Eclipse Modeling Framework (EMF), NMF treats any dataset in terms of Model-Driven Engineering *(MDE) model*.
EMF is the famous modeling tool for MDE software development methodology.
To find out more about EMF and MDE, please refer to:
- [A brief introduction to Model-Driven Engineering (MDE).pdf](/docs/A%20brief%20introduction%20to%20Model-Driven%20Engineering%20(MDE).pdf)
- https://www.eclipse.org/modeling/emf
- https://eclipsesource.com/blogs/tutorials/emf-tutorial

NMF endows the datasets with modeling concepts of MDE models.
Therefore, NMF editing operations follow the editing logic of MDE models.
In NMF, any model (i.e., dataset) must conform to some metamodel -- the structure that describes a model itself.
In particular, NMF relies on Ecore metamodel presented below:

<p align="center">
   <img src="/docs/meta-metamodel.svg" width="700" style="background-color:white;">
</p>

NMF aims to resolve the MDE model scalability problem by using the graph database to handle a large amount of data.
Since the structure of MDE models has a nature of a graph, we use Neo4j graph database as default storage.

## NMF architecture
NMF consists of three modules: [NMF-editor](/neo4j-io), [NMF-loader](/modelLoader), and [NMF-generator](/codeGenerator).

The modules are designed to achieve the following goals:
- NMF-loader: stores existing MDE models in the Neo4j database
- NMF-editor: performs editing operations (i.e., CREATE, UPDATE, REMOVE, READ) over the models directly in the database
- NMF-generator: provides a domain-specific API to edit the models

The overall NMF architecture within dependencies between the modules are presented in the following figure:

<p align="center">
   <img src="/docs/NMF-architecture.svg" width="500" style="background-color:white;">
</p>

## Prerequisites
Before using NMF modules a user must have:
1. A running Neo4j database instance (either local or remote) with [APOC](https://github.com/neo4j-contrib/neo4j-apoc-procedures) plugin installed. 
We recommend using [Neo4j Desktop](https://neo4j.com/download) to set up the database environment.
2. JRE (8+) installed. Run in terminal: ```java --version``` to check the jre installation.
3. Gradle (6+) build tool installed

We recommend using Intellij IDE since it provides integration for Gradle and JRE.

## NMF-editor
NMF-editor is a core module that provides an interaction with the Neo4j database. 
NMF-loader is packaged as an executable jar file and can be used as runtime dependency.
The jar can be found in the release section.
The test sandbox can be found in [NMF-editor test](neo4j-io/src/test/kotlin/InitTest.kt) file.

### Kotlin usage example
First, **configure the DB credentials as defined in** [Neo4jDBCredentials.txt](Neo4jDBCredentials.txt),

then:
```kotlin
val dbUri = DBCredentials.dbUri
val username = DBCredentials.username
val password =DBCredentials.password
val graphManager = GraphManager(dbUri, username, password)  // init a connection with the database
   
val n1 = graphManager.createNode("Node1") // n1 is a node controller
val n2 = graphManager.createNode("Node2")
val n3 = n1.reateChild("ref2", "Node3")
graphManager.saveChanges()  // commit updates to the storage

n1.createOutRef("ref1", n2) // controllers remain interactive after the commit
n1.putProperty("property", "Test property")
graphManager.saveChanges()  // commit new changes

n1.remove()  // remove the node 'n1' within its child 'n3' (cascade delete)
graphManager.saveChanges()
graphManager.close()
```

The modification operations are applied in a transactional way on ```graphManager.saveChanges()``` function invocation.

## NMF-loader
This module provides a model storing facility. NMF-loader can export an existing MDE model provided in XMI format into the Neo4j database. 
The input model must be provided in a file. Only ```*.xmi``` and ```*.ecore``` file formats are supported.

### Usage
NMF-loader is packaged as an executable jar file and can be used from a command line. 
Download the latest [NMF-loader](https://github.com/geodes-sms/NeoModelingFramework/releases/tag/v1.0) release and run the following command in terminal:
```java -jar <NMF_LOADER_PATH> --help```. The output should be as follows:
```bash
java -jar <NMF_LOADER_PATH> --help
  -h,--host <HOST:PORT>   Database host address with port used to create bolt connection. Example: -h 127.0.0.1:7687
  -m,--model <PATH>       path to model file to be loaded
  -u,--user <arg>         Database auth: username
  -p,--password <arg>     Database auth: password
```

NMF-loader requires 4 parameters: 
- Database host (-h) address of Neo4j database to establish a connection with. Both local and remote addresses are supported.
By default, Neo4j local installation is on `http://127.0.0.1:7687` 
- Database credentials: a username (-u) and a password (-p)
- Model path (-m): an actual location of the model to load

NMF-loader can proceed both model instances and metamodels (a model of any level M_i according to [model-hierarchy](docs/metalevels.svg)). 
To correctly process a model of level M_1, NMF-loader requires a metamodel the model conforms to.
For that, a model instance stored in XMI format must have linked its metamodel location in the xmi header as follows:
```
...
xsi:schemaLocation="EPACKAGE_NAME PATH_TO_METAMODEL"
...
```

An example list of valid models can be found at [MDE models examples](/EmfModel/instance) directory.

## NMF-generator
NMF-generator is a code generation facility. It produces a set of Kotlin classes (domain-specific API) for editing a specific model.
Unlike a generic API of NMF-editor, the produced API is conceptually closer to the domain rather than to data.
The generated API relies on NMF-editor to interact with data in the Neo4j database.

### Usage
NMF-loader is packaged as an executable jar file and can be used as runtime dependency. 
NMF-generator takes a metamodel in Ecore format as an input and produces a set of files with Kotlin code.
By default, NMF-generator outputs the result API in [editor](/modelEditor) directory which represents a module with preconfigured dependencies.

```bash
java -jar <NMF_LOADER_PATH> --help
  -mm   Path of the input metamodel
  -o,--output   Output dirrectory
```

[Example of the generated domain-specific API](/modelEditor/src/main/kotlin/geodes/sms/nmf/editor)

[Metamodels examples](/EmfModel/metamodel)

# Empirical Evaluation

The quantitative evaluation can be re-run by running [RQ1Eval.kt](modelLoader/src/test/kotlin/evaluation) and [RQ2Eval.kt](modelEditor/src/test/kotlin/evaluation) files. Make sure you have an empty instance of Neo4j running.
Results will be generated as CSV files, under [Evaluation/results](Evaluation/results).

Results can be plotted by running the Jupyter Notebooks at [Evaluation/analysis](Evaluation/analysis).

Please note that due to differences in hardware, re-running the experiments will probably generate slightly different results than those reported in the paper. 

