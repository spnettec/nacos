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
import com.alibaba.nacos.plugin.datasource.impl.derby.HistoryConfigInfoMapperByDerby;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.util.ArrayList;
import java.util.List;

/**
 * The mysql implementation of HistoryConfigInfoMapper.
 *
 * @author hyx
 **/

public class HistoryConfigInfoMapperByOracle extends HistoryConfigInfoMapperByDerby {

    @Override
    public MapperResult pageFindConfigHistoryFetchRows(MapperContext context) {
        String sql =
                "SELECT nid,data_id,group_id,tenant_id,app_name,src_ip,src_user,op_type,gmt_create,gmt_modified FROM his_config_info "
                        + "WHERE data_id is NULL AND group_id is NULL AND tenant_id is NULL ORDER BY nid DESC  OFFSET "
                        + context.getStartRow() + " ROWS FETCH NEXT " + context.getPageSize() + " ROWS ONLY";
        List<Object> paraList = new ArrayList<>();
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String groupId = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String tenantId = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        if (StringUtils.isNotBlank(dataId)) {
            sql = sql.replace("data_id is NULL","data_id = ?");
            paraList.add(dataId);
        }
        if (StringUtils.isNotBlank(groupId)) {
            sql = sql.replace("group_id is NULL","group_id = ?");
            paraList.add(groupId);
        }
        if (StringUtils.isNotBlank(tenantId)) {
            sql = sql.replace("tenant_id is NULL","tenant_id = ?");
            paraList.add(tenantId);
        }
        return new MapperResult(sql, paraList);
    }
    @Override
    public String getDataSource() {
        return DataSourceConstant.ORACLE;
    }
}
