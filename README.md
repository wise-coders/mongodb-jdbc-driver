# MongoDb JDBC Driver

JDBC driver capable to execute native MongoDb queries, similar with Mongo Shell. 
The driver is written by [DbSchema Database Designer team](https://dbschema.com)
for the tool and for everybody how needs an MongoDb JDBC driver. 

The driver is using the native [MongoDb Java driver](https://mongodb.github.io/mongo-java-driver/) to connect and execute queries. 
Therefore the JDBC URL is the same as [MongoDb URL](https://mongodb.github.io/mongo-java-driver/3.4/driver/tutorials/connect-to-mongodb/).

The driver returns by default a ResultSet with a single Object. Use **resultSet.getObject(1)** to get this object.
Adding the parameter 'expand=true' in the URL will create a column in the result set for each key in the result document.

If expand is set the driver will read ahead a number of rows in order to create a correct ResultSetMetaData. This is transparent for the user.
This because the first document in the result may have less keys as the next records.


[DbSchema Database Designer](https://dbschema.com) is also calling methods from the JDBC driver DatabaseMetaData.getTables(), getColumns(), etc., to 
deduce a logical structure of the database. We presume that collections are storing similar documents, so we 'deduce' a virtual schema by 
scanning random documents from each collection.
The number of scanned documents can be set in the URL using the parameter scan=<fast|medium|full>.

To be able to execute native MongoDb queries we embedded an Rhino JavaScript engine inside the driver.
Each time you execute a query we parse and run it as JavaScript with Rhino.

[DbSchema Database Designer](https://dbschema.com) is showing the MongoDb structure as diagrams and can execute MongoDb queries.
Further tools like Random Data Generator for MongoDb, Relational Data Browse and others are available.

The JDBC driver is also capable of finding out 'virtual relations' ( a kind of foriegn keys in MongoDb ). 
This are ObjectID values in collections which are referring 
other collections. We return this via DatabaseMetaData.getImportedKeys() and DbSchema is showing them as 'virtual relations'.

DbSchema can be downloaded for free [from dbschema.com](http://dbschema.com/download). The MongoJdbcDriver is automatically included.

## License

BSD License-3. Free to use, distribution forbidden. Improvements of the driver accepted only in https://bitbucket.org/dbschema/mongodb-jdbc-driver.

## Download JDBC Driver Binary Distribution

[Available here](http://www.dbschema.com/jdbc-drivers/MongoDbJdbcDriver.zip). Unpack and include all jars in your classpath. The driver is compatible with Java 8.

## Driver URL

```
jdbc:mongodb://[username:password@]host1[:port1][,...hostN[:portN]][/[database][?options]]
```
The driver is using the same URL, options and parameters as [native MongoDb Java driver](https://docs.mongodb.com/manual/reference/connection-string/). 
Different is only the 'jdbc:' prefix.


## How to use the Driver

The driver can be use similar with any other JDBC driver. The resultSet will always receive a single object as document.
```
#!java

import java.sql.Connection;
import java.sql.PreparedStatement;

...

Class.forName("com.dbschema.MongoJdbcDriver");
Properties properties = new Properties();
properties.put("user", "someuser");
properties.put("password", "somepassword" );
Connection con = DriverManager.getConnection("jdbc:mongodb://host1:9160/keyspace1", properties);
// OTHER URL (SAME AS FOR MONGODB NATIVE DRIVER): mongodb://db1.example.net,db2.example.net:2500/?replicaSet=test&connectTimeoutMS=300000
String query = "db.sampleCollection().find()";
Statement statement = con.createStatement();
ResultSet rs = statement.executeQuery( query );
Object json = rs.getObject(1);

```

Any contributions to this project are welcome.
We are looking forward to improve this and make possible to execute all MongoDb native queries via JDBC.

## How it Works

The driver implements a PreparedStatement where native MongoDb queries can be passed. Sample: 'db.myCollection.find()'.
In the MongoPreparedStatement we start a Rhino JavaScript engine, and pass this query to the engine.
The engine receives also an object 'db':new WrappedMongoDatabase()
The WrappedMongoDatabase is a wrapper around the native MongoDatabase object, with support for Collections as native member variables.
This  make possible to do ´db.myCollection´ - otherwise it would work only ´db.getCollection('myCollection')´
The collection objects are wrapped as well into WrappedMongoCollection. The reason for this is that most of the methods 
require Bson objects, and JavaScript will generate only Map objects.
For example ´db.myCollection.find({'age':12})´ will result in a call of db.myCollection.find(Bson bson) with a Map instead of Bson, which will throw an error.
We tried various solutions for avoiding this, including java Proxy. If you know any better solution please let us know, we can improve the project.
Writing the Wrapper class we added methods which receive Map objects and we take care of the conversion.

In test cases we try to add all possible queries we want to support. If you find any query which does not work please feel free to commit in the source code or write us.



## DbSchema Main Features for MongoDb

* Structure discovery and diagrams 
* Relational Data Browse and Editor
* Query Editor
* Visual Query Builder
* Random Data Generator
* Data Loader
 

DbSchema reads sample JSon documents from the database and builds diagrams showing the JSon structure. We consider that each collection documents have similar structure.

![mongodb1.png](https://bitbucket.org/repo/BELRaG/images/282491526-mongodb1.png)

Use the Query Editor to edit and execute MongoDb queries in the native language:

![mongodb2.png](https://bitbucket.org/repo/BELRaG/images/2249668125-mongodb2.png)


Using Relational Data Browse you can explore data from multiple collections simultaneously. 
Collections may bind one with another using virtual relations ( if one field value points to a certain document from another collection ). 
This is shown as a line between collections ( see here master and slave ). T
hen data from both collections can be explored. Clicking a document in the first collection will update the second collection with the matching documents.

![mongo3.png](https://bitbucket.org/repo/BELRaG/images/2228714881-mongo3.png)

A full description of DbSchema features is available on [DbSchema website](http://www.dbschema.com/mongodb-tool.html).