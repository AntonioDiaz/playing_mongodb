## Mongo DB & Spring Boot
—

https://www.mongodb.com/compatibility/spring-boot
  
https://university.mongodb.com/courses/M220J/about

### Notes
* MongoDB Java Driver Base Classes
  * MongoClient
  * MongoDatabase
  * MongoCollection
  * Document
  * Bson
* Document 
  ```java
  public class Document implements Map<String, Object>, Serializable, Bson {...}
  ```

## Query builders
see `QueryBuilders.java`  

* __Filters__
```java
Bson queryFilter = all("cast", "Salma Hayek", "Johnny Depp");
List<Document> results = new ArrayList<>();
moviesCollection.find(queryFilter).into(results);
```

* __Projections__
```java
Document newResult =
    moviesCollection
        .find(queryFilter)
        .limit(1)
        .projection(fields(include("title", "year"), exclude("_id")))
        .iterator()
        .tryNext();
```

  * Sorts
  * Aggregation
  * Update
  * Indexes


## Basic Reads
