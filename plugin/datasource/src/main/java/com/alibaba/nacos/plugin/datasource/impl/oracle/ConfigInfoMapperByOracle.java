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
import com.alibaba.nacos.common.utils.NamespaceUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.common.utils.UuidUtils;
import com.alibaba.nacos.plugin.datasource.constants.ContextConstant;
import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.mapper.ConfigInfoMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The mysql implementation of ConfigInfoMapper.
 *
 * @author hyx
 **/

public class ConfigInfoMapperByOracle extends AbstractMapperByOracle implements ConfigInfoMapper {

    @Override
    public MapperResult findConfigInfoByAppFetchRows(MapperContext context) {
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String tenantId = (String) context.getWhereParameter(FieldConstant.TENANT_ID);

        String sql =
                "SELECT ID,data_id,group_id,tenant_id,app_name,content FROM config_info WHERE tenant_id LIKE ? AND "
                        + "app_name = ?" + " OFFSET " + context.getStartRow() + " ROWS FETCH NEXT "
                        + context.getPageSize() + " ROWS ONLY";

        return new MapperResult(sql, CollectionUtils.list(tenantId, appName));
    }

    @Override
    public MapperResult getTenantIdList(MapperContext context) {
        return new MapperResult(
                "SELECT tenant_id FROM config_info WHERE tenant_id != '" + NamespaceUtil.getNamespaceDefaultId()
                        + "' GROUP BY tenant_id OFFSET " + context.getStartRow() + " ROWS FETCH NEXT "
                        + context.getPageSize() + " ROWS ONLY", Collections.emptyList());
    }

    @Override
    public MapperResult getGroupIdList(MapperContext context) {
        return new MapperResult(
                "SELECT group_id FROM config_info WHERE tenant_id ='" + NamespaceUtil.getNamespaceDefaultId()
                        + "' GROUP BY group_id OFFSET " + context.getStartRow() + " ROWS FETCH NEXT "
                        + context.getPageSize() + " ROWS ONLY", Collections.emptyList());
    }

    @Override
    public MapperResult findAllConfigKey(MapperContext context) {
        String sql = " SELECT data_id,group_id,app_name FROM "
                + " ( SELECT id FROM config_info WHERE tenant_id LIKE ? ORDER BY id OFFSET " + context.getStartRow()
                + " ROWS FETCH NEXT " + context.getPageSize() + " ROWS ONLY ) "
                + "g, config_info t  WHERE g.id = t.id ";
        return new MapperResult(sql, CollectionUtils.list(context.getWhereParameter(FieldConstant.TENANT_ID)));
    }

    @Override
    public MapperResult findAllConfigInfoBaseFetchRows(MapperContext context) {
        return new MapperResult(
                "SELECT t.id,data_id,group_id,content,md5 " + " FROM ( SELECT id FROM config_info ORDER BY id OFFSET "
                        + context.getStartRow() + " ROWS FETCH NEXT " + context.getPageSize() + " ROWS ONLY )  "
                        + " g, config_info t WHERE g.id = t.id ", Collections.emptyList());
    }

    @Override
    public MapperResult findAllConfigInfoFragment(MapperContext context) {
        String contextParameter = context.getContextParameter(ContextConstant.NEED_CONTENT);
        boolean needContent = contextParameter != null && Boolean.parseBoolean(contextParameter);
        return new MapperResult("SELECT id,data_id,group_id,tenant_id,app_name," + (needContent ? "content," : "")
                + "md5,gmt_modified,type FROM config_info WHERE id > ? " + "ORDER BY id ASC OFFSET "
                + context.getStartRow() + " ROWS FETCH NEXT " + context.getPageSize() + " ROWS ONLY",
                CollectionUtils.list(context.getWhereParameter(FieldConstant.ID)));
    }

    @Override
    public MapperResult findChangeConfigFetchRows(MapperContext context) {
        final String tenant = (String) context.getWhereParameter(FieldConstant.TENANT);
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);

        final Timestamp startTime = (Timestamp) context.getWhereParameter(FieldConstant.START_TIME);
        final Timestamp endTime = (Timestamp) context.getWhereParameter(FieldConstant.END_TIME);

        List<Object> paramList = new ArrayList<>();

        final String sqlFetchRows = "SELECT id,data_id,group_id,tenant_id,app_name,content,type,md5,gmt_modified FROM"
                + " config_info WHERE ";
        String where = " 1=1 ";

        if (!StringUtils.isBlank(dataId)) {
            where += " AND data_id LIKE ? ";
            paramList.add(dataId);
        }
        if (!StringUtils.isBlank(group)) {
            where += " AND group_id LIKE ? ";
            paramList.add(group);
        }

        if (!StringUtils.isBlank(tenant)) {
            where += " AND tenant_id = ? ";
            paramList.add(tenant);
        }

