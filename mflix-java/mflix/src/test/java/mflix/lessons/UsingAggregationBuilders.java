package mflix.lessons;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @see com.mongodb.client.model.Facet
 * @see com.mongodb.client.model.Accumulators
 * @see com.mongodb.client.model.Aggregates
 */
@SpringBootTest
public class UsingAggregationBuilders extends AbstractLesson {

  /*
  In this lesson we are going to walk you through how to build complex
  aggregation framework pipelines using the Java Driver aggregation builders
   */

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

  @Test
  public void aggregateSeveralStages() {
    /*
    db.movies.aggregate([
        {$match: {countries: "Portugal"}},
        {$unwind: "$cast"},
        {$group: {_id: "$cast", gigs: {$sum: 1}}}
    ])
    */
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

    /*
    The aggregation framework also provides stages that combine
    operations that are typically expressed by several stages.
    For example, $sortByCount, combines both the $group with a $sum
    accumulator with a $sort stage.
    Don't believe me? Well, let's check it out!
     */

    List<Bson> shorterPipeline = new ArrayList<>();

    // we already have built booth $match and $unwind stages
    shorterPipeline.add(matchStage);
    shorterPipeline.add(unwindCastStage);

    // create the $sortByCountStage
    Bson sortByCount = Aggregates.sortByCount("$cast");

    // append $sortByCount stage to shortPipeline
    shorterPipeline.add(sortByCount);

    // list to collect shorterPipeline results
    List<Document> sortByCountResults = new ArrayList<>();

    for (Document doc : moviesCollection.aggregate(shorterPipeline)) {
      System.out.println(doc);
      sortByCountResults.add(doc);
    }

    /*
    Running both pipelines, the same set of results.
     */

    Assert.assertEquals(groupByResults, sortByCountResults);
  }

  @Test
  public void complexStages() {
    List<Bson> pipeline = new ArrayList<>();
    Bson unwindCast = Aggregates.unwind("$cast");
    Bson groupCastSet = Aggregates.group("",
            Accumulators.addToSet("cast_list", "$cast"));
    Facet castMembersFacet = new Facet("cast_members", unwindCast, groupCastSet);
    // unwind genres
    Bson unwindGenres = Aggregates.unwind("$genres");
    // genres facet bucket
    Bson genresSortByCount = Aggregates.sortByCount("$genres");
    // create a genres count facet
    Facet genresCountFacet = new Facet("genres_count", unwindGenres, genresSortByCount);
    // year bucketAuto
    Bson yearBucketStage = Aggregates.bucketAuto("$year", 10);
    // year bucket facet
    Facet yearBucketFacet = new Facet("year_bucket", yearBucketStage);
    // $facets stage
    Bson facetsStage = Aggregates.facet(castMembersFacet, genresCountFacet, yearBucketFacet);
    // match stage
    Bson matchStage = Aggregates.match(Filters.eq("countries", "Portugal"));
    // putting it all together
    pipeline.add(matchStage);
    pipeline.add(facetsStage);
    int countDocs = 0;
    for (Document doc : moviesCollection.aggregate(pipeline)) {
      System.out.println(doc);
      countDocs++;
    }
    Assert.assertEquals(1, countDocs);
  }

  /*
  Let's recap:
  - Aggregation framework pipelines are composed of lists of Bson stage
  document objects
  - Use the driver Aggregates builder class to compose the different stages
  - Use Accumulators, Sorts and Filters builders to compose the different
  stages expressions
  - Complex aggregation stages can imply several different sub-pipelines
  and stage arguments.
   */

}
