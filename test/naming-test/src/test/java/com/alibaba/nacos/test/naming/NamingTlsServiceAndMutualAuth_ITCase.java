/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.test.naming;

import com.alibaba.nacos.Nacos;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.remote.client.RpcConstants;
import com.alibaba.nacos.core.remote.RpcServerTlsConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.alibaba.nacos.test.naming.NamingBase.randomDomainName;

/**
 * @author githucheng2978.
 * @date .
 **/
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest(classes = Nacos.class, properties = {
        "server.servlet.context-path=/nacos",
        RpcServerTlsConfig.PREFIX+".enableTls=true",
        RpcServerTlsConfig.PREFIX+".mutualAuthEnable=true",
        RpcServerTlsConfig.PREFIX+".compatibility=false",
        RpcServerTlsConfig.PREFIX+".certChainFile=test-server-cert.pem",
        RpcServerTlsConfig.PREFIX+".certPrivateKey=test-server-key.pem",
        RpcServerTlsConfig.PREFIX+".trustCollectionCertFile=test-ca-cert.pem",

},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class NamingTlsServiceAndMutualAuth_ITCase {


    @LocalServerPort
    private int port;

    @Test
    public void test_a_MutualAuth() throws NacosException {
        String serviceName = randomDomainName();
        System.setProperty(RpcConstants.RPC_CLIENT_TLS_ENABLE,"true");
        System.setProperty(RpcConstants.RPC_CLIENT_TLS_TRUST_COLLECTION_CHAIN_PATH,"test-ca-cert.pem");
        System.setProperty(RpcConstants.RPC_CLIENT_TLS_CERT_CHAIN_PATH,"test-client-cert.pem");
        System.setProperty(RpcConstants.RPC_CLIENT_TLS_CERT_KEY,"test-client-key.pem");
        System.setProperty(RpcConstants.RPC_CLIENT_MUTUAL_AUTH,"true");
        Instance   instance = new Instance();
        instance.setIp("127.0.0.1");
        instance.setPort(8081);
        instance.setWeight(2);
        instance.setClusterName(Constants.DEFAULT_CLUSTER_NAME);
        NamingService namingService = NamingFactory.createNamingService("127.0.0.1" + ":" + port);
        Map<String, String> map = new HashMap<String, String>();
        map.put("netType", "external-update");
        map.put("version", "2.0");
        instance.setMetadata(map);
        namingService.registerInstance(serviceName, instance);
        try {
            TimeUnit.SECONDS.sleep(3L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        List<Instance> instances = namingService.getAllInstances(serviceName, false);
        Assert.assertEquals(instances.size(), 1);
        Assert.assertEquals("2.0", instances.get(0).getMetadata().get("version"));
        namingService.shutDown();

    }


    @Test(expected = NacosException.class)
    public void test_b_MutualAuthClientTrustCa() throws NacosException {
        String serviceName = randomDomainName();
        System.setProperty(RpcConstants.RPC_CLIENT_TLS_ENABLE,"true");
        System.setProperty(RpcConstants.RPC_CLIENT_MUTUAL_AUTH,"true");
        System.setProperty(RpcConstants.RPC_CLIENT_TLS_CERT_CHAIN_PATH,"");
        System.setProperty(RpcConstants.RPC_CLIENT_TLS_CERT_KEY,"");
        System.setProperty(RpcConstants.RPC_CLIENT_TLS_TRUST_COLLECTION_CHAIN_PATH,"test-ca-cert.pem");
        Instance   instance = new Instance();
        instance.setIp("127.0.0.1");
        instance.setPort(8081);
        instance.setWeight(2);
        instance.setClusterName(Constants.DEFAULT_CLUSTER_NAME);
        NamingService namingService = NamingFactory.createNamingService("127.0.0.1" + ":" + port);
        Map<String, String> map = new HashMap<String, String>();
        map.put("netType", "external-update");
        map.put("version", "2.0");
        instance.setMetadata(map);
        namingService.registerInstance(serviceName, instance);
        namingService.shutDown();

    }

    @Test(expected = NacosException.class)
    public void test_c_MutualAuthClientTrustALl() throws NacosException {
        String serviceName = randomDomainName();
        System.setProperty(RpcConstants.RPC_CLIENT_TLS_ENABLE,"true");
        System.setProperty(RpcConstants.RPC_CLIENT_MUTUAL_AUTH,"true");
        System.setProperty(RpcConstants.RPC_CLIENT_TLS_CERT_CHAIN_PATH,"");
        System.setProperty(RpcConstants.RPC_CLIENT_TLS_CERT_KEY,"");
        System.setProperty(RpcConstants.RPC_CLIENT_TLS_TRUST_ALL,"true");
        Instance   instance = new Instance();
        instance.setIp("127.0.0.1");
        instance.setPort(8081);
        instance.setWeight(2);
        instance.setClusterName(Constants.DEFAULT_CLUSTER_NAME);
        NamingService namingService = NamingFactory.createNamingService("127.0.0.1" + ":" + port);
        Map<String, String> map = new HashMap<String, String>();
        map.put("netType", "external-update");
        map.put("version", "2.0");
        instance.setMetadata(map);
        namingService.registerInstance(serviceName, instance);
        namingService.shutDown();
    }

    @After
    public void after(){
        System.setProperty(RpcConstants.RPC_CLIENT_TLS_ENABLE,"");
    }
}
