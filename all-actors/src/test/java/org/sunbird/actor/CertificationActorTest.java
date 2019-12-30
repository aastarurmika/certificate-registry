package org.sunbird.actor;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import org.apache.commons.math3.analysis.function.Power;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.ActorOperations;
import org.sunbird.BaseActor;
import org.sunbird.JsonKeys;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.cassandraimpl.CassandraDACImpl;
import org.sunbird.common.ElasticSearchRestHighImpl;
import org.sunbird.common.factory.EsClientFactory;
import org.sunbird.common.inf.ElasticSearchService;
import org.sunbird.helper.ServiceFactory;
import org.sunbird.message.Localizer;
import org.sunbird.message.ResponseCode;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import org.sunbird.service.ICertService;
import org.sunbird.serviceimpl.CertsServiceImpl;
import org.sunbird.utilities.CertificateUtil;
import scala.concurrent.duration.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        Localizer.class,
        ICertService.class,
        CertsServiceImpl.class,
        CertificateUtil.class,
        EsClientFactory.class,
        ServiceFactory.class,
        ElasticSearchRestHighImpl.class,
        ElasticSearchService.class,
        CassandraOperation.class,
        CassandraDACImpl.class})
@PowerMockIgnore("javax.management.*")
public class CertificationActorTest {

    private static ActorSystem system = ActorSystem.create("system");
    private static final Props props = Props.create(CertificationActor.class);

    @BeforeClass
    public static void setUp() throws Exception {
    }

    //@Before
    public void beforeTestSetUp() throws Exception {
        PowerMockito.mockStatic(Localizer.class);
        when(Localizer.getInstance()).thenReturn(null);

        PowerMockito.mockStatic(EsClientFactory.class);
        ElasticSearchService elasticSearchService = PowerMockito.mock(ElasticSearchService.class);
        ElasticSearchRestHighImpl elasticSearchRestHigh = PowerMockito.mock(ElasticSearchRestHighImpl.class);
        PowerMockito.whenNew(ElasticSearchRestHighImpl.class).withNoArguments().thenReturn(elasticSearchRestHigh);
        when(EsClientFactory.getInstance()).thenReturn(elasticSearchRestHigh);

        PowerMockito.mockStatic(ServiceFactory.class);
        PowerMockito.mock(CassandraOperation.class);
        CassandraDACImpl cassandraDAC = PowerMockito.mock(CassandraDACImpl.class);
        PowerMockito.whenNew(CassandraDACImpl.class).withNoArguments().thenReturn(cassandraDAC);

        PowerMockito.mockStatic(CertificateUtil.class);
        when(CertificateUtil.isIdPresent(Mockito.anyString())).thenReturn(false);

        ICertService iCertService = PowerMockito.mock(ICertService.class);
        CertsServiceImpl certsService = PowerMockito.mock(CertsServiceImpl.class);
        PowerMockito.whenNew(CertsServiceImpl.class).withNoArguments().thenReturn(certsService);
        when(certsService.add(Mockito.any(Request.class))).thenReturn("id");

        when(certsService.validate(Mockito.any(Request.class))).thenReturn(getValidateCertResponse());
        Map<String,Object> map = new HashMap<>();
        map.put(JsonKeys.ACCESS_CODE,"access_code");
        when(CertificateUtil.getCertificate(Mockito.anyString())).thenReturn(map);

        when(certsService.download(Mockito.any(Request.class))).thenReturn(getValidateCertResponse());
        when(CertificateUtil.getCertificate(Mockito.anyString())).thenReturn(map);

        when(certsService.generate(Mockito.any(Request.class))).thenReturn(getValidateCertResponse());
        when(CertificateUtil.getCertificate(Mockito.anyString())).thenReturn(map);

        when(certsService.verify(Mockito.any(Request.class))).thenReturn(getValidateCertResponse());
        when(CertificateUtil.getCertificate(Mockito.anyString())).thenReturn(map);
    }


    //@AfterClass
    public static void tearDown() throws Exception {
        Duration duration = Duration.create(10L, TimeUnit.SECONDS);
        TestKit.shutdownActorSystem(system, duration, true);
        system = null;
    }

    @Test
    public void addCertificate() throws Exception {
        Request request = createAddCertRequest();
        request.setOperation(ActorOperations.ADD.getOperation());
        beforeTestSetUp();
        TestKit testKit = new TestKit(system);
        ActorRef actorRef = system.actorOf(props);
        actorRef.tell(request, testKit.getRef());
        Response res = testKit.expectMsgClass(Duration.create(10, TimeUnit.SECONDS),Response.class);
        Assert.assertTrue(null != res && res.getResponseCode() == ResponseCode.OK);
    }

