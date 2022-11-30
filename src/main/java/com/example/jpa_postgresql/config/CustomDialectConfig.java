package com.example.jpa_postgresql.config;

import com.vladmihalcea.hibernate.type.array.StringArrayType;
import org.hibernate.dialect.PostgreSQL10Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.context.annotation.Configuration;

/**
 * <pre>
 * com.example.jpa_postgresql.config
 * └ CustomDialectConfig
 *
 *
 * </pre>
 *
 * @author : hycho
 * @date : 2022-11-30
 **/
@Configuration
public class CustomDialectConfig extends PostgreSQL10Dialect {

    public CustomDialectConfig() {
        super();

        // 배열 컬럼 타입 지정
        this.registerHibernateType(2003, StringArrayType.class.getName());

        // DB 내장함수 사용
        this.registerFunction("concat_ws", new StandardSQLFunction("concat_ws", StandardBasicTypes.STRING));
        this.registerFunction("string_agg", new StandardSQLFunction("string_agg", StandardBasicTypes.STRING));
        this.registerFunction("array_to_string", new StandardSQLFunction("array_to_string", StandardBasicTypes.STRING));

        // 사용자 정의함수
        // String 형태의 return type 함수는 StandardBasicTypes.STRING 으로 선언
        // SAMPLE: 공통 코드 명 호출 함수
        // this.registerFunction("fn_get_comm_cd_nm", new SQLFunctionTemplate(StandardBasicTypes.STRING, "public.fn_get_comm_cd_nm(?1)"));
    }
}