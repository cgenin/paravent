package net.christophe.genin.spring.boot.paravent.queue.core.graphql;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.reactivex.core.Vertx;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.GraphqlManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@ConditionalOnClass(GraphQL.class)
@Configuration
public class GraphqlConfiguration {

    private GraphQL graphQL;

    @Autowired
    private GraphqlManager graphqlManager;

    @Autowired
    private Vertx vertx;


    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

    @PostConstruct
    public void init() throws IOException {
        URL url = Resources.getResource("graphql/schema.graphqls");
        String sdl = Resources.toString(url, Charsets.UTF_8);
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }


    private RuntimeWiring buildWiring() {
        final GraphqlDataFetchers graphqlDataFetchers = new GraphqlDataFetchers(vertx, graphqlManager);
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query")
                        .dataFetcher("queue", graphqlDataFetchers.getQueueOperation())
                        .dataFetcher("metadata", graphqlDataFetchers.getMetadataOperation())
                        .dataFetcher("document", graphqlDataFetchers.getDocumentOperation())
                        .dataFetcher("task", graphqlDataFetchers.getTasksOperation())
                )
                .type(newTypeWiring("Metadata")
                        .dataFetcher("queue", graphqlDataFetchers.getSubQueue())
                )
                .type(newTypeWiring("Document")
                        .dataFetcher("queue", graphqlDataFetchers.getSubQueue())
                        .dataFetcher("metadata", graphqlDataFetchers.getDocumentMetadata())
                        .dataFetcher("bin", graphqlDataFetchers.getDocumentBin())
                )
                .type(newTypeWiring("Mutation")
                        .dataFetcher("startAll", graphqlDataFetchers.startAllTasks())
                        .dataFetcher("stopAll", graphqlDataFetchers.stopAllTasks())
                        .dataFetcher("start", graphqlDataFetchers.startTasks())
                        .dataFetcher("stop", graphqlDataFetchers.stopTasks())
                        .dataFetcher("addToQueue", graphqlDataFetchers.addToQueue())
                )
                .build();
    }

}
