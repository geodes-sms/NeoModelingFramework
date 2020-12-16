# Neo Modeling Framework (NMF)
A set of tools for efficient EMF model editing, built on top of the Neo4j graph database. NMF performs all edit operations (CREATE, UPDATE, REMOVE, READ)
directly in the database.

To find out more about Eclipse Modeling Framework (EMF): 

https://www.eclipse.org/modeling/emf

https://eclipsesource.com/blogs/tutorials/emf-tutorial

## Prerequisites
1. A running Neo4j database instance is required with APOC plugin installed. 
Visit https://neo4j.com/download to download Neo4j and https://github.com/neo4j-contrib/neo4j-apoc-procedures to configure APOC plugin
1. Java 8 installed

## Graph-io
A generic graph editor - a core of NMF

The source code of the module: https://github.com/geodes-sms/NeoModelingFramework/tree/master/neo4j-io

### Kotlin usage example
```kotlin
val dbUri = "bolt://localhost:7687"
val username = "neo4j"
val password = "admin"
val graphManager = GraphManager(dbUri, username, password)  // init connection with database
   
val n1 = graphManager.createNode("Node1") // n1 is a node controller
val n2 = graphManager.createNode("Node1")
val n3 = graphManager.createNode("Node2")

val n4 = n1.createChild("ref", "Node4")
val n5 = n4.createChild("ref", "Node4")
graphManager.saveChanges()  // commit updates to the storage

n5.createOutRef("ref2", n1) // controllers remain interactable after the commit
n1.putProperty("property", 44)
graphManager.saveChanges()  // commit new changes

n1.removeChild("ref", n4)  // remove children (cascade delete)
graphManager.saveChanges()
```

## Model loader 
Loads existing EMF model from XMI file to the storage

The source code of the module: https://github.com/geodes-sms/NeoModelingFramework/tree/master/modelLoader

### Usage
1. Prepare an XMI file
A model must come with its metamodel. So, make sure the model file contains `xsi:schemaLocation` attribute pointing to location of the metamodel.
```
...
xsi:schemaLocation="EPACKAGE_NAME PATH_TO_METAMODEL"
...
```
Example of well-formed model files: https://github.com/geodes-sms/NeoModelingFramework/blob/master/EmfModel/instance
1. Download latest jar from "Releases" section
1. Launch the `modelLoader.jar` from a command line
```bash
java -jar <PATH_THE_LOADER> --help
  -h,--host <HOST:PORT>   Database host address with port used to create bolt connection. Example: -h 127.0.0.1:7687
  -m,--model <PATH>       path to model file to be loaded
  -u,--user <arg>         Database auth: username
  -p,--password <arg>     Database auth: password
```

## Code generator
Produces a domain-specific editor from .ecore model