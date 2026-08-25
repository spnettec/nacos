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

package com.alibaba.nacos.console.aot;

import com.alibaba.nacos.api.naming.pojo.maintainer.ServiceView;
import com.alibaba.nacos.console.config.ConsoleWebConfig;
import com.alibaba.nacos.console.filter.NacosConsoleAuthFilter;
import com.alibaba.nacos.console.filter.XssFilter;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Runtime hints for Nacos console native image support.
 *
 * <p>The console module can be deployed independently from the server/core
 * persistence stack, so server-side hints are registered by class name only when
 * the corresponding dependency is present on the actual application classpath.
 *
 * @author Dioxide.CN
 * @author heyoulin
 * @date 2024/8/6
 * @since 2.4.0
 */
@SuppressWarnings("all")
public class NacosRuntimeHints implements RuntimeHintsRegistrar {
    
    private final Class<?>[] directReflectionTypes = {byte.class, byte[].class,
        boolean.class, Object.class, Integer.class,
        String.class, ConcurrentHashMap.class,
        com.sun.management.GarbageCollectorMXBean.class, com.sun.management.GcInfo.class,
        sun.misc.Unsafe.class, java.io.PrintWriter.class, java.lang.Double.class,
        java.lang.management.MemoryUsage.class, java.lang.management.BufferPoolMXBean.class,
        java.lang.management.ClassLoadingMXBean.class,
        java.lang.management.CompilationMXBean.class,
        java.lang.management.MemoryMXBean.class,
        java.lang.management.MemoryManagerMXBean.class,
        java.lang.management.MemoryPoolMXBean.class,
        java.lang.management.MonitorInfo.class,
        java.lang.management.ManagementPermission.class,
        java.lang.management.ThreadMXBean.class,
        java.lang.management.ThreadInfo.class, java.lang.management.LockInfo.class,
        java.lang.System.class, java.lang.Thread.class,
        java.net.InetSocketAddress.class, java.nio.ByteBuffer.class,
        java.nio.channels.SelectableChannel.class,
        java.nio.channels.SocketChannel.class, java.nio.channels.FileChannel.class,
        java.security.AccessController.class, java.sql.Date.class,
        java.sql.Driver.class, java.sql.DriverManager.class, java.sql.Time.class,
        java.sql.Timestamp.class, java.util.concurrent.locks.LockSupport.class,
        java.util.Optional.class, java.util.Properties.class,
        com.google.protobuf.Any.class, com.google.protobuf.Any.Builder.class,
        com.google.protobuf.DescriptorProtos.FieldOptions.class,
        com.google.protobuf.DescriptorProtos.FieldOptions.Builder.class,
        com.google.protobuf.DescriptorProtos.FieldOptions.CType.class,
        com.google.protobuf.DescriptorProtos.FieldOptions.EditionDefault.class,
        com.google.protobuf.DescriptorProtos.FieldOptions.EditionDefault.Builder.class,
        com.google.protobuf.DescriptorProtos.FieldOptions.EditionDefaultOrBuilder.class,
        com.google.protobuf.DescriptorProtos.FieldOptions.FeatureSupport.class,
        com.google.protobuf.DescriptorProtos.FieldOptions.FeatureSupport.Builder.class,
        com.google.protobuf.DescriptorProtos.FieldOptions.FeatureSupportOrBuilder.class,
        com.google.protobuf.DescriptorProtos.FieldOptions.JSType.class,
        com.google.protobuf.DescriptorProtos.FieldOptions.OptionRetention.class,
        com.google.protobuf.DescriptorProtos.FieldOptions.OptionTargetType.class,
        com.google.protobuf.DescriptorProtos.Edition.class,
        com.google.protobuf.DescriptorProtos.FeatureSet.class,
        com.google.protobuf.DescriptorProtos.FeatureSet.Builder.class,
        com.google.protobuf.DescriptorProtos.FeatureSet.EnforceNamingStyle.class,
        com.google.protobuf.DescriptorProtos.FeatureSet.EnumType.class,
        com.google.protobuf.DescriptorProtos.FeatureSet.FieldPresence.class,
        com.google.protobuf.DescriptorProtos.FeatureSet.JsonFormat.class,
        com.google.protobuf.DescriptorProtos.FeatureSet.MessageEncoding.class,
        com.google.protobuf.DescriptorProtos.FeatureSet.RepeatedFieldEncoding.class,
        com.google.protobuf.DescriptorProtos.FeatureSet.Utf8Validation.class,
        com.google.protobuf.DescriptorProtos.FeatureSet.VisibilityFeature.class,
        com.google.protobuf.DescriptorProtos.FeatureSet.VisibilityFeature.Builder.class,
        com.google.protobuf.DescriptorProtos.FeatureSet.VisibilityFeature.DefaultSymbolVisibility.class,
        com.google.protobuf.DescriptorProtos.FeatureSet.VisibilityFeatureOrBuilder.class,
        com.google.protobuf.DescriptorProtos.FeatureSetOrBuilder.class,
        com.google.protobuf.ByteString.class, com.google.protobuf.Message.class,
        com.google.protobuf.CodedInputStream.class,
        com.google.common.util.concurrent.AbstractFuture.class,
        com.google.common.util.concurrent.ListenableFuture.class,
        io.grpc.KnownLength.class, io.grpc.ServerCall.class, io.grpc.ServerBuilder.class,
        io.grpc.stub.ClientCalls.class, io.grpc.stub.ServerCalls.class,
        io.grpc.stub.ServerCallStreamObserver.class, io.grpc.stub.CallStreamObserver.class,
        io.grpc.stub.StreamObserver.class, io.grpc.internal.ReadableBuffers.class,
        io.grpc.internal.ServerImplBuilder.class, io.grpc.internal.ClientStreamListener.class,
        io.grpc.ForwardingServerCall.class,
        io.grpc.ForwardingServerCall.SimpleForwardingServerCall.class,
        io.grpc.netty.shaded.io.netty.bootstrap.ServerBootstrap.class,
        io.grpc.netty.shaded.io.netty.buffer.AbstractByteBufAllocator.class,
        io.grpc.netty.shaded.io.netty.buffer.ByteBuf.class,
        io.grpc.netty.shaded.io.netty.buffer.ByteBufAllocator.class,
        io.grpc.netty.shaded.io.netty.buffer.ByteBufInputStream.class,
        io.grpc.netty.shaded.io.netty.buffer.PooledByteBufAllocator.class,
        io.grpc.netty.shaded.io.netty.buffer.UnpooledByteBufAllocator.class,
        io.grpc.netty.shaded.io.netty.buffer.UnpooledDirectByteBuf.class,
        io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder.class,
        io.grpc.netty.shaded.io.grpc.netty.NettyServerProvider.class,
        io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder.class,
        io.grpc.netty.shaded.io.grpc.netty.NettyChannelProvider.class,
        io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel.class,
        io.grpc.netty.shaded.io.netty.util.AttributeKey.class,
        ConsoleWebConfig.class, NacosConsoleAuthFilter.class, XssFilter.class,
        com.alibaba.nacos.common.notify.SlowEvent.class,
        com.alibaba.nacos.common.packagescan.PackageScan.class,
        com.alibaba.nacos.common.packagescan.DefaultPackageScan.class,
        com.alibaba.nacos.common.remote.client.grpc.GrpcUtils.class,
        com.alibaba.nacos.api.naming.remote.request.AbstractNamingRequest.class,
        com.alibaba.nacos.api.config.remote.request.ConfigBatchListenRequest.class,
        com.alibaba.nacos.api.config.remote.request.ConfigBatchListenRequest.ConfigListenContext.class,
        com.alibaba.nacos.api.config.remote.response.ConfigChangeBatchListenResponse.class,
        com.alibaba.nacos.api.config.remote.response.ConfigChangeBatchListenResponse.ConfigContext.class,
        com.alibaba.nacos.api.remote.response.ClientDetectionResponse.class,
        com.alibaba.nacos.api.remote.response.ConnectResetResponse.class,
        com.alibaba.nacos.api.remote.response.ErrorResponse.class,
        com.alibaba.nacos.api.remote.response.HealthCheckResponse.class,
        com.alibaba.nacos.api.remote.response.Response.class,
        com.alibaba.nacos.api.remote.response.ServerCheckResponse.class,
        com.alibaba.nacos.api.remote.response.ServerLoaderInfoResponse.class,
        com.alibaba.nacos.api.remote.response.ServerReloadResponse.class,
        com.alibaba.nacos.api.remote.response.SetupAckResponse.class,
        com.alibaba.nacos.api.ability.ServerAbilities.class,
        com.alibaba.nacos.api.config.ability.ServerConfigAbility.class,
        com.alibaba.nacos.api.naming.ability.ServerNamingAbility.class,
        com.alibaba.nacos.api.grpc.auto.BiRequestStreamGrpc.class,
        com.alibaba.nacos.api.grpc.auto.BiRequestStreamGrpc.BiRequestStreamBlockingStub.class,
        com.alibaba.nacos.api.grpc.auto.BiRequestStreamGrpc.BiRequestStreamFutureStub.class,
        com.alibaba.nacos.api.grpc.auto.BiRequestStreamGrpc.BiRequestStreamImplBase.class,
        com.alibaba.nacos.api.grpc.auto.BiRequestStreamGrpc.BiRequestStreamStub.class,
        com.alibaba.nacos.api.grpc.auto.Metadata.class,
        com.alibaba.nacos.api.grpc.auto.Metadata.Builder.class,
        com.alibaba.nacos.api.grpc.auto.NacosGrpcService.class,
        com.alibaba.nacos.api.grpc.auto.Payload.class,
        com.alibaba.nacos.api.grpc.auto.Payload.Builder.class,
        com.alibaba.nacos.api.grpc.auto.RequestGrpc.class,
        com.alibaba.nacos.api.grpc.auto.RequestGrpc.RequestStub.class,
        com.alibaba.nacos.api.grpc.auto.RequestGrpc.RequestBlockingStub.class,
        com.alibaba.nacos.api.grpc.auto.RequestGrpc.RequestFutureStub.class,
        com.alibaba.nacos.api.grpc.auto.RequestGrpc.RequestImplBase.class,
        com.alibaba.nacos.api.naming.remote.request.InstanceRequest.class,
        com.alibaba.nacos.api.naming.remote.request.PersistentInstanceRequest.class,
        com.alibaba.nacos.api.naming.remote.request.BatchInstanceRequest.class,
        com.alibaba.nacos.api.naming.remote.request.NotifySubscriberRequest.class,
        com.alibaba.nacos.api.naming.remote.request.ServiceListRequest.class,
        com.alibaba.nacos.api.naming.remote.request.ServiceQueryRequest.class,
        com.alibaba.nacos.api.naming.remote.request.SubscribeServiceRequest.class,
        com.alibaba.nacos.api.naming.remote.response.BatchInstanceResponse.class,
        com.alibaba.nacos.api.naming.remote.response.InstanceResponse.class,
        com.alibaba.nacos.api.naming.remote.response.NotifySubscriberResponse.class,
        com.alibaba.nacos.api.naming.remote.response.QueryServiceResponse.class,
        com.alibaba.nacos.api.naming.remote.response.ServiceListResponse.class,
        com.alibaba.nacos.api.naming.remote.response.SubscribeServiceResponse.class,
        com.alibaba.nacos.api.config.remote.request.ConfigQueryRequest.class,
        com.alibaba.nacos.api.config.remote.request.ClientConfigMetricRequest.class,
        com.alibaba.nacos.api.config.remote.request.ConfigChangeNotifyRequest.class,
        com.alibaba.nacos.api.config.remote.request.ConfigPublishRequest.class,
        com.alibaba.nacos.api.config.remote.request.ConfigRemoveRequest.class,
        com.alibaba.nacos.api.config.remote.request.cluster.ConfigChangeClusterSyncRequest.class,
        com.alibaba.nacos.api.config.remote.response.ClientConfigMetricResponse.class,
        com.alibaba.nacos.api.config.remote.response.ConfigChangeNotifyResponse.class,
        com.alibaba.nacos.api.config.remote.response.ConfigPublishResponse.class,
        com.alibaba.nacos.api.config.remote.response.ConfigQueryResponse.class,
        com.alibaba.nacos.api.config.remote.response.ConfigRemoveResponse.class,
        com.alibaba.nacos.api.config.remote.response.cluster.ConfigChangeClusterSyncResponse.class,
        com.alibaba.nacos.api.remote.request.ClientDetectionRequest.class,
        com.alibaba.nacos.api.remote.request.ConnectionSetupRequest.class,
        com.alibaba.nacos.api.remote.request.ConnectResetRequest.class,
        com.alibaba.nacos.api.remote.request.HealthCheckRequest.class,
        com.alibaba.nacos.api.remote.request.InternalRequest.class,
        com.alibaba.nacos.api.remote.request.PushAckRequest.class,
        com.alibaba.nacos.api.remote.request.Request.class,
        com.alibaba.nacos.api.remote.request.RequestMeta.class,
        com.alibaba.nacos.api.remote.request.ServerCheckRequest.class,
        com.alibaba.nacos.api.remote.request.ServerLoaderInfoRequest.class,
        com.alibaba.nacos.api.remote.request.ServerReloadRequest.class,
        com.alibaba.nacos.api.remote.request.SetupAckRequest.class,
        com.alibaba.nacos.api.naming.pojo.Cluster.class,
        com.alibaba.nacos.api.naming.pojo.Instance.class,
        com.alibaba.nacos.api.naming.pojo.ListView.class,
        com.alibaba.nacos.api.naming.pojo.Service.class,
        com.alibaba.nacos.api.naming.pojo.ServiceInfo.class,
        com.alibaba.nacos.api.naming.pojo.builder.InstanceBuilder.class,
        com.alibaba.nacos.api.naming.pojo.maintainer.ClusterInfo.class,
        com.alibaba.nacos.api.naming.pojo.maintainer.ServiceDetailInfo.class,
        com.alibaba.nacos.api.naming.pojo.healthcheck.AbstractHealthChecker.class,
        com.alibaba.nacos.api.naming.pojo.healthcheck.HealthCheckerFactory.class,
        com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Http.class,
        com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Mysql.class,
        com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Tcp.class,
        com.alibaba.nacos.api.selector.AbstractSelector.class,
        com.alibaba.nacos.api.selector.ExpressionSelector.class,
        com.alibaba.nacos.api.selector.NoneSelector.class,
        ServiceView.class};
    
