package a.b.c.type;

import a.b.c.api.QueryService;
import ai.care.arc.dgraph.dictionary.IDgraphType;
import ai.care.arc.graphql.annotation.DataFetcherService;
import ai.care.arc.graphql.annotation.GraphqlMethod;
import graphql.schema.DataFetcher;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Query
 * Generate with GraphQL Schema By Arc
 */
@DataFetcherService
public class Query implements IDgraphType {
  @Autowired
  private QueryService queryService;

  @GraphqlMethod(
      type = "Query"
  )
  public DataFetcher<Project> project() {
    return dataFetchingEnvironment ->  {
      return queryService.handleProject(dataFetchingEnvironment);
    };
  }

  @GraphqlMethod(
      type = "Query"
  )
  public DataFetcher<List<User>> users() {
    return dataFetchingEnvironment ->  {
      return queryService.handleUsers(dataFetchingEnvironment);
    };
  }

}