# Mongo DB & Spring Boot
—
<h2> Contents </h2>  

- [Links](#links)
- [Notes](#notes)
- [Queries](#queries)
  - [Filters](#filters)
  - [Projections](#projections)
- [Basic Reads](#basic-reads)
  - [Default Codec](#default-codec)
  - [CustomCodec](#customcodec)
- [Aggregation](#aggregation)
- [Writes](#writes)
- [Updates](#updates)
- [Joins](#joins)

## Links  
https://www.mongodb.com/compatibility/spring-boot
  
https://university.mongodb.com/courses/M220J/about

## Notes
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

## Queries

### Filters
```java
Bson queryFilter = all("cast", "Salma Hayek", "Johnny Depp");
List<Document> results = new ArrayList<>();
moviesCollection.find(queryFilter).into(results);
```

### Projections
```java
Document newResult =
    moviesCollection
        .find(queryFilter)
        .limit(1)
        .projection(fields(include("title", "year"), exclude("_id")))
        .iterator()
        .tryNext();
```


## Basic Reads
* Using Pojo
  * The POJO implementation is much cleaner. It outsources translation between BSON and Object to a __custom Codec__ which makes it easer to manage and maintain the code.

  * In either approach the field names don't have to match the object attribute name. In case of the custom Codec this is easy to accommodate in the code, via the __@BsonProperty__ annotation only for the fields that don't correspond to the attribute name one-to-one.

  * To map the Document to an Object manually you have to maka use of getters and setters, while using a POJO allows for automatic instropection (aka automatic process of analyzing a bean's design patterns to reveal the bean's properties, event and methods).

  * Handling of generics and subdocuments is cleaner and easier to maintain with POJO, utilizing the Custom Codec or the field customization approach instead of writing separate methods to traverse sub-documents with getters and setters.

  * When writting a Document object to a database, the document _id is automatically generated and can be accessed in the Document object for the subsequent use in the mapping method.

### Default Codec
  * Using default codec
```java
CodecRegistry pojoCodecRegistry =
    fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        fromProviders(PojoCodecProvider.builder().automatic(true).build()));
MongoCollection<ActorBasic> actors =
    testDb.getCollection("actors", ActorBasic.class).withCodecRegistry(pojoCodecRegistry);
Bson queryFilter = new Document("_id", actor1Id);
ActorBasic pojoActor = actors.find(queryFilter).iterator().tryNext();
```

  * Pojo
```java
public class ActorBasic {
  @BsonProperty("_id")
  private ObjectId id;

  private String name;

  @BsonProperty("date_of_birth")
  private Date dateOfBirth;

  private List awards;

  @BsonProperty("num_movies")
  private int numMovies;

  public ActorBasic() { // constructor
  }

  public ActorBasic withNewId() {
    setId(new ObjectId());
    return this;
  }
}
```

### CustomCodec
* Defining CustomCodec
```java
public class ActorCodec implements CollectibleCodec<ActorWithStringId> {

  private final Codec<Document> documentCodec;

  public ActorCodec() {
    super();
    this.documentCodec = new DocumentCodec();
  }

  public void encode(
      BsonWriter bsonWriter, ActorWithStringId actor, EncoderContext encoderContext) {
    Document actorDoc = new Document();
    String actorId = actor.getId();
    String name = actor.getName();
    Date dateOfBirth = actor.getDateOfBirth();
    List awards = actor.getAwards();
    int numMovies = actor.getNumMovies();
    if (null != actorId) 
      actorDoc.put("_id", new ObjectId(actorId));
    if (null != name) 
      actorDoc.put("name", name);
    if (null != dateOfBirth) 
      actorDoc.put("date_of_birth", dateOfBirth);
    if (null != awards) 
      actorDoc.put("awards", awards);
    if (0 != numMovies) 
      actorDoc.put("num_movies", numMovies);
    documentCodec.encode(bsonWriter, actorDoc, encoderContext);
  }

  @Override
  public ActorWithStringId decode(BsonReader bsonReader, DecoderContext decoderContext) {
    Document actorDoc = documentCodec.decode(bsonReader, decoderContext);
    ActorWithStringId actor = new ActorWithStringId();
    actor.setId(actorDoc.getObjectId("_id").toHexString());
    actor.setName(actorDoc.getString("name"));
    actor.setDateOfBirth(actorDoc.getDate("date_of_birth"));
    actor.setAwards((List<Document>) actorDoc.get("awards"));
    actor.setNumMovies(actorDoc.getInteger("num_movies"));
    return actor;
  }

  @Override
  public Class<ActorWithStringId> getEncoderClass() {
    return ActorWithStringId.class;
  }

  @Override
  public ActorWithStringId generateIdIfAbsentFromDocument(ActorWithStringId actor) {
    return !documentHasId(actor) ? actor.withNewId() : actor;
  }

  @Override
  public boolean documentHasId(ActorWithStringId actor) {
    return null != actor.getId();
  }

  @Override
  public BsonString getDocumentId(ActorWithStringId actor) {
    if (!documentHasId(actor)) {
      throw new IllegalStateException("This document does not have an " + "_id");
    }
    return new BsonString(actor.getId());
  }
}
```

  * Using CustomCodec
  ```java
      // first we establish the use of our new custom codec
    ActorCodec actorCodec = new ActorCodec();
    // then create a codec registry with this codec
    CodecRegistry codecRegistry =
        fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromCodecs(actorCodec));
    // we can now access the actors collection with the use of our custom
    // codec that is specifically tailored for the actor documents.
    Bson queryFilter = new Document("_id", actor1Id);
    MongoCollection<ActorWithStringId> customCodecActors =
        testDb.getCollection("actors", ActorWithStringId.class).withCodecRegistry(codecRegistry);
    // we retrieve the first actor document
    ActorWithStringId actor = customCodecActors.find(Filters.eq("_id", actor1Id)).first();
  ```

## Aggregation
* Aggregation is a pipeline:
  * __Pipelines__ are composed of __stages__, broad units of work.
  * Within stages, __expressions__ are used to specify individual units of work.
* Expresions are funciones.
* Aggregations single

```java
@Test
public void singleStageAggregation() {
  Bson countryPT = Filters.eq("countries", "Portugal");
  List<Bson> pipeline = new ArrayList<>();
  Bson matchStage = Aggregates.match(countryPT);
  pipeline.add(matchStage);
  AggregateIterable<Document> iterable = moviesCollection.aggregate(pipeline);
  List<Document> builderMatchStageResults = new ArrayList<>();
  iterable.into(builderMatchStageResults);
  Assert.assertEquals(115, builderMatchStageResults.size());
}
```
  * Aggregation complex
```javascript
  db.movies.aggregate([
      {$match: {countries: "Portugal"}},
      {$unwind: "$cast"},
      {$group: {_id: "$cast", gigs: {$sum: 1}}}
  ])
```

```java
  @Test
  public void aggregateSeveralStages() {
    List<Bson> pipeline = new ArrayList<>();
    Bson matchStage = Aggregates.match(Filters.eq("countries", "Portugal"));
    Bson unwindCastStage = Aggregates.unwind("$cast");
    Bson groupStage = Aggregates.group("$cast", Accumulators.sum("count", 1));
    Bson sortStage = Aggregates.sort(Sorts.descending("count"));
    pipeline.add(matchStage);
    pipeline.add(unwindCastStage);
    pipeline.add(groupStage);
    pipeline.add(sortStage);

    AggregateIterable<Document> iterable = moviesCollection.aggregate(pipeline);

    List<Document> groupByResults = new ArrayList<>();
    for (Document doc : iterable) {
      System.out.println(doc);
      groupByResults.add(doc);
    }
  }
```

## Writes
- We can insert new documents using both the __insertOne__ or __insertMany__ collection methods. 
- Update using the __$ flag set to true also allows us to insert new documents.
- Using __$setOnInsert__ update operator provides a way to set specific fields only in the case of insert.

* Example __insertOne__
```java
@Test
public void testWriteOneDocument() {
  Document doc = new Document("title", "Fortnite");
  doc.append("year", 2018);
  doc.put("label", "Epic Games");
  videoGames.insertOne(doc);
  Assert.assertNotNull(doc.getObjectId("_id"));
  Document retrieved = videoGames.find(Filters.eq("_id", doc.getObjectId("_id"))).first();
  Assert.assertEquals(retrieved, doc);
}
```

* Example __$setOnInsert__
```java
  @Test
  public void testUpsertDocument() {
    Document doc1 = new Document("title", "Final Fantasy");
    doc1.put("year", 2003);
    doc1.put("label", "Square Enix");
    Bson query = new Document("title", "Final Fantasy");
    UpdateResult resultNoUpsert = videoGames.updateOne(query, new Document("$set", doc1));
    Assert.assertEquals(0, resultNoUpsert.getMatchedCount());
    Assert.assertNotEquals(1, resultNoUpsert.getModifiedCount());
    UpdateOptions options = new UpdateOptions();
    options.upsert(true);
    UpdateResult resultWithUpsert =
        videoGames.updateOne(query, new Document("$set", doc1), options);
    Assert.assertEquals(0, resultWithUpsert.getModifiedCount());
    Assert.assertNotNull(resultWithUpsert.getUpsertedId());
    Assert.assertTrue(resultWithUpsert.getUpsertedId().isObjectId());
    Bson updateObj1 =
        Updates.combine(
            Updates.set("title", "Final Fantasy 1"), Updates.setOnInsert("just_inserted", "yes"));
    query = Filters.eq("title", "Final Fantasy");
    UpdateResult updateAlreadyExisting = videoGames.updateOne(query, updateObj1, options);
    Document finalFantasyRetrieved =
        videoGames.find(Filters.eq("title", "Final Fantasy 1")).first();
    Assert.assertFalse(finalFantasyRetrieved.keySet().contains("just_inserted"));
    Document doc2 = new Document("title", "CS:GO");
    doc2.put("year", 2018);
    doc2.put("label", "Source");
    Document updateObj2 = new Document();
    updateObj2.put("$set", doc2);
    updateObj2.put("$setOnInsert", new Document("just_inserted", "yes"));
    UpdateResult newDocumentUpsertResult =
        videoGames.updateOne(Filters.eq("title", "CS:GO"), updateObj2, options);
    Bson queryInsertedDocument = new Document("_id", newDocumentUpsertResult.getUpsertedId());
    Document csgoDocument = videoGames.find(queryInsertedDocument).first();
    Assert.assertEquals("yes", csgoDocument.get("just_inserted"));
  }
```  
## Updates
 1. You can replace entire documents with __replaceOne__. This operation may cause you to lose data, so it isn't recommended for situations when you need a simple update.  
https://github.com/AntonioDiaz/playing_mongodb/blob/21ec65cdf969c2faba5855ed462e60a1a9b6b5e2/mflix-java/mflix/src/test/java/mflix/lessons/UpdateOperators.java#L79-L98

 2. You can update a value in a single document using the __updateOne__ and __set__ or __inc__ operators.
https://github.com/AntonioDiaz/playing_mongodb/blob/c64737256070b448b9cc87a82a80a65cee2a48c9/mflix-java/mflix/src/test/java/mflix/lessons/UpdateOperators.java#L131-L151

 3. You can update multiple documents that match your query using updateMany in conjunction with set or inc operations.
https://github.com/AntonioDiaz/playing_mongodb/blob/c64737256070b448b9cc87a82a80a65cee2a48c9/mflix-java/mflix/src/test/java/mflix/lessons/UpdateOperators.java#L166-L180


 4. You can completely remove a field from a document by using updateOne or updateMany with the unset operation.
https://github.com/AntonioDiaz/playing_mongodb/blob/c64737256070b448b9cc87a82a80a65cee2a48c9/mflix-java/mflix/src/test/java/mflix/lessons/UpdateOperators.java#L230-L249


 For more update operators and their use cases, feel free to check out the following documentation page:  
 http://mongodb.github.io/mongo-java-driver/3.8/driver/tutorials/perform-write-operations/

## Joins
* Join two collections of data, e.g. movies and comments.
* Usage new expressive '$lookup'
* Build aggregations in Compass, and then export to language.
* Expressive `lookup` allows us to apply aggregation pipeplines to data-before the data is joined.
* `let` allows us to declare variables in our pipeline, referring to document fields in our source collection.
* Compass export to language feature produces aggregation in our application's native language.
* Join example:
```json
{
  from: 'comments',
  let: {'id': '$_id'},
  pipeline: [
    { '$match': 
      { '$expr': { '$eq': ['$movie_id', '$$id'] }},
    }, {'$count': 'count'}  
  ],
  as: 'movie_comments'
}
```



<img width="950" alt="mongo_lookup" src="https://user-images.githubusercontent.com/725743/196049510-60f28540-2c74-488c-ba44-e3b3c4ba5c31.png">
