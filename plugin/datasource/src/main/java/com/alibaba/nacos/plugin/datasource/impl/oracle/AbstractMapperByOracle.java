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

import com.alibaba.nacos.common.utils.UuidUtils;
import com.alibaba.nacos.plugin.datasource.enums.derby.TrustedDerbylFunctionEnum;
import com.alibaba.nacos.plugin.datasource.enums.oracle.TrustedOracleFunctionEnum;
import com.alibaba.nacos.plugin.datasource.mapper.AbstractMapper;

import java.util.List;

/**
 * The abstract derby mapper contains CRUD methods.
 *
 * @author blake.qiu
 **/
public abstract class AbstractMapperByOracle extends AbstractMapper {

    @Override
    public String getFunction(String functionName) {
        return TrustedOracleFunctionEnum.getFunctionByName(functionName);
    }

    @Override
    public String insert(List<String> columns) {
        StringBuilder sql = new StringBuilder();
        String method = "INSERT INTO ";
        sql.append(method);
        sql.append(getTableName());

        int size = columns.size();
        sql.append("(");
        for (int i = 0; i < size; i++) {
            sql.append(columns.get(i).split("@")[0]);
            sql.append(", ");
        }
        sql.append("id");
        sql.append(") ");

        sql.append("VALUES");
        sql.append("(");
        for (int i = 0; i < size; i++) {
            String[] parts = columns.get(i).split("@");
            if (parts.length == 2) {
                sql.append(getFunction(parts[1]));
            } else {
                sql.append("?");
            }
            sql.append(",");
        }
        sql.append(UuidUtils.nextId());
        sql.append(")");
        return sql.toString();
    }

}
