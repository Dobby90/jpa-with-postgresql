package com.example.jpa_postgresql.service;

import com.example.jpa_postgresql.framework.annotation.ColumnPosition;
import com.example.jpa_postgresql.framework.component.ColumnDefinition;
import com.example.jpa_postgresql.model.entity.Board;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.metamodel.internal.MetamodelImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.Column;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Transient;
import javax.persistence.metamodel.Metamodel;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * <pre>
 * com.example.jpa_postgresql.service
 * └ SchemaGenerator
 *
 *
 * </pre>
 *
 * @author : hycho
 * @date : 2022-12-01
 **/
@Slf4j
@RequiredArgsConstructor
@Service
public class initService {

    private final EntityManagerFactory entityManagerFactory;

    private final JdbcTemplate jdbcTemplate;

    public void initSchemaCreate() throws IOException, ClassNotFoundException {

        Map<String, String> settings = new HashMap<>();
        settings.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        settings.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        settings.put("hibernate.connection.url", "jdbc:postgresql://localhost:5432/demo");
        settings.put("hibernate.connection.username", "dobby");
        settings.put("hibernate.connection.password", "1234");
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(settings).build();
        MetadataSources metadataSources = new MetadataSources(serviceRegistry);

        metadataSources.addAnnotatedClass(Board.class);

        Metadata metadata = metadataSources.buildMetadata();

        SchemaExport schemaExport = new SchemaExport();
        schemaExport.setHaltOnError(true);
        schemaExport.setFormat(true);
        schemaExport.setDelimiter(";");
        String initSchemaFilePath = "initSchema.sql";
        File initSchemaFile = new File(initSchemaFilePath);
        schemaExport.setOutputFile(initSchemaFile.getName());
        schemaExport.create(EnumSet.of(TargetType.SCRIPT), metadata);

        log.info("initSchema.sql File Create Success");

        BufferedReader inFiles = new BufferedReader(new InputStreamReader(new FileInputStream(initSchemaFilePath), StandardCharsets.UTF_8));
        StringBuilder fileText = new StringBuilder();
        String temp;
        while ((temp = inFiles.readLine()) != null) {
            fileText.append(temp);
        }
        String[] DDLs = fileText.toString().split(";");

        List<String> convertedDDLs = new ArrayList<>();

        for (String DDL : DDLs) {
            DDL = ltrim(DDL);
            if (DDL.toLowerCase().startsWith("create table")) {
                convertedDDLs.add(convert(DDL) + ";");
            } else {
                convertedDDLs.add(DDL + ";");
            }
        }

        for (String convertedDDL : convertedDDLs) {
            jdbcTemplate.execute(convertedDDL);
        }
        log.info("initSchema.sql Execute Success");

        inFiles.close();
        if (initSchemaFile.delete()) {
            log.info("initSchema.sql File Delete Success");
        }
    }

    private EntityPersister getEntityPersister(String tableName) {

        Metamodel metamodel = entityManagerFactory.getMetamodel();

        Map<String, EntityPersister> entityPersisters = ((MetamodelImpl) metamodel).entityPersisters();

        for (String className : entityPersisters.keySet()) {
            EntityPersister entityPersister = entityPersisters.get(className);

            if (((SingleTableEntityPersister) entityPersister).getTableName().equalsIgnoreCase(tableName)) {
                return entityPersister;
            }
        }

        throw new IllegalArgumentException("not exist table");
    }

    private String convert(String ddl) throws ClassNotFoundException {
        StringBuilder convertedDDL = new StringBuilder();

        int startColumnBody = ddl.indexOf('(');
        int endColumnBody = ddl.lastIndexOf(')');

        String tableName = ddl.substring("create table ".length(), startColumnBody).trim();
        String columnBody = ddl.substring(startColumnBody + 1, endColumnBody);
        String primaryKeyDefinition = "";

        int primaryKey = columnBody.indexOf("primary key");
        primaryKeyDefinition = columnBody.substring(primaryKey);
        columnBody = columnBody.substring(0, primaryKey - 2);
        columnBody = ltrim(columnBody);
        columnBody = rtrim(columnBody);

        List<ColumnDefinition> columnDefinitions = Arrays.stream(columnBody.split(",")).map(ColumnDefinition::new).collect(toList());
        columnDefinitions.add(new ColumnDefinition(primaryKeyDefinition));

        EntityPersister entityPersister = getEntityPersister(tableName);

        Class<?> clazz = Class.forName(entityPersister.getEntityName());
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            setPosition(field, columnDefinitions);
        }

        convertedDDL
                .append("create table")
                .append(" ")
                .append(tableName)
                .append(" ")
                .append("(");

        StringJoiner columns = new StringJoiner(", ");

        columnDefinitions
                .stream()
                .sorted(
                        Comparator.comparingInt(ColumnDefinition::getPosition)
                                .thenComparing(ColumnDefinition::getColumnName))
                .forEach(entityField -> {
                    columns.add(entityField.getColumnDefinition());
                });

        convertedDDL.append(columns);

        convertedDDL.append(")");

        return convertedDDL.toString();
    }

    public void setPosition(Field field, List<ColumnDefinition> columnDefinitions) {
        String columnName;
        int position = Integer.MAX_VALUE - 10;

        if (field.getAnnotation(Transient.class) == null) {
            Column column = field.getAnnotation(Column.class);
            ColumnPosition columnPosition = field.getAnnotation(ColumnPosition.class);

            columnName = column.name();

            if (columnPosition != null && columnPosition.value() > 0) {
                position = columnPosition.value();
            }

            if (columnName != null) {
                for (ColumnDefinition columnDefinition : columnDefinitions) {
                    if (columnDefinition.getColumnName() != null) {
                        if (columnName.equalsIgnoreCase(columnDefinition.getColumnName())) {
                            columnDefinition.setPosition(position);
                        }
                    }
                }
            }
        }
    }

    public String ltrim(String text) {
        return text.replaceAll("^\\s+", "");
    }

    public String rtrim(String text) {
        return text.replaceAll("\\s+$", "");
    }
}