    private final String[] optionalReflectionTypes = {
        "io.grpc.internal.ServerCallImpl",
        "io.grpc.netty.shaded.io.grpc.netty.WriteQueue",
        "io.grpc.netty.shaded.io.grpc.netty.NettyServerStream",
        "io.grpc.netty.shaded.io.grpc.netty.NettyConnectionHelper",
        "com.caucho.hessian.io.Hessian2Input",
        "com.caucho.hessian.io.ContextSerializerFactory",
        "com.zaxxer.hikari.HikariConfig",
        "com.alibaba.nacos.persistence.datasource.ExternalDataSourceProperties",
        "com.alibaba.nacos.persistence.datasource.LocalDataSourceServiceImpl",
        "com.alibaba.nacos.persistence.configuration.condition.ConditionStandaloneEmbedStorage",
        "com.alibaba.nacos.naming.core.v2.event.metadata.MetadataEvent$ServiceMetadataEvent",
        "com.alibaba.nacos.naming.core.v2.client.ClientSyncDatumSnapshot",
        "com.alibaba.nacos.naming.core.v2.client.ClientSyncData",
        "com.alibaba.nacos.naming.core.v2.metadata.InstanceMetadata",
        "com.alibaba.nacos.naming.core.v2.pojo.HealthCheckInstancePublishInfo",
        "com.alibaba.nacos.naming.core.v2.pojo.BatchInstancePublishInfo",
        "com.alibaba.nacos.naming.core.v2.pojo.BatchInstanceData",
        "com.alibaba.nacos.naming.core.v2.pojo.InstancePublishInfo",
        "com.alibaba.nacos.naming.core.v2.pojo.Service",
        "com.alibaba.nacos.naming.pojo.Subscriber",
        "com.alibaba.nacos.naming.pojo.Subscribers",
        "com.alibaba.nacos.naming.pojo.ClusterInfo",
        "com.alibaba.nacos.naming.pojo.InstanceOperationInfo",
        "com.alibaba.nacos.naming.pojo.IpAddressInfo",
        "com.alibaba.nacos.naming.pojo.Record",
        "com.alibaba.nacos.naming.pojo.ServiceDetailInfo",
        "com.alibaba.nacos.naming.pojo.ServiceNameView",
        "com.alibaba.nacos.config.server.filter.ConfigEnabledFilter",
        "com.alibaba.nacos.config.server.Config",
        "com.alibaba.nacos.config.server.service.LongPollingConnectionMetricsCollector",
        "com.alibaba.nacos.config.server.remote.RpcConfigChangeNotifier",
        "com.alibaba.nacos.naming.config.NamingEnabledFilter",
        "com.alibaba.nacos.naming.NamingApp",
        "com.alibaba.nacos.cmdb.CmdbApp",
        "com.alibaba.nacos.istio.config.IstioEnabledFilter",
        "com.alibaba.nacos.istio.IstioApp",
        "com.alibaba.nacos.prometheus.PrometheusApp",
        "com.alibaba.nacos.sys.filter.NacosTypeExcludeFilter",
        "com.alibaba.nacos.sys.filter.NacosPackageExcludeFilter",
        "com.alibaba.nacos.plugin.control.ControlManagerCenter",
        "com.alibaba.nacos.plugin.control.connection.ConnectionMetricsCollector",
        "com.alibaba.nacos.plugin.auth.impl.jwt.NacosJwtPayload",
        "com.alibaba.nacos.legacy.adapter.naming.CatalogController",
        "com.alibaba.nacos.consistency.snapshot.LocalFileMeta",
        "com.alibaba.nacos.consistency.ProtocolMetaData",
        "com.alibaba.nacos.consistency.ap.APProtocol",
        "com.alibaba.nacos.consistency.cp.CPProtocol",
        "com.alibaba.nacos.consistency.DataOperation",
        "com.alibaba.nacos.consistency.entity.WriteRequest",
        "com.alibaba.nacos.consistency.entity.WriteRequest$Builder",
        "com.alibaba.nacos.consistency.entity.ReadRequest",
        "com.alibaba.nacos.consistency.entity.ReadRequest$Builder",
        "com.alibaba.nacos.consistency.entity.Response",
        "com.alibaba.nacos.consistency.entity.Response$Builder",
        "com.alibaba.nacos.consistency.entity.GetRequest",
        "com.alibaba.nacos.consistency.entity.GetRequest$Builder",
        "com.alibaba.nacos.consistency.entity.Log",
        "com.alibaba.nacos.consistency.entity.Log$Builder",
        "com.alibaba.nacos.consistency.serialize.HessianSerializer",
        "com.alibaba.nacos.consistency.serialize.JacksonSerializer",
        "com.alibaba.nacos.consistency.serialize.NacosHessianSerializerFactory",
        "com.alibaba.nacos.core.distributed.raft.RaftConfig",
        "com.alibaba.nacos.core.distributed.raft.RaftEvent",
        "com.alibaba.nacos.core.distributed.raft.NacosClosure",
        "com.alibaba.nacos.core.cluster.Member",
        "com.alibaba.nacos.core.cluster.remote.request.AbstractClusterRequest",
        "com.alibaba.nacos.core.cluster.remote.request.MemberReportRequest",
        "com.alibaba.nacos.core.cluster.remote.response.MemberReportResponse",
        "com.alibaba.nacos.core.cluster.remote.ClusterRpcClientProxy",
        "com.alibaba.nacos.core.distributed.distro.entity.DistroData",
        "com.alibaba.nacos.core.distributed.distro.entity.DistroKey",
        "com.alibaba.nacos.core.monitor.GrpcServerThreadPoolMonitor",
        "com.alibaba.nacos.core.remote.grpc.BaseGrpcServer",
        "com.alibaba.nacos.core.remote.BaseRpcServer",
        "com.alibaba.nacos.core.remote.grpc.GrpcBiStreamRequestAcceptor",
        "com.alibaba.nacos.core.remote.grpc.GrpcClusterServer",
        "com.alibaba.nacos.core.remote.grpc.GrpcSdkServer",
        "com.alibaba.nacos.core.remote.RuntimeConnectionEjector",
        "com.alibaba.nacos.core.remote.ConnectionManager",
        "com.alibaba.nacos.naming.cluster.remote.request.DistroDataRequest",
        "com.alibaba.nacos.naming.cluster.remote.response.DistroDataResponse",
        "com.alipay.sofa.jraft.entity.LocalFileMetaOutter$LocalFileMeta",
        "com.alipay.sofa.jraft.entity.LocalFileMetaOutter$LocalFileMeta$Builder",
        "com.alipay.sofa.jraft.entity.LocalStorageOutter$LogPBMeta",
        "com.alipay.sofa.jraft.entity.LocalStorageOutter$LogPBMeta$Builder",
        "com.alipay.sofa.jraft.entity.LocalStorageOutter$StablePBMeta",
        "com.alipay.sofa.jraft.entity.LocalStorageOutter$StablePBMeta$Builder",
        "com.alipay.sofa.jraft.entity.LocalStorageOutter$ConfigurationPBMeta",
        "com.alipay.sofa.jraft.entity.LocalStorageOutter$ConfigurationPBMeta$Builder",
        "com.alipay.sofa.jraft.entity.LocalStorageOutter$LocalSnapshotPbMeta",
        "com.alipay.sofa.jraft.entity.LocalStorageOutter$LocalSnapshotPbMeta$Builder",
        "com.alipay.sofa.jraft.entity.LocalStorageOutter$LocalSnapshotPbMeta$File",
        "com.alipay.sofa.jraft.entity.LocalStorageOutter$LocalSnapshotPbMeta$File$Builder",
        "com.alipay.sofa.jraft.entity.RaftOutter$EntryMeta",
        "com.alipay.sofa.jraft.entity.RaftOutter$EntryMeta$Builder",
        "com.alipay.sofa.jraft.entity.RaftOutter$SnapshotMeta",
        "com.alipay.sofa.jraft.entity.RaftOutter$SnapshotMeta$Builder",
        "com.alipay.sofa.jraft.entity.codec.v2.LogOutter$PBLogEntry",
        "com.alipay.sofa.jraft.entity.codec.v2.LogOutter$PBLogEntry$Builder",
        "com.alipay.sofa.jraft.rpc.RpcRequests$AppendEntriesRequest",
        "com.alipay.sofa.jraft.rpc.RpcRequests$AppendEntriesResponse",
        "com.alipay.sofa.jraft.rpc.RpcRequests$PingRequest",
        "com.alipay.sofa.jraft.rpc.RpcRequests$ErrorResponse",
        "com.alipay.sofa.jraft.rpc.RpcRequests$InstallSnapshotRequest",
        "com.alipay.sofa.jraft.rpc.RpcRequests$InstallSnapshotResponse",
        "com.alipay.sofa.jraft.rpc.RpcRequests$TimeoutNowRequest",
        "com.alipay.sofa.jraft.rpc.RpcRequests$TimeoutNowResponse",
        "com.alipay.sofa.jraft.rpc.RpcRequests$RequestVoteRequest",
        "com.alipay.sofa.jraft.rpc.RpcRequests$RequestVoteResponse",
        "com.alipay.sofa.jraft.rpc.RpcRequests$GetFileRequest",
        "com.alipay.sofa.jraft.rpc.RpcRequests$GetFileResponse",
        "com.alipay.sofa.jraft.rpc.RpcRequests$ReadIndexRequest",
        "com.alipay.sofa.jraft.rpc.RpcRequests$ReadIndexResponse",
        "com.alipay.sofa.jraft.rpc.CliRequests$LearnersOpResponse",
        "com.alipay.sofa.jraft.rpc.CliRequests$ResetLearnersRequest",
        "com.alipay.sofa.jraft.rpc.CliRequests$RemoveLearnersRequest",
        "com.alipay.sofa.jraft.rpc.CliRequests$AddLearnersRequest",
        "com.alipay.sofa.jraft.rpc.CliRequests$GetPeersResponse",
        "com.alipay.sofa.jraft.rpc.CliRequests$GetPeersRequest",
        "com.alipay.sofa.jraft.rpc.CliRequests$GetLeaderResponse",
        "com.alipay.sofa.jraft.rpc.CliRequests$GetLeaderRequest",
        "com.alipay.sofa.jraft.rpc.CliRequests$TransferLeaderRequest",
        "com.alipay.sofa.jraft.rpc.CliRequests$ResetPeerRequest",
        "com.alipay.sofa.jraft.rpc.CliRequests$SnapshotRequest",
        "com.alipay.sofa.jraft.rpc.CliRequests$ChangePeersResponse",
        "com.alipay.sofa.jraft.rpc.CliRequests$ChangePeersRequest",
        "com.alipay.sofa.jraft.rpc.CliRequests$RemovePeerResponse",
        "com.alipay.sofa.jraft.rpc.CliRequests$RemovePeerRequest",
        "com.alipay.sofa.jraft.rpc.CliRequests$AddPeerResponse",
        "com.alipay.sofa.jraft.rpc.CliRequests$AddPeerRequest",
        "com.alipay.sofa.jraft.rpc.ProtobufMsgFactory",
        "com.alipay.sofa.jraft.rpc.RpcRequestClosure",
        "com.alipay.sofa.jraft.rpc.impl.AbstractClientService",
        "com.alipay.sofa.jraft.rpc.impl.BoltRaftRpcFactory",
        "com.alipay.sofa.jraft.rpc.impl.GrpcRaftRpcFactory",
        "com.alipay.sofa.jraft.util.JRaftSignalHandler",
        "com.alipay.sofa.jraft.util.concurrent.MpscSingleThreadExecutor",
        "com.alipay.sofa.jraft.util.timer.DefaultRaftTimerFactory",
        "com.alipay.sofa.jraft.util.internal.ThrowUtil",
        "com.alipay.sofa.jraft.core.DefaultJRaftServiceFactory",
        "com.alipay.sofa.jraft.core.NodeImpl",
        "com.alipay.sofa.jraft.core.Replicator",
        "com.alipay.sofa.jraft.storage.snapshot.local.LocalSnapshotReader"};
    
