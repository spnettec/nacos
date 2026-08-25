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

package com.alibaba.nacos.core.aot;

import com.alibaba.nacos.consistency.snapshot.LocalFileMeta;
import com.alibaba.nacos.core.plugin.model.PluginStateSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.SerializationHints;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NacosCoreRuntimeHintsTest {
    
    @Mock
    RuntimeHints runtimeHints;
    
    @Mock
    ReflectionHints reflectionHints;
    
    @Mock
    SerializationHints serializationHints;
    
    NacosCoreRuntimeHints nacosCoreRuntimeHints;
    
    @BeforeEach
    void setUp() {
        nacosCoreRuntimeHints = new NacosCoreRuntimeHints();
        when(runtimeHints.reflection()).thenReturn(reflectionHints);
        when(runtimeHints.serialization()).thenReturn(serializationHints);
    }
    
    @Test
    void registerHints() {
        nacosCoreRuntimeHints.registerHints(runtimeHints, null);
        
        verify(reflectionHints).registerType(LocalFileMeta.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INTROSPECT_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS);
        verify(reflectionHints).registerType(PluginStateSnapshot.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INTROSPECT_PUBLIC_METHODS, MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS);
        verify(serializationHints).registerType(PluginStateSnapshot.class);
    }
}
