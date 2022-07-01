/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.grpc.auto;

public interface PayloadOrBuilder extends
    // @@protoc_insertion_point(interface_extends:Payload)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.Metadata metadata = 2;</code>
   * @return Whether the metadata field is set.
   */
  boolean hasMetadata();
  /**
   * <code>.Metadata metadata = 2;</code>
   * @return The metadata.
   */
  Metadata getMetadata();
  /**
   * <code>.Metadata metadata = 2;</code>
   */
  MetadataOrBuilder getMetadataOrBuilder();

  /**
   * <code>.google.protobuf.Any body = 3;</code>
   * @return Whether the body field is set.
   */
  boolean hasBody();
  /**
   * <code>.google.protobuf.Any body = 3;</code>
   * @return The body.
   */
  com.google.protobuf.Any getBody();
  /**
   * <code>.google.protobuf.Any body = 3;</code>
   */
  com.google.protobuf.AnyOrBuilder getBodyOrBuilder();
}
