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

import com.alibaba.nacos.common.utils.Preconditions;
import com.alibaba.nacos.common.utils.StringUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Properties of external DataSource.
 *
 * @author Nacos
 */
public class ExternalDataSourceProperties {

    private static final String MYSQL_DRIVER_NAME = "com.mysql.cj.jdbc.Driver";

    private static final String MARIADB_DRIVER_NAME = "org.mariadb.jdbc.Driver";

    private static final String ORACLE_DRIVER_NAME = "oracle.jdbc.OracleDriver";

    private static final String POSTGRESQL_DRIVER_NAME = "org.postgresql.Driver";

    private static final String SQLSERVER_DRIVER_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    private static final String TEST_QUERY = "SELECT 1";
    private static final String NACOS_ENC_PREFIX = "NacosEnc(";

    private static final String NACOS_ENC_SUFFIX = ")";
    /**
     * Build serveral HikariDataSource.
     *
     * @param environment {@link Environment}
     * @param callback    Callback function when constructing data source
     * @return List of {@link HikariDataSource}
     */
    List<HikariDataSource> build(Environment environment, Callback<HikariDataSource> callback) {
        List<HikariDataSource> dataSources = new ArrayList<>();
        DatasourceConfigResolver configResolver = new DatasourceConfigResolver(environment);
        Integer num = configResolver.resolve("num", Integer.class);
        Preconditions.checkArgument(Objects.nonNull(num),
            "nacos.plugin.datasource.db.num (legacy db.num) is null");
        String dbType = environment.getProperty("DB_TYPE", "NONE").toUpperCase(Locale.ROOT);
        String platformPrefix = dbType + ".";
        String defaultUser = firstText(configResolver.resolveIndexed("user", 0, true),
                environment.getProperty("DB_USER"), environment.getProperty(platformPrefix + "USER"));
        Preconditions.checkArgument(Objects.nonNull(defaultUser),
            "nacos.plugin.datasource.db.user[.index] (legacy db.user[.index]) is null");
        String defaultPassword = firstText(configResolver.resolveIndexed("password", 0, true),
                environment.getProperty("DB_PWD"), environment.getProperty("DB_PASSWORD"),
                environment.getProperty(platformPrefix + "PASSWORD"));
        Preconditions.checkArgument(Objects.nonNull(defaultPassword),
            "nacos.plugin.datasource.db.password[.index] "
                + "(legacy db.password[.index]) is null");
        String driverName = firstText(configResolver.resolve("driver-name", String.class),
                configResolver.resolve("driverName", String.class), environment.getProperty("DB_DRIVER_NAME"),
                environment.getProperty(platformPrefix + "DRIVER_NAME"));
        String testQuery = firstText(configResolver.resolve("test-query", String.class),
                configResolver.resolve("testQuery", String.class), environment.getProperty("DB_TEST_QUERY"),
                environment.getProperty(platformPrefix + "TEST_QURTY"),
                environment.getProperty(platformPrefix + "TEST_QUERY"));
        for (int index = 0; index < num; index++) {
            String url = firstText(configResolver.resolveIndexed("url", index, false),
                    index == 0 ? environment.getProperty("DB_URL") : null,
                    index == 0 ? environment.getProperty(platformPrefix + "URL") : null);
            Preconditions.checkArgument(Objects.nonNull(url),
                "nacos.plugin.datasource.db.url.%s (legacy db.url.%s) is null", index,
                index);
            String user = firstText(configResolver.resolveIndexed("user", index, true), defaultUser);
            String password = firstText(configResolver.resolveIndexed("password", index, true), defaultPassword);
            DataSourcePoolProperties poolProperties =
                DataSourcePoolProperties.build(configResolver);
            if (StringUtils.isEmpty(poolProperties.getDataSource().getDriverClassName())) {
                poolProperties.setDriverClassName(resolveDriverName(driverName, url));
            }
            poolProperties.setJdbcUrl(url.trim());
            poolProperties.setUsername(user.trim());
            poolProperties.setPassword(resolveNacosEncPassword(password.trim()));
            HikariDataSource ds = poolProperties.getDataSource();
            if (StringUtils.isEmpty(ds.getConnectionTestQuery())) {
                poolProperties.setTestQuery(hasText(testQuery) ? testQuery : TEST_QUERY);
            }

            dataSources.add(ds);
            callback.accept(ds);
        }
        Preconditions.checkArgument(!dataSources.isEmpty(), "no datasource available");
        return dataSources;
    }

