## Mongo DB & Spring Boot


https://www.mongodb.com/compatibility/spring-boot



### Concepts

MongoTemplate         
MongoRepository        
BSON                -> object in MongoDB  
MongoDB Atlas       -> https://www.mongodb.com/atlas/database

Annotations:  
@Document("groceryitems")
@EnableMongoRepositories

### MongoDB Atlas
<img width="686" alt="hello_world_01" src="https://user-images.githubusercontent.com/725743/194713472-38fa9c28-b0b7-4afc-aaa5-8ea555488c11.png">

--  
### Files
<img width="454" alt="hello_world_02" src="https://user-images.githubusercontent.com/725743/194713508-4f405b51-9a36-467b-aedf-73218aedccc3.png">
__pom.xml__
````xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
````

__application.properties__
````properties
spring.data.mongodb.uri=mongodb+srv://mongo_user:password_22@antonio-diaz-arroyo-clu.mj7kznv.mongodb.net/?retryWrites=true&w=majority
spring.data.mongodb.database=mygrocerylist
````

__GroceryItem__
````java
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("groceryitems")
@Data
public class GroceryItem {

  @Id
  private String id;

  private String name;
  private int quantity;
  private String category;

  public GroceryItem(String id, String name, int quantity, String category) {
    super();
    this.id = id;
    this.name = name;
    this.quantity = quantity;
    this.category = category;
  }
}
````

__MongoRepository__
```java
import com.adiaz.hellomongo.model.GroceryItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ItemRepository extends MongoRepository<GroceryItem, String> {

  @Query("{name:'?0'}")
  GroceryItem findItemByName(String name);

  @Query(value="{category:'?0'}", fields="{'name' : 1, 'quantity' : 1, 'category': 1}")
  List<GroceryItem> findAll(String category);

  long count();
}
```

__MongoTemplate__
````java
import com.adiaz.hellomongo.model.GroceryItem;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class CustomItemRepositoryImpl implements CustomItemRepository{
  @Autowired
  MongoTemplate mongoTemplate;

  public void updateItemQuantity(String name, float newQuantity) {
    Query query = new Query(Criteria.where("name").is(name));
    Update update = new Update();
    update.set("quantity", newQuantity);
    UpdateResult result = mongoTemplate.updateFirst(query, update, GroceryItem.class);
    if(result == null)
      System.out.println("No documents updated");
    else
      System.out.println(result.getModifiedCount() + " document(s) updated..");
  }
}
````