    private final String[] optionalDerbyTypes = {
        "org.apache.derby.impl.store.raw.data.CachedPage",
        "org.apache.derby.catalog.types.TypesImplInstanceGetter",
        "org.apache.derby.impl.services.uuid.BasicUUIDGetter",
        "org.apache.derby.iapi.types.DTSClassInfo",
        "org.apache.derby.iapi.services.loader.ClassInfo",
        "org.apache.derby.impl.io.DirStorageFactory",
        "org.apache.derby.impl.store.raw.log.LogRecord",
        "org.apache.derby.impl.store.raw.xact.XactId",
        "org.apache.derby.impl.store.raw.log.CheckpointOperation",
        "org.apache.derby.impl.store.raw.xact.TransactionTable",
        "org.apache.derby.impl.store.raw.xact.TransactionTableEntry",
        "org.apache.derby.impl.store.raw.log.LogCounter",
        "org.apache.derby.impl.store.raw.log.ChecksumOperation",
        "org.apache.derby.impl.store.raw.data.BaseDataFileFactoryJ4",
        "org.apache.derby.impl.store.raw.xact.XactFactory",
        "org.apache.derby.impl.store.raw.log.ReadOnly",
        "org.apache.derby.impl.store.raw.xact.BeginXact",
        "org.apache.derby.impl.store.raw.xact.EndXact",
        "org.apache.derby.impl.store.raw.data.ContainerOperation",
        "org.apache.derby.impl.store.raw.data.InitPageOperation",
        "org.apache.derby.impl.store.raw.data.AllocPageOperation",
        "org.apache.derby.impl.store.raw.data.InsertOperation",
        "org.apache.derby.impl.store.raw.data.LogicalUndoOperation",
        "org.apache.derby.impl.store.raw.data.InvalidatePageOperation",
        "org.apache.derby.impl.store.raw.data.EncryptContainerOperation",
        "org.apache.derby.impl.store.raw.data.EncryptContainerUndoOperation",
        "org.apache.derby.impl.store.raw.data.CopyRowsOperation",
        "org.apache.derby.impl.store.raw.data.ContainerUndoOperation",
        "org.apache.derby.impl.store.raw.data.CompressSpacePageOperation",
        "org.apache.derby.impl.store.raw.data.CompressSpacePageOperation10_2",
        "org.apache.derby.impl.store.raw.data.ChainAllocPageOperation",
        "org.apache.derby.iapi.jdbc.DRDAServerStarter",
        "org.apache.derby.iapi.jdbc.JDBCBoot",
        "org.apache.derby.iapi.security.SecurityUtil",
        "org.apache.derby.iapi.services.monitor.Monitor",
        "org.apache.derby.impl.services.monitor.FileMonitor",
        "org.apache.derby.impl.services.jmx.JMXManagementService",
        "org.apache.derby.impl.services.cache.ConcurrentCacheFactory",
        "org.apache.derby.impl.services.locks.ConcurrentPool",
        "org.apache.derby.impl.services.jce.JCECipherFactoryBuilder",
        "org.apache.derby.iapi.types.DataValueFactoryImpl",
        "org.apache.derby.impl.store.raw.data.BaseDataFileFactory",
        "org.apache.derby.impl.store.replication.master.MasterController",
        "org.apache.derby.impl.sql.execute.RealResultSetStatisticsFactory",
        "org.apache.derby.impl.jdbc.authentication.NoneAuthenticationServiceImpl",
        "org.apache.derby.iapi.services.property.PropertyValidation",
        "org.apache.derby.impl.sql.conn.GenericLanguageConnectionFactory",
        "org.apache.derby.impl.sql.compile.OptimizerFactoryImpl",
        "org.apache.derby.impl.services.bytecode.BCJava",
        "org.apache.derby.impl.sql.execute.xplain.XPLAINFactory",
        "org.apache.derby.impl.sql.compile.TypeCompilerFactoryImpl",
        "org.apache.derby.impl.sql.catalog.DataDictionaryImpl",
        "org.apache.derby.impl.sql.execute.GenericExecutionFactory",
        "org.apache.derby.impl.services.daemon.SingleThreadDaemonFactory",
        "org.apache.derby.impl.services.timer.SingletonTimerFactory",
        "org.apache.derby.impl.jdbc.authentication.BasicAuthenticationServiceImpl",
        "org.apache.derby.impl.db.BasicDatabase",
        "org.apache.derby.impl.services.stream.SingleStream",
        "org.apache.derby.impl.jdbc.authentication.NativeAuthenticationServiceImpl",
        "org.apache.derby.impl.store.access.sort.ExternalSortFactory",
        "org.apache.derby.impl.jdbc.authentication.SpecificAuthenticationServiceImpl",
        "org.apache.derby.impl.store.access.sort.UniqueWithDuplicateNullsExternalSortFactory",
        "org.apache.derby.impl.services.reflect.ReflectClassesJava2",
        "org.apache.derby.impl.jdbc.authentication.JNDIAuthenticationService",
        "org.apache.derby.impl.store.raw.log.LogToFile",
        "org.apache.derby.impl.store.access.heap.HeapConglomerateFactory",
        "org.apache.derby.impl.db.SlaveDatabase",
        "org.apache.derby.impl.services.jmxnone.NoManagementService",
        "org.apache.derby.impl.store.access.RllRAMAccessManager",
        "org.apache.derby.impl.store.replication.slave.SlaveController",
        "org.apache.derby.impl.sql.GenericLanguageFactory",
        "org.apache.derby.impl.services.uuid.BasicUUIDFactory",
        "org.apache.derby.impl.store.raw.RawStore",
        "org.apache.derby.impl.store.access.btree.index.B2IFactory",
        "org.apache.derby.impl.services.monitor.ModuleInstance",
        "org.apache.derby.impl.services.monitor.ProtocolKey",
        "org.apache.derby.impl.services.monitor.TopService",
        "org.apache.derby.iapi.services.cache.ClassSizeCatalogImpl"};
    