    @Test
    public void validateCertificate() throws Exception {

        Request request = createValidateCertRequest();
        request.setOperation(ActorOperations.VALIDATE.getOperation());
        beforeTestSetUp();
        TestKit testKit = new TestKit(system);
        ActorRef actorRef = system.actorOf(props);
        actorRef.tell(request, testKit.getRef());
        Response res = testKit.expectMsgClass(Duration.create(10, TimeUnit.SECONDS),Response.class);
        Assert.assertTrue(null != res && res.getResponseCode() == ResponseCode.OK);
    }

    //@Test
    public void downloadCertificate() throws Exception {

        Request request = createDownloadCertRequest();
        request.setOperation(ActorOperations.DOWNLOAD.getOperation());
        beforeTestSetUp();
        TestKit testKit = new TestKit(system);
        ActorRef actorRef = system.actorOf(props);
        actorRef.tell(request, testKit.getRef());
        Response res = testKit.expectMsgClass(Duration.create(1000, TimeUnit.SECONDS),Response.class);
        Assert.assertTrue(null != res && res.getResponseCode() == ResponseCode.OK);
    }
    //@Test
    public void generateCertificate() throws Exception {

        Request request = createAddCertRequest();
        request.setOperation(ActorOperations.GENERATE.getOperation());
        beforeTestSetUp();
        TestKit testKit = new TestKit(system);
        ActorRef actorRef = system.actorOf(props);
        actorRef.tell(request, testKit.getRef());
        Response res = testKit.expectMsgClass(Duration.create(10, TimeUnit.SECONDS),Response.class);
        Assert.assertTrue(null != res && res.getResponseCode() == ResponseCode.OK);
    }
   // @Test
    public void verifyCertificate() throws Exception {

        Request request = createAddCertRequest();
        request.setOperation(ActorOperations.VERIFY.getOperation());
        beforeTestSetUp();
        TestKit testKit = new TestKit(system);
        ActorRef actorRef = system.actorOf(props);
        actorRef.tell(request, testKit.getRef());
        Response res = testKit.expectMsgClass(Duration.create(10, TimeUnit.SECONDS),Response.class);
        Assert.assertTrue(null != res && res.getResponseCode() == ResponseCode.OK);
    }

    private Response getValidateCertResponse(){
        Map<String,Object>responseMap=new HashMap<>();
        responseMap.put(JsonKeys.JSON,new HashMap<>());
        responseMap.put(JsonKeys.PDF,"pdf url");
        responseMap.put(JsonKeys.RELATED,new HashMap<>());
        Response response=new Response();
        response.put(JsonKeys.RESPONSE,responseMap);
        return response;
    }
    private Request createDownloadCertRequest() {
        Request reqObj = new Request();
        Map<String,Object> reqMap = new HashMap<>();
        reqMap.put(JsonKeys.PDF_URL,"pdf_url");
        reqObj.getRequest().putAll(reqMap);
        return reqObj;
    }
    private Request createValidateCertRequest() {
        Request reqObj = new Request();
        Map<String,Object> reqMap = new HashMap<>();
        reqMap.put(JsonKeys.CERT_ID,"certId");
        reqMap.put(JsonKeys.ACCESS_CODE,"access_code");
        reqObj.getRequest().putAll(reqMap);
        return reqObj;
    }
    private Request createAddCertRequest() {
        Request reqObj = new Request();
        Map<String,Object> reqMap = new HashMap<>();
        Map<String,Object> innerMap = new HashMap<>();
        innerMap.put("recipientId", "b69b056d-1d7d-47a9-a3b6-584fce840fd3");
        innerMap.put("recipientName", "Akash Kumar");
        innerMap.put("id", "id5dd4");
        innerMap.put("accessCode", "EANAB13");
        Map<String,Object> jsonData = new HashMap<>();
        jsonData.put("id", "http://localhost:8080/_schemas/Certificate/d5a28280-98ac-4294-a508-21075dc7d475");
        jsonData.put("type",new String[]{"Assertion","Extension"});
        jsonData.put("issuedOn", "2019-08-31T12:52:25Z");
        Map<String,Object> recipient = new HashMap<>();
        recipient.put("identity", "ntptest103");
        recipient.put("name", "Aishwarya");
        jsonData.put("recipient", recipient);
        Map<String,Object> badge = new HashMap<>();
        badge.put("id", "http://localhost:8080/_schemas/Badge.json");
        badge.put("name", "Sunbird installation");
        badge.put("description", "Certificate of Appreciation in National Level ITI Grading");
        jsonData.put("badge",badge);
        innerMap.put("pdfUrl", "djdjd/r01284093466818969624/275acee4-1964-4986-9dc3-06e08e9a1aa0.pdf");
        Map<String,Object> related = new HashMap<>();
        related.put("courseId", "course-idsd");
        related.put("type", "hell");
        related.put("batchId", "batchId");
        related.put("introUrl", "intro url");
        related.put("completionUrl", "completionUrl");
        innerMap.put("related", related);
        innerMap.put("jsonData", jsonData);
        reqMap.put("request",innerMap);
        reqObj.getRequest().putAll(reqMap);
        return reqObj;
    }

}