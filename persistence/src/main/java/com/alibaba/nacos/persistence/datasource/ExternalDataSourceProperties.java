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

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.Preconditions;
import com.alibaba.nacos.common.utils.StringUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.util.ObjectUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.alibaba.nacos.common.utils.CollectionUtils.getOrDefault;

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

    private Integer num;

    private List<String> url = new ArrayList<>();

    private List<String> user = new ArrayList<>();

    private String driverName;

    private String testQuery;

    private List<String> password = new ArrayList<>();

    public void setNum(Integer num) {
        this.num = num;
    }

    public void setUrl(List<String> url) {
        this.url = url;
    }

    public void setUser(List<String> user) {
        this.user = user;
    }

    public void setPassword(List<String> password) {
        this.password = password;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public void setTestQuery(String testQuery) {
        this.testQuery = testQuery;
    }

    /**
     * Build serveral HikariDataSource.
     *
     * @param environment {@link Environment}
     * @param callback    Callback function when constructing data source
     * @return List of {@link HikariDataSource}
     */
    List<HikariDataSource> build(Environment environment, Callback<HikariDataSource> callback) {
        List<HikariDataSource> dataSources = new ArrayList<>();
        Binder.get(environment).bind("db", Bindable.ofInstance(this));
        applyEnvironmentFallbacks(environment);
        Preconditions.checkArgument(Objects.nonNull(num), "db.num is null");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(user),
                "db.user or db.user.[index] is null");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(password),
                "db.password or db.password.[index] is null");
        for (int index = 0; index < num; index++) {
            int currentSize = index + 1;
            Preconditions.checkArgument(url.size() >= currentSize, "db.url.%s is null", index);
            DataSourcePoolProperties poolProperties = DataSourcePoolProperties.build(environment);
            String jdbcUrl = url.get(index).trim();
            if (StringUtils.isEmpty(poolProperties.getDataSource().getDriverClassName())) {
                poolProperties.setDriverClassName(resolveDriverName(driverName, jdbcUrl));
            }
            poolProperties.setJdbcUrl(jdbcUrl);
            poolProperties.setUsername(getOrDefault(user, index, user.get(0)).trim());
            poolProperties.setPassword(resolveNacosEncPassword(getOrDefault(password, index, password.get(0)).trim()));
            HikariDataSource ds = poolProperties.getDataSource();
            if (StringUtils.isEmpty(ds.getConnectionTestQuery())) {
                poolProperties.setTestQuery(ObjectUtils.isEmpty(testQuery) ? TEST_QUERY : testQuery);
            }

            dataSources.add(ds);
            callback.accept(ds);
        }
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(dataSources),
                "no datasource available");
        return dataSources;
    }

    private void applyEnvironmentFallbacks(Environment environment) {
        String dbType = environment.getProperty("DB_TYPE", "NONE").toUpperCase(Locale.ROOT);
        String platformPrefix = dbType + ".";
        String fallbackUrl = firstText(environment.getProperty("DB_URL"), environment.getProperty(platformPrefix + "URL"),
                environment.getProperty("db.url[0]"), environment.getProperty("db.url.0"));
        String fallbackUser = firstText(environment.getProperty("DB_USER"), environment.getProperty(platformPrefix + "USER"),
                environment.getProperty("db.user[0]"), environment.getProperty("db.user.0"));
        String fallbackPassword = firstText(environment.getProperty("DB_PWD"), environment.getProperty("DB_PASSWORD"),
                environment.getProperty(platformPrefix + "PASSWORD"), environment.getProperty("db.password[0]"),
                environment.getProperty("db.password.0"));
        String fallbackDriverName = firstText(environment.getProperty("DB_DRIVER_NAME"),
                environment.getProperty(platformPrefix + "DRIVER_NAME"), environment.getProperty("db.driver-name"),
                environment.getProperty("db.driverName"));
        String fallbackTestQuery = firstText(environment.getProperty("DB_TEST_QUERY"),
                environment.getProperty(platformPrefix + "TEST_QURTY"), environment.getProperty(platformPrefix + "TEST_QUERY"),
                environment.getProperty("db.test-query"), environment.getProperty("db.testQuery"));
        if (hasText(fallbackUrl) && !hasUsableValue(url)) {
            url = Collections.singletonList(fallbackUrl);
        }
        if (hasText(fallbackUser)) {
            user = Collections.singletonList(fallbackUser);
        }
        if (hasText(fallbackPassword)) {
            password = Collections.singletonList(fallbackPassword);
        }
        if (hasText(fallbackDriverName) && !hasText(driverName)) {
            driverName = fallbackDriverName;
        }
        if (hasText(fallbackTestQuery) && !hasText(testQuery)) {
            testQuery = fallbackTestQuery;
        }
    }

    private static boolean hasUsableValue(List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return false;
        }
        String value = values.get(0);
        return hasText(value) && !value.startsWith("${");
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