    private final List<Class<? extends Serializable>> directSerializationTypes = List.of(byte.class,
        byte[].class, String.class, ConcurrentHashMap.class,
        com.alibaba.nacos.api.grpc.auto.Metadata.class,
        com.alibaba.nacos.api.grpc.auto.Payload.class,
        com.alibaba.nacos.api.naming.pojo.Cluster.class,
        com.alibaba.nacos.api.naming.pojo.Instance.class,
        com.alibaba.nacos.api.naming.pojo.Service.class,
        com.alibaba.nacos.api.naming.pojo.maintainer.ClusterInfo.class,
        com.alibaba.nacos.api.naming.pojo.maintainer.ServiceDetailInfo.class,
        com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Http.class,
        com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Mysql.class,
        com.alibaba.nacos.api.naming.pojo.healthcheck.impl.Tcp.class,
        com.alibaba.nacos.api.selector.ExpressionSelector.class,
        com.alibaba.nacos.api.selector.NoneSelector.class);
    
    private final String[] optionalSerializationTypes = {
        "com.alibaba.nacos.naming.core.v2.client.ClientSyncData",
        "com.alibaba.nacos.naming.core.v2.metadata.InstanceMetadata",
        "com.alibaba.nacos.naming.core.v2.pojo.HealthCheckInstancePublishInfo",
        "com.alibaba.nacos.naming.core.v2.pojo.BatchInstancePublishInfo",
        "com.alibaba.nacos.naming.core.v2.pojo.BatchInstanceData",
        "com.alibaba.nacos.naming.core.v2.pojo.InstancePublishInfo",
        "com.alibaba.nacos.naming.core.v2.pojo.Service",
        "com.alibaba.nacos.naming.pojo.Subscriber",
        "com.alibaba.nacos.naming.pojo.Subscribers",
        "com.alibaba.nacos.naming.pojo.ClusterInfo",
        "com.alibaba.nacos.naming.pojo.IpAddressInfo",
        "com.alibaba.nacos.naming.pojo.Record",
        "com.alibaba.nacos.naming.pojo.ServiceDetailInfo",
        "com.alibaba.nacos.consistency.entity.WriteRequest",
        "com.alibaba.nacos.consistency.entity.ReadRequest",
        "com.alibaba.nacos.consistency.entity.Response",
        "com.alibaba.nacos.consistency.entity.GetRequest",
        "com.alibaba.nacos.consistency.entity.Log"};
    
