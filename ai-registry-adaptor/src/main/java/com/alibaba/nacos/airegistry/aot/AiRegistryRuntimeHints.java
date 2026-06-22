/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.airegistry.aot;

import com.alibaba.nacos.airegistry.form.GetServerForm;
import com.alibaba.nacos.airegistry.form.ListServerForm;
import com.alibaba.nacos.airegistry.form.ListServersNacosForm;
import com.alibaba.nacos.airegistry.form.ListServersOfficialForm;
import com.alibaba.nacos.airegistry.form.SkillsFileQueryForm;
import com.alibaba.nacos.airegistry.form.SkillsSearchForm;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.util.stream.Stream;

/**
 * Runtime hints for AI registry web request binding.
 *
 * @author heyoulin
 */
public class AiRegistryRuntimeHints implements RuntimeHintsRegistrar {
    
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        Stream
            .of(GetServerForm.class, ListServerForm.class, ListServersNacosForm.class,
                ListServersOfficialForm.class, SkillsFileQueryForm.class, SkillsSearchForm.class)
            .forEach(type -> hints.reflection()
                .registerType(type, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INTROSPECT_PUBLIC_METHODS,
                    MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.DECLARED_FIELDS));
    }
}