        if (!StringUtils.isBlank(appName)) {
            where += " AND app_name = ? ";
            paramList.add(appName);
        }
        if (startTime != null) {
            where += " AND gmt_modified >=? ";
            paramList.add(startTime);
        }
        if (endTime != null) {
            where += " AND gmt_modified <=? ";
            paramList.add(endTime);
        }
        return new MapperResult(
                sqlFetchRows + where + " OFFSET " + context.getStartRow() + " ROWS FETCH NEXT " + context.getPageSize()
                        + " ROWS ONLY", paramList);
    }

    @Override
    public MapperResult listGroupKeyMd5ByPageFetchRows(MapperContext context) {
        return new MapperResult(" SELECT t.id,data_id,group_id,tenant_id,app_name,type,md5,gmt_modified "
                + "FROM ( SELECT id FROM config_info ORDER BY id OFFSET " + context.getStartRow() + " ROWS FETCH NEXT "
                + context.getPageSize() + " ROWS ONLY ) g, config_info t WHERE g.id = t.id", Collections.emptyList());
    }

    @Override
    public MapperResult findConfigInfoBaseLikeFetchRows(MapperContext context) {
        final String tenant = (String) context.getWhereParameter(FieldConstant.TENANT);
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);

        List<Object> paramList = new ArrayList<>();
        final String sqlFetchRows = "SELECT id,data_id,group_id,tenant_id,content FROM config_info WHERE ";
        String where = " 1=1 AND tenant_id='" + NamespaceUtil.getNamespaceDefaultId() + "' ";
        if (!StringUtils.isBlank(dataId)) {
            where += " AND data_id LIKE ? ";
            paramList.add(dataId);
        }
        if (!StringUtils.isBlank(group)) {
            where += " AND group_id LIKE ? ";
            paramList.add(group);
        }
        if (!StringUtils.isBlank(tenant)) {
            where += " AND content LIKE ? ";
            paramList.add(tenant);
        }
        return new MapperResult(
                sqlFetchRows + where + " OFFSET " + context.getStartRow() + " ROWS FETCH NEXT " + context.getPageSize()
                        + " ROWS ONLY", paramList);
    }

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

        final String sql = "SELECT id,data_id,group_id,tenant_id,app_name,content,md5,type FROM config_info";
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
    public MapperResult findConfigInfoBaseByGroupFetchRows(MapperContext context) {
        return null;
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

        final String sqlFetchRows = "SELECT id,data_id,group_id,tenant_id,app_name,content,md5,encrypted_data_key FROM config_info";
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
    public MapperResult updateConfigInfoAtomicCas(MapperContext context) {
        List<Object> paramList = new ArrayList<>();

        paramList.add(context.getUpdateParameter(FieldConstant.CONTENT));
        paramList.add(context.getUpdateParameter(FieldConstant.MD5));
        paramList.add(context.getUpdateParameter(FieldConstant.SRC_IP));
        paramList.add(context.getUpdateParameter(FieldConstant.SRC_USER));
        paramList.add(context.getUpdateParameter(FieldConstant.APP_NAME));
        paramList.add(context.getUpdateParameter(FieldConstant.C_DESC));
        paramList.add(context.getUpdateParameter(FieldConstant.C_USE));
        paramList.add(context.getUpdateParameter(FieldConstant.EFFECT));
        paramList.add(context.getUpdateParameter(FieldConstant.TYPE));
        paramList.add(context.getUpdateParameter(FieldConstant.C_SCHEMA));
        paramList.add(context.getUpdateParameter(FieldConstant.ENCRYPTED_DATA_KEY));
        paramList.add(context.getWhereParameter(FieldConstant.DATA_ID));
        paramList.add(context.getWhereParameter(FieldConstant.GROUP_ID));
        String sql = "UPDATE config_info SET " + "content=?, md5=?, src_ip=?, src_user=?, gmt_modified="
                + "(SELECT SYSTIMESTAMP FROM DUAL)"
                + ", app_name=?, c_desc=?, c_use=?, effect=?, type=?, c_schema=?, encrypted_data_key=? "
                + "WHERE data_id=? AND group_id=? AND tenant_id=? AND (md5=? OR md5 IS NULL OR md5='')";
        String tenantTmp = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        if(StringUtils.isNotBlank(tenantTmp)) {
            paramList.add(context.getWhereParameter(FieldConstant.TENANT_ID));
        } else{
            sql = "UPDATE config_info SET " + "content=?, md5=?, src_ip=?, src_user=?, gmt_modified="
                    + "(SELECT SYSTIMESTAMP FROM DUAL)"
                    + ", app_name=?, c_desc=?, c_use=?, effect=?, type=?, c_schema=?, encrypted_data_key=? "
                    +  "WHERE data_id=? AND group_id=? AND tenant_id is NULL AND (md5=? OR md5 IS NULL OR md5='')";
        }
        paramList.add(context.getWhereParameter(FieldConstant.MD5));

        return new MapperResult(sql, paramList);
    }

    @Override
    public String getDataSource() {
        return DataSourceConstant.ORACLE;
    }
}