    private static String firstText(String... values) {
        for (String value : values) {
            if (hasText(value) && !value.startsWith("${")) {
                return value;
            }
        }
        return null;
    }

    private static boolean hasText(String value) {
        return StringUtils.isNotBlank(value);
    }

    static String resolveDriverName(String configuredDriverName, String jdbcUrl) {
        if (hasText(configuredDriverName)) {
            return configuredDriverName;
        }
        String normalizedUrl = jdbcUrl.toLowerCase(Locale.ROOT);
        if (normalizedUrl.startsWith("jdbc:mariadb:")) {
            return MARIADB_DRIVER_NAME;
        }
        if (normalizedUrl.startsWith("jdbc:oracle:")) {
            return ORACLE_DRIVER_NAME;
        }
        if (normalizedUrl.startsWith("jdbc:postgresql:")) {
            return POSTGRESQL_DRIVER_NAME;
        }
        if (normalizedUrl.startsWith("jdbc:sqlserver:")) {
            return SQLSERVER_DRIVER_NAME;
        }
        return MYSQL_DRIVER_NAME;
    }

    static String resolveNacosEncPassword(String raw) {
        if (raw == null || !raw.startsWith(NACOS_ENC_PREFIX) || !raw.endsWith(NACOS_ENC_SUFFIX)) {
            return raw;
        }
        String cipher = raw.substring(NACOS_ENC_PREFIX.length(), raw.length() - NACOS_ENC_SUFFIX.length());
        byte[] key = loadNacosEncKey();
        try {
            byte[] data = Base64.getDecoder().decode(cipher);
            if (data.length < 16) {
                throw new IllegalArgumentException("NacosEnc ciphertext too short, need at least 16 bytes IV");
            }
            IvParameterSpec iv = new IvParameterSpec(data, 0, 16);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesCipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            byte[] plaintext = aesCipher.doFinal(data, 16, data.length - 16);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt NacosEnc password", e);
        }
    }

    private static byte[] loadNacosEncKey() {
        // 1. file mount (production)
        try {
            Path keyFile = Path.of("/run/secrets/nacos-enc-key");
            if (Files.isReadable(keyFile)) {
                String raw = Files.readString(keyFile).trim();
                if (!raw.isEmpty()) {
                    return sha256(raw);
                }
            }
        } catch (Exception ignored) {
        }
        // 2. plaintext env (dev)
        String envKey = System.getenv("NACOS_ENC_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            return sha256(envKey);
        }
        // 3. base64-encoded env (prod, avoids plaintext in docker inspect)
        String envKeyB64 = System.getenv("NACOS_ENC_KEY_BASE64");
        if (envKeyB64 != null && !envKeyB64.isEmpty()) {
            return sha256(new String(Base64.getDecoder().decode(envKeyB64), StandardCharsets.UTF_8));
        }
        // 4. system property
        String propKey = System.getProperty("nacos.enc.key");
        if (propKey != null && !propKey.isEmpty()) {
            return sha256(propKey);
        }
        throw new IllegalStateException(
                "NacosEnc password found but no key available: set NACOS_ENC_KEY or mount /run/secrets/nacos-enc-key");
    }

    private static byte[] sha256(String input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    interface Callback<D> {

        /**
         * Perform custom logic.
         *
         * @param datasource dataSource.
         */
        void accept(D datasource);
    }
}
