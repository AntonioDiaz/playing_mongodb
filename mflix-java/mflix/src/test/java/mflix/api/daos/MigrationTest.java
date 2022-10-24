package mflix.api.daos;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import mflix.config.MongoDBConfiguration;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@SpringBootTest(classes = {MongoDBConfiguration.class})
@EnableConfigurationProperties
@EnableAutoConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class MigrationTest extends TicketTest {

  MongoCollection<Document> movies;

  @Autowired
  MongoClient mongoClient;

  @Value("${spring.mongodb.database}")
  String databaseName;

  @Before
  public void setup() throws IOException {
    movies = mongoClient.getDatabase(databaseName).getCollection("movies");
  }

  @Test
  public void testAllDocumentsUpdateDateIsDateType() {
    Bson filter = Filters.type("lastupdated", "string");

    int expectedCount = 0;
    Assert.assertEquals(
        "Should not find documents where `lastupdated` is of " + "`string` type",
        expectedCount,
        movies.countDocuments(filter));
  }

  @Test
  public void testAllDocumentsIMDBRatingNumber() {
    Bson filter = Filters.not(Filters.type("imdb.rating", "number"));

    int expectedCount = 0;
    Assert.assertEquals(
        "Should not find documents where `imdb.rating` is of" + " not of `number` type",
        expectedCount,
        movies.countDocuments(filter));
  }

  @Test
  public void testDateFormat() throws ParseException {
    String dateStringInput = "2015-08-13 00:27:59.177000000";
    String datePattern = "yyyy-MM-dd HH:mm:ss.SSSSSSSSS";
    SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
    //Date parse = dateFormat.parse(dateStringInput);
    DateTimeFormatter myDateFormat = DateTimeFormatter.ofPattern(datePattern);
    LocalDate localDateTime = LocalDate.parse(dateStringInput, myDateFormat);
  }
}