    private final String[] resourcePattern = {AotConfiguration.reflectToNativeLibraryLoader(),
        ".*libnetty_transport_native_epoll_.*\\.so", ".*\\.desc$", ".*\\.html$",
        ".*\\.css$", ".*\\.js$", ".*\\.js.map$", ".*\\.png$", ".*\\.svg$",
        ".*\\.eot$", ".*\\.woff$", ".*\\.woff2$", ".*\\.ttf$",
        "org/apache/derby/modules.properties", "application.properties"};
    
    public NacosRuntimeHints() {
    }
    
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        Stream.of(directReflectionTypes).forEach(type -> registerReflectionType(hints, type));
        registerNestedTypes(hints, com.google.protobuf.DescriptorProtos.class);
        Stream.of(optionalReflectionTypes, optionalDerbyTypes).flatMap(Stream::of)
            .forEach(className -> registerOptionalClass(hints, classLoader, className));
        
        for (String pattern : resourcePattern) {
            if (!pattern.isBlank()) {
                hints.resources().registerPattern(pattern);
            }
        }
        
        directSerializationTypes.forEach(type -> hints.serialization().registerType(type));
        Stream.of(optionalSerializationTypes)
            .forEach(className -> registerOptionalSerializationType(hints, classLoader, className));
    }
    
    private void registerNestedTypes(RuntimeHints hints, Class<?> type) {
        registerReflectionType(hints, type);
        for (Class<?> nestedType : type.getDeclaredClasses()) {
            registerNestedTypes(hints, nestedType);
        }
    }
    
    private void registerOptionalClass(RuntimeHints hints, ClassLoader classLoader,
        String className) {
        resolveClass(classLoader, className).ifPresent(clazz -> {
            try {
                registerReflectionType(hints, clazz);
            } catch (LinkageError e) {
                // Optional dependency is only partially present in this application.
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    private void registerOptionalSerializationType(RuntimeHints hints, ClassLoader classLoader,
        String className) {
        resolveClass(classLoader, className).filter(Serializable.class::isAssignableFrom)
            .map(clazz -> (Class<? extends Serializable>) clazz)
            .ifPresent(type -> {
                try {
                    hints.serialization().registerType(type);
                } catch (LinkageError e) {
                    // Optional dependency is only partially present in this application.
                }
            });
    }
    
    private void registerReflectionType(RuntimeHints hints, Class<?> type) {
        hints.reflection().registerType(type, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INTROSPECT_PUBLIC_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.DECLARED_FIELDS, MemberCategory.DECLARED_CLASSES,
            MemberCategory.PUBLIC_CLASSES);
    }
    
    private java.util.Optional<Class<?>> resolveClass(ClassLoader classLoader, String className) {
        ClassLoader targetClassLoader = classLoader != null ? classLoader
            : Thread.currentThread().getContextClassLoader();
        try {
            return java.util.Optional.of(Class.forName(className, false, targetClassLoader));
        } catch (ClassNotFoundException | LinkageError e) {
            return java.util.Optional.empty();
        }
    }
}
