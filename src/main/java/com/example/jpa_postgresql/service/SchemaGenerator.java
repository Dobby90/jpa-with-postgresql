package com.example.jpa_postgresql.service;

import com.example.jpa_postgresql.ColumnDefinition;
import com.example.jpa_postgresql.annotation.ColumnPosition;
import com.example.jpa_postgresql.entity.Board;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.stereotype.Service;

import javax.persistence.Column;
import javax.persistence.Transient;
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
@Service
public class SchemaGenerator extends SchemaGeneratorBase {

    public void createSchema() throws IOException, ClassNotFoundException {

        Map<String, String> settings = new HashMap<>();
        settings.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        settings.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        settings.put("hibernate.connection.url", "jdbc:postgresql://localhost:5432/demo");
        settings.put("hibernate.connection.username", "dobby");
        settings.put("hibernate.connection.password", "1234");
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(settings).build();
        MetadataSources metadataSources = new MetadataSources(serviceRegistry);

        //
        metadataSources.addAnnotatedClass(Board.class);
        //

        Metadata metadata = metadataSources.buildMetadata();

        SchemaExport schemaExport = new SchemaExport();
        schemaExport.setHaltOnError(true);
        schemaExport.setFormat(true);
        schemaExport.setDelimiter(";");
        String sqlFilePath = "create.sql";
        File sqlFile = new File(sqlFilePath);
        schemaExport.setOutputFile(sqlFile.getName());
        schemaExport.createOnly(EnumSet.of(TargetType.SCRIPT), metadata);

        BufferedReader inFiles = new BufferedReader(new InputStreamReader(new FileInputStream(sqlFilePath), StandardCharsets.UTF_8));
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
                convertedDDLs.add(convert(DDL + ";"));
            } else {
                convertedDDLs.add(DDL + ";");
            }
        }

        for (String convertedDDL : convertedDDLs) {
            System.out.println(convertedDDL);
            //jdbcTemplate.execute(convertedDDL);
        }

        if (sqlFile.exists()) {
            inFiles.close();
            sqlFile.delete();
            System.out.println("File Delete Success");
        }
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

        List<ColumnDefinition> columnDefinitions = Arrays.stream(columnBody.split(", ")).map(ColumnDefinition::new).collect(toList());
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

        convertedDDL.append(columns.toString());

        convertedDDL.append(")");

        return convertedDDL.toString();
    }

    public void setPosition(Field field, List<ColumnDefinition> columnDefinitions) {
        String name = field.getName();
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
                        if (columnName.toLowerCase().equals(columnDefinition.getColumnName().toLowerCase())) {
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
        return text.replaceAll("\\s+$","");
    }
}
