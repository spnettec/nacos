/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.persistence.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExternalDataSourcePropertiesTest {
    
    @SuppressWarnings("checkstyle:linelength")
    public static final String JDBC_URL =
        "jdbc:mysql://127.0.0.1:3306/nacos_devtest?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC";

    public static final String MARIADB_JDBC_URL = "jdbc:mariadb://127.0.0.1:3306/nacos_devtest";

    public static final String ORACLE_JDBC_URL = "jdbc:oracle:thin:@127.0.0.1:1521/orcl";

    public static final String POSTGRESQL_JDBC_URL = "jdbc:postgresql://127.0.0.1:5432/nacos";

    public static final String SQLSERVER_JDBC_URL = "jdbc:sqlserver://127.0.0.1:1433;databaseName=nacos";
    
    public static final String PASSWORD = "nacos";
    
    public static final String USERNAME = "nacos_devtest";
    
    @Test
    void externalDatasourceNormally() {
        HikariDataSource expectedDataSource = new HikariDataSource();
        expectedDataSource.setJdbcUrl(JDBC_URL);
        expectedDataSource.setUsername(USERNAME);
        expectedDataSource.setPassword(PASSWORD);
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("db.num", "1");
        environment.setProperty("db.user", USERNAME);
        environment.setProperty("db.password", PASSWORD);
        environment.setProperty("db.url.0", JDBC_URL);
        List<HikariDataSource> dataSources =
            new ExternalDataSourceProperties().build(environment, (dataSource -> {
                assertEquals(dataSource.getJdbcUrl(), expectedDataSource.getJdbcUrl());
                assertEquals(dataSource.getUsername(), expectedDataSource.getUsername());
                assertEquals(dataSource.getPassword(), expectedDataSource.getPassword());
                
            }));
        assertEquals(1, dataSources.size());
    }

    @Test
    void externalDatasourceShouldInferDriverFromJdbcUrl() {
        assertEquals("com.mysql.cj.jdbc.Driver", ExternalDataSourceProperties.resolveDriverName(null, JDBC_URL));
        assertEquals("org.mariadb.jdbc.Driver",
                ExternalDataSourceProperties.resolveDriverName(null, MARIADB_JDBC_URL));
        assertEquals("oracle.jdbc.OracleDriver", ExternalDataSourceProperties.resolveDriverName(null, ORACLE_JDBC_URL));
        assertEquals("org.postgresql.Driver", ExternalDataSourceProperties.resolveDriverName(null, POSTGRESQL_JDBC_URL));
        assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver",
                ExternalDataSourceProperties.resolveDriverName(null, SQLSERVER_JDBC_URL));
    }

    @Test
    void externalDatasourceShouldPreferConfiguredDriverName() {
        assertEquals("custom.Driver",
                ExternalDataSourceProperties.resolveDriverName("custom.Driver", MARIADB_JDBC_URL));
    }
    
    @Test
    void externalDatasourceToAssertMultiJdbcUrl() {
        
        HikariDataSource expectedDataSource = new HikariDataSource();
        expectedDataSource.setJdbcUrl(JDBC_URL);
        expectedDataSource.setUsername(USERNAME);
        expectedDataSource.setPassword(PASSWORD);
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("db.num", "2");
        environment.setProperty("db.user", USERNAME);
        environment.setProperty("db.password", PASSWORD);
        environment.setProperty("db.url.0", JDBC_URL);
        environment.setProperty("db.url.1", JDBC_URL);
        List<HikariDataSource> dataSources =
            new ExternalDataSourceProperties().build(environment, (dataSource -> {
                assertEquals(dataSource.getJdbcUrl(), expectedDataSource.getJdbcUrl());
                assertEquals(dataSource.getUsername(), expectedDataSource.getUsername());
                assertEquals(dataSource.getPassword(), expectedDataSource.getPassword());
                
            }));
        assertEquals(2, dataSources.size());
    }
    
    @Test
    void externalDatasourceToAssertMultiPasswordAndUsername() {
        
        HikariDataSource expectedDataSource = new HikariDataSource();
        expectedDataSource.setJdbcUrl(JDBC_URL);
        expectedDataSource.setUsername(USERNAME);
        expectedDataSource.setPassword(PASSWORD);
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("db.num", "2");
        environment.setProperty("db.user.0", USERNAME);
        environment.setProperty("db.user.1", USERNAME);
        environment.setProperty("db.password.0", PASSWORD);
        environment.setProperty("db.password.1", PASSWORD);
        environment.setProperty("db.url.0", JDBC_URL);
        environment.setProperty("db.url.1", JDBC_URL);
        List<HikariDataSource> dataSources =
            new ExternalDataSourceProperties().build(environment, (dataSource -> {
                assertEquals(dataSource.getJdbcUrl(), expectedDataSource.getJdbcUrl());
                assertEquals(dataSource.getUsername(), expectedDataSource.getUsername());
                assertEquals(dataSource.getPassword(), expectedDataSource.getPassword());
                
            }));
        assertEquals(2, dataSources.size());
    }
    
    @Test
    void externalDatasourceToAssertMinIdle() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("db.num", "1");
        environment.setProperty("db.user", USERNAME);
        environment.setProperty("db.password", PASSWORD);
        environment.setProperty("db.url.0", JDBC_URL);
        List<HikariDataSource> dataSources =
            new ExternalDataSourceProperties().build(environment, (dataSource -> {
                dataSource.validate();
                assertEquals(DataSourcePoolProperties.DEFAULT_MINIMUM_IDLE,
                    dataSource.getMinimumIdle());
            }));
        assertEquals(1, dataSources.size());
    }
    
    @Test
    void externalDatasourceFailureWithLarkInfo() {
        assertThrows(IllegalArgumentException.class, () -> {
            
            MockEnvironment environment = new MockEnvironment();
            new ExternalDataSourceProperties().build(environment, null);
            
        });
        
    }
    
    @Test
    void externalDatasourceFailureWithErrorInfo() {
        assertThrows(IllegalArgumentException.class, () -> {
            
            HikariDataSource expectedDataSource = new HikariDataSource();
            expectedDataSource.setJdbcUrl(JDBC_URL);
            expectedDataSource.setUsername(USERNAME);
            expectedDataSource.setPassword(PASSWORD);
            MockEnvironment environment = new MockEnvironment();
            // error num of db
            environment.setProperty("db.num", "2");
            environment.setProperty("db.user", USERNAME);
            environment.setProperty("db.password", PASSWORD);
            environment.setProperty("db.url.0", JDBC_URL);
            List<HikariDataSource> dataSources =
                new ExternalDataSourceProperties().build(environment, (dataSource -> {
                    assertEquals(dataSource.getJdbcUrl(), expectedDataSource.getJdbcUrl());
                    assertEquals(dataSource.getUsername(), expectedDataSource.getUsername());
                    assertEquals(dataSource.getPassword(), expectedDataSource.getPassword());
                    
                }));
        });
    }

}
