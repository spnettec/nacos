/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.impl.derby.ConfigInfoMapperByDerby;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.util.ArrayList;
import java.util.List;

/**
 * The mysql implementation of ConfigInfoMapper.
 *
 * @author hyx
 **/

public class ConfigInfoMapperByOracle extends ConfigInfoMapperByDerby {
    @Override
    public MapperResult findConfigInfo4PageCountRows(MapperContext context) {
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String content = (String) context.getWhereParameter(FieldConstant.CONTENT);
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String tenantId = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        final List<Object> paramList = new ArrayList<>();

        final String sqlCount = "SELECT count(*) FROM config_info";
        StringBuilder where = new StringBuilder(" WHERE ");
        if (StringUtils.isNotBlank(dataId)) {
            where.append(" tenant_id=? ");
            paramList.add(tenantId);
        } else{
            where.append(" tenant_id is NULL ");
        }
        if (StringUtils.isNotBlank(dataId)) {
            where.append(" AND data_id=? ");
            paramList.add(dataId);
        }
        if (StringUtils.isNotBlank(group)) {
            where.append(" AND group_id=? ");
            paramList.add(group);
        }
        if (StringUtils.isNotBlank(appName)) {
            where.append(" AND app_name=? ");
            paramList.add(appName);
        }
        if (!StringUtils.isBlank(content)) {
            where.append(" AND content LIKE ? ");
            paramList.add(content);
        }
        return new MapperResult(sqlCount + where, paramList);
    }

    @Override
    public MapperResult findConfigInfo4PageFetchRows(MapperContext context) {
        final String tenantId = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String content = (String) context.getWhereParameter(FieldConstant.CONTENT);

        List<Object> paramList = new ArrayList<>();

        final String sql = "SELECT id,data_id,group_id,tenant_id,app_name,content,type FROM config_info";
        StringBuilder where = new StringBuilder(" WHERE ");
        if (StringUtils.isNotBlank(dataId)) {
            where.append(" tenant_id=? ");
            paramList.add(tenantId);
        } else{
            where.append(" tenant_id is NULL ");
        }
        if (StringUtils.isNotBlank(dataId)) {
            where.append(" AND data_id=? ");
            paramList.add(dataId);
        }
        if (StringUtils.isNotBlank(group)) {
            where.append(" AND group_id=? ");
            paramList.add(group);
        }

        if (StringUtils.isNotBlank(appName)) {
            where.append(" AND app_name=? ");
            paramList.add(appName);
        }
        if (!StringUtils.isBlank(content)) {
            where.append(" AND content LIKE ? ");
            paramList.add(content);
        }
        return new MapperResult(
                sql + where + " OFFSET " + context.getStartRow() + " ROWS FETCH NEXT " + context.getPageSize()
                        + " ROWS ONLY", paramList);
    }


    @Override
    public MapperResult findConfigInfoLike4PageCountRows(MapperContext context) {
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String content = (String) context.getWhereParameter(FieldConstant.CONTENT);
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String tenantId = (String) context.getWhereParameter(FieldConstant.TENANT_ID);

        final List<Object> paramList = new ArrayList<>();

        final String sqlCountRows = "SELECT count(*) FROM config_info";
        StringBuilder where = new StringBuilder(" WHERE ");
        if (StringUtils.isNotBlank(tenantId)) {
            where.append(" tenant_id LIKE ? ");
            paramList.add(tenantId);
        } else{
            where.append(" tenant_id is NULL ");
        }
        if (!StringUtils.isBlank(dataId)) {
            where.append(" AND data_id LIKE ? ");
            paramList.add(dataId);
        }
        if (!StringUtils.isBlank(group)) {
            where.append(" AND group_id LIKE ? ");
            paramList.add(group);
        }
        if (!StringUtils.isBlank(appName)) {
            where.append(" AND app_name = ? ");
            paramList.add(appName);
        }
        if (!StringUtils.isBlank(content)) {
            where.append(" AND content LIKE ? ");
            paramList.add(content);
        }
        return new MapperResult(sqlCountRows + where, paramList);
    }
    @Override
    public MapperResult findConfigInfoLike4PageFetchRows(MapperContext context) {

        final String tenantId = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String content = (String) context.getWhereParameter(FieldConstant.CONTENT);

        List<Object> paramList = new ArrayList<>();

        final String sqlFetchRows = "SELECT id,data_id,group_id,tenant_id,app_name,content,encrypted_data_key FROM config_info";
        StringBuilder where = new StringBuilder(" WHERE ");
        if (StringUtils.isNotBlank(dataId)) {
            where.append(" tenant_id=? ");
            paramList.add(tenantId);
        } else{
            where.append(" tenant_id is NULL ");
        }
        if (!StringUtils.isBlank(dataId)) {
            where.append(" AND data_id LIKE ? ");
            paramList.add(dataId);
        }
        if (!StringUtils.isBlank(group)) {
            where.append(" AND group_id LIKE ? ");
            paramList.add(group);
        }
        if (!StringUtils.isBlank(appName)) {
            where.append(" AND app_name = ? ");
            paramList.add(appName);
        }
        if (!StringUtils.isBlank(content)) {
            where.append(" AND content LIKE ? ");
            paramList.add(content);
        }
        String sql =
                sqlFetchRows + where + " OFFSET " + context.getStartRow() + " ROWS FETCH NEXT " + context.getPageSize()
                        + " ROWS ONLY";
        return new MapperResult(sql, paramList);
    }

    @Override
    public MapperResult findAllConfigInfoFetchRows(MapperContext context) {
        final String tenantId = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        String sql = " SELECT t.id,data_id,group_id,tenant_id,app_name,content,md5 "
                + " FROM ( SELECT id FROM config_info  WHERE tenant_id IS NULL ORDER BY id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY )"
                + " g, config_info t  WHERE g.id = t.id ";
        List<Object> parasList = CollectionUtils.list(context.getStartRow(),
                context.getPageSize());
        if (StringUtils.isNotBlank(tenantId)) {
            sql = sql.replace("tenant_id IS NULL","tenant_id LIKE ?");
            parasList.add(0,tenantId);
        }
        return new MapperResult(sql,
                CollectionUtils.list(context.getStartRow(),
                        context.getPageSize()));
    }

    @Override
    public String getDataSource() {
        return DataSourceConstant.ORACLE;
    }
}
