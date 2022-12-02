package com.example.jpa_postgresql.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metamodel.internal.MetamodelImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Metamodel;
import java.util.Map;

/**
 * <pre>
 * com.example.jpa_postgresql.service
 * └ SchemaGeneratorBase
 *
 *
 * </pre>
 *
 * @author : hycho
 * @date : 2022-12-01
 **/
public class SchemaGeneratorBase {

    @Autowired
    protected EntityManagerFactory entityManagerFactory;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected EntityPersister getEntityPersister(String tableName) {

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
}
