package ai.care.arc.generator;

import ai.care.arc.dgraph.datasource.DgraphSchemaType;
import ai.care.arc.generator.convert.DgraphSchemaTypes2DdlString;
import ai.care.arc.generator.convert.GraphqlSchemaType2DgraphSchemaType;
import ai.care.arc.generator.convert.TypeDefinitionRegistry2GraphqlSchemaTypes;
import ai.care.arc.graphql.idl.GraphqlSchemaType;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 根据GraphqlSchema生成Dgraph数据库结构Schema
 * <p>
 * ┌─────────────────────────────┐          ┌──────────────────────┐          ┌────────────────────────┐          ┌───────────────────────────────┐
 * │InputStream for GraphqlSchema│          │TypeDefinitionRegistry│          │Stream<DgraphSchemaType>│          │Stream<String> for DgraphSchema│
 * └──────────────┬──────────────┘          └──────────┬───────────┘          └───────────┬────────────┘          └───────────────┬───────────────┘
 *                │                                    │                                  │                                       │
 *                │───────────────────────────────────>│                                  │                                       │
 *                │                                    │                                  │                                       │
 *                │                                    │                                  │                                       │
 *                │                                    │ ─────────────────────────────────>                                       │
 *                │                                    │                                  │                                       │
 *                │                                    │                                  │                                       │
 *                │                                    │                                  │ ─────────────────────────────────────>│
 * ┌──────────────┴──────────────┐          ┌──────────┴───────────┐          ┌───────────┴────────────┐          ┌───────────────┴───────────────┐
 * │InputStream for GraphqlSchema│          │TypeDefinitionRegistry│          │Stream<DgraphSchemaType>│          │Stream<String> for DgraphSchema│
 * └─────────────────────────────┘          └──────────────────────┘          └────────────────────────┘          └───────────────────────────────┘
 *
 * @author yuheng.wang
 */
public class DgraphSchemaGenerator {

    private Function<InputStream, TypeDefinitionRegistry> inputStream2TypeDefinitionRegistry;
    private Function<Stream<DgraphSchemaType>, Stream<String>> dgraphSchemaTypes2DdlString;
    private Function<TypeDefinitionRegistry, Stream<DgraphSchemaType>> typeDefinitionRegistry2DgraphSchemaTypes;

    public DgraphSchemaGenerator(Function<InputStream, TypeDefinitionRegistry> inputStream2TypeDefinitionRegistry, Function<Stream<DgraphSchemaType>, Stream<String>> dgraphSchemaTypes2DdlString, Function<TypeDefinitionRegistry, Stream<DgraphSchemaType>> typeDefinitionRegistry2DgraphSchemaTypes) {
        this.inputStream2TypeDefinitionRegistry = inputStream2TypeDefinitionRegistry;
        this.dgraphSchemaTypes2DdlString = dgraphSchemaTypes2DdlString;
        this.typeDefinitionRegistry2DgraphSchemaTypes = typeDefinitionRegistry2DgraphSchemaTypes;
    }

    public DgraphSchemaGenerator() {
        Function<TypeDefinitionRegistry, Stream<GraphqlSchemaType>> typeDefinitionRegistry2GraphqlSchemaTypes = new TypeDefinitionRegistry2GraphqlSchemaTypes();
        Function<GraphqlSchemaType, DgraphSchemaType> graphqlSchemaType2DgraphSchemaType = new GraphqlSchemaType2DgraphSchemaType();

        this.inputStream2TypeDefinitionRegistry = inputStream -> new SchemaParser().parse(inputStream);
        this.dgraphSchemaTypes2DdlString = new DgraphSchemaTypes2DdlString();
        this.typeDefinitionRegistry2DgraphSchemaTypes = typeDefinitionRegistry ->
                typeDefinitionRegistry2GraphqlSchemaTypes
                        .andThen(graphqlSchemaTypeStream -> graphqlSchemaTypeStream.map(graphqlSchemaType2DgraphSchemaType))
                        .apply(typeDefinitionRegistry);
    }

    public List<String> generate(InputStream graphSchemaInputStream) {
        return typeDefinitionRegistry2DgraphSchemaTypes
                .compose(inputStream2TypeDefinitionRegistry)
                .andThen(dgraphSchemaTypes2DdlString)
                .apply(graphSchemaInputStream)
                .collect(Collectors.toList());
    }

}