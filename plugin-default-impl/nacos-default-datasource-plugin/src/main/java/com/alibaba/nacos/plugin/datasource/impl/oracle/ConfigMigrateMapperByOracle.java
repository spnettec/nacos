/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.datasource.impl.oracle;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.mapper.ConfigMigrateMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Config migrate mapper by derby.
 *
 * @author Sunrisea
 */
public class ConfigMigrateMapperByOracle extends AbstractMapperByOracle implements ConfigMigrateMapper {

    @Override
    public MapperResult findConfigIdNeedInsertMigrate(MapperContext context) {
        String sql = "SELECT ci.id FROM config_info ci WHERE ci.tenant_id is NULL AND NOT EXISTS "
                + " ( SELECT 1 FROM config_info ci2  WHERE ci2.data_id = ci.data_id AND ci2.group_id = ci.group_id AND ci2.tenant_id = 'public' )"
                + " AND ci.id > ?" + " ORDER BY ci.id OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        return new MapperResult(sql, CollectionUtils.list(context.getWhereParameter(FieldConstant.ID),
                context.getPageSize()));
    }

    @Override
    public MapperResult findConfigNeedUpdateMigrate(MapperContext context) {
        String sql = "SELECT ci.id, ci.data_id, ci.group_id, ci.tenant_id"
                + " FROM config_info ci WHERE ci.tenant_id = ? AND "
                + " (ci.src_user <> ? OR ci.src_user IS NULL) AND EXISTS "
                + " ( SELECT 1 FROM config_info ci2 WHERE ci2.data_id = ci.data_id AND ci2.group_id = ci.group_id "
                + " AND ci2.tenant_id = ? AND ci2.src_user = ? AND ci2.md5 <> ci.md5 "
                + " AND ci2.gmt_modified < ci.gmt_modified )"
                + " AND ci.id > ?" + " ORDER BY ci.id OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        return new MapperResult(sql,
                CollectionUtils.list(context.getWhereParameter(FieldConstant.SRC_TENANT),
                        context.getWhereParameter(FieldConstant.SRC_USER),
                        context.getWhereParameter(FieldConstant.TARGET_TENANT),
                        context.getWhereParameter(FieldConstant.SRC_USER), context.getWhereParameter(FieldConstant.ID),
                        context.getPageSize()));
    }

    @Override
    public MapperResult findConfigGrayIdNeedInsertMigrate(MapperContext context) {
        String sql = "SELECT ci.id FROM config_info_gray ci WHERE ci.tenant_id is NULL AND NOT EXISTS "
                + " ( SELECT 1 FROM config_info_gray ci2  WHERE ci2.data_id = ci.data_id AND ci2.group_id = ci.group_id"
                + " AND ci2.tenant_id = 'public' AND ci2.gray_name = ci.gray_name)" + " AND ci.id > ?"
                + " ORDER BY ci.id OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        return new MapperResult(sql, CollectionUtils.list(context.getWhereParameter(FieldConstant.ID),
                context.getPageSize()));
    }

    @Override
    public MapperResult findConfigGrayNeedUpdateMigrate(MapperContext context) {
        String sql = "SELECT ci.id, ci.data_id, ci.group_id, ci.tenant_id, ci.gray_name "
                + " FROM config_info_gray ci WHERE ci.tenant_id = ? AND "
                + " (ci.src_user <> ? OR ci.src_user IS NULL) AND EXISTS "
                + " ( SELECT 1 FROM config_info_gray ci2 WHERE ci2.data_id = ci.data_id AND ci2.group_id = ci.group_id "
                + " AND ci2.gray_name = ci.gray_name AND ci2.tenant_id = ? AND ci2.src_user = ? AND ci2.md5 <> ci.md5 "
                + " AND ci2.gmt_modified < ci.gmt_modified )"
                + " AND ci.id > ?" + " ORDER BY ci.id OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";
        return new MapperResult(sql,
                CollectionUtils.list(context.getWhereParameter(FieldConstant.SRC_TENANT),
                        context.getWhereParameter(FieldConstant.SRC_USER),
                        context.getWhereParameter(FieldConstant.TARGET_TENANT),
                        context.getWhereParameter(FieldConstant.SRC_USER), context.getWhereParameter(FieldConstant.ID),
                        context.getPageSize()));
    }

    @Override
    public MapperResult migrateConfigInsertByIds(MapperContext context) {
        ArrayList<Object> paramList = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "INSERT INTO config_info (id, data_id, group_id, content, md5, src_user, src_ip, "
                        + "app_name, tenant_id, c_desc, type, encrypted_data_key, gmt_create, gmt_modified) "
                        + "select trunc(dbms_random.value(0,999999999999999999)), "
                        + "data_id, group_id, content, md5, ?, src_ip, "
                        + "app_name, 'public', c_desc, type, encrypted_data_key, sysdate, sysdate from config_info WHERE ");
        sql.append("id IN (");
        List<Long> ids = (List<Long>) context.getWhereParameter(FieldConstant.IDS);
        paramList.add(context.getWhereParameter(FieldConstant.SRC_USER));
        for (int i = 0; i < ids.size(); i++) {
            sql.append("? ");
            if (i < ids.size() - 1) {
                sql.append(", ");
            }
            paramList.add(ids.get(i));
        }
        sql.append(") ");
        return new MapperResult(sql.toString(), paramList);
    }

    @Override
    public MapperResult migrateConfigGrayInsertByIds(MapperContext context) {
        StringBuilder sql = new StringBuilder(
                "INSERT INTO config_info_gray (id, data_id, group_id, content, md5, src_user, src_ip, "
                        + "app_name, tenant_id, gray_name, gray_rule, encrypted_data_key, gmt_create, gmt_modified) "
                        + "select trunc(dbms_random.value(0,999999999999999999)), "
                        + "data_id, group_id, "
                        + "content, md5, ?, src_ip, "
                        + "app_name, 'public', gray_name, gray_rule, encrypted_data_key, sysdate, sysdate from config_info_gray WHERE ");
        sql.append("id IN (");
        ArrayList<Object> paramList = new ArrayList<>();
        List<Long> ids = (List<Long>) context.getWhereParameter(FieldConstant.IDS);
        paramList.add(context.getWhereParameter(FieldConstant.SRC_USER));
        for (int i = 0; i < ids.size(); i++) {
            sql.append("? ");
            if (i < ids.size() - 1) {
                sql.append(", ");
            }
            paramList.add(ids.get(i));
        }
        sql.append(") ");
        return new MapperResult(sql.toString(), paramList);
    }

    @Override
    public String getDataSource() {
        return DataSourceConstant.ORACLE;
    }

}
