package io.incognito.rest.client;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.SpringServletContainerInitializer;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

import io.incognito.rest.client.helper.ResponseTypeConverter;
import io.incognito.rest.client.types.dto.response.BaseApiResponse;
import io.incognito.rest.client.types.dto.response.EmptyOrStringBodyResponse;
import io.incognito.rest.client.types.dto.response.StringObjectMapResponse;
import lombok.Data;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpClientsTest{
    private final WebClient webClient = WebClient.create("http://101.101.217.170:8080/Boxwood_portal_demo");

    public WebClient getWebClient() {
        return webClient.mutate().build();
    }

    @Test
    public void testEmptyBodyAndEmptyResponse() {
        final Object body = Map.of(
                "process_name",  "",
                "deployed_status", "",
                "workspace_id", "WORKSPACE2020120314115178ef35cb68374e549cbb28a794z",
                "curPage", 1,
                "scale", 12,
                "length", 10,
                "workspace_name", "",
                "view_type", "table"
        );


        final BaseDataResponse resp = HttpClients.post(getWebClient())
                .url("/process/getProcessList")
                .build()
                .executeWithBodyAsync(body, BaseDataResponse.class)
//                .map(response -> {
//                    return ResponseTypeConverter.convertEmptyOrStringBodyResponseType(response, BaseDataResponse.class, new ObjectMapper());
//                })
                .block();

        assertTrue(resp != null && resp.isSuccess());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class BaseDataResponse extends BaseApiResponse {
        private DataPart data;
        private String resultMsg;
        private String message;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class DataPart {
        private int total;
        private List<ProcessInfo> list;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class ProcessInfo {
        @JsonProperty("creator_cd")
        private String creatorCd;
        @JsonProperty("deployed_yn")
        private String deployedYn;
        @JsonProperty("deployed_status")
        private String deployedStatus;
        @JsonProperty("event_category1")
        private String eventCategory1;
        @JsonProperty("description")
        private String description;
        @JsonProperty("created_at")
        private String createdAt;
        @JsonProperty("minor_version")
        private Integer minorVersion;
//        @JsonProperty("modeler_data") : "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<bpmn2:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" xmlns:boxwood=\"http://ecoletree.com/schema/bpmn/boxwood\" id=\"sample-diagram\" targetNamespace=\"http://bpmn.io/schema/bpmn\" xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\">\n  <bpmn2:process id=\"PROCESS202504021047428a4e8c7de9b04c698c7d3b6a2b323\" name=\"Demo 0402\" isExecutable=\"true\">\n    <bpmn2:extensionElements>\n      <camunda:properties>\n        <camunda:property name=\"process_cd\" value=\"PROCESS202504021047428a4e8c7de9b04c698c7d3b6a2b323\" />\n      </camunda:properties>\n    </bpmn2:extensionElements>\n    <bpmn2:startEvent id=\"StartEvent_1\">\n      <bpmn2:extensionElements>\n        <camunda:executionListener class=\"kr.co.ecoletree.boxwood.bpmn.execution.listener.start.StartEventListener\" event=\"start\" />\n        <camunda:executionListener class=\"kr.co.ecoletree.boxwood.bpmn.execution.listener.end.StartEventListener\" event=\"end\" />\n      </bpmn2:extensionElements>\n      <bpmn2:outgoing>Flow_1vtimti</bpmn2:outgoing>\n    </bpmn2:startEvent>\n    <bpmn2:serviceTask id=\"Activity_1atj6ni\" name=\"Send Slack message (Demo 0420)\" camunda:asyncBefore=\"true\" implementation=\"##WebService\" camunda:type=\"external\" camunda:topic=\"chatbot\" boxwood:taskType=\"SERVICE20250402104616a718262acf124a6eb05e714df8923\">\n      <bpmn2:extensionElements>\n        <camunda:properties>\n          <camunda:property name=\"service_cd\" value=\"SERVICE20250402104616a718262acf124a6eb05e714df8923\" />\n          <camunda:property name=\"service_name\" value=\"Send Slack message (Demo 0420)\" />\n          <camunda:property name=\"service_ext_cd\" value=\"SERVICEEXT20250402111750e6ce1216b27a42cd8205710996\" />\n          <camunda:property name=\"connector_cd\" value=\"CONNECTOR202504021044003b74ec3001524115b03dfed1eae\" />\n          <camunda:property name=\"connector_name\" value=\"BOXWOOD Demo 0402\" />\n          <camunda:property name=\"version_num\" value=\"2\" />\n          <camunda:property name=\"version_tag\" value=\"version_tag\" />\n          <camunda:property name=\"sync_type_cd\" value=\"SY002\" />\n          <camunda:property name=\"sync_type\" value=\"ASYNC\" />\n          <camunda:property name=\"error_handle_use_timeout\" value=\"Y\" />\n          <camunda:property name=\"error_handle_timeout\" value=\"1\" />\n          <camunda:property name=\"error_handle_use_action\" value=\"Y\" />\n          <camunda:property name=\"error_handle_on_error_action\" value=\"EA001\" />\n          <camunda:property name=\"error_action\" value=\"Terminate Process\" />\n          <camunda:property name=\"inputs\" value=\"[{&#34;name&#34;:&#34;message&#34;,&#34;alias&#34;:&#34;Text1&#34;,&#34;type&#34;:&#34;text&#34;,&#34;value&#34;:&#34;&#34;},{&#34;name&#34;:&#34;channel&#34;,&#34;alias&#34;:&#34;Text1&#34;,&#34;type&#34;:&#34;text&#34;,&#34;value&#34;:&#34;#bot-test&#34;}]\" />\n          <camunda:property name=\"outputs\" value=\"[{&#34;name&#34;:&#34;ok&#34;,&#34;alias&#34;:&#34;Text1&#34;,&#34;type&#34;:&#34;text&#34;,&#34;value&#34;:&#34;&#34;},{&#34;name&#34;:&#34;next_msg&#34;,&#34;alias&#34;:&#34;Text1&#34;,&#34;type&#34;:&#34;text&#34;,&#34;value&#34;:&#34;&#34;}]\" />\n          <camunda:property name=\"url\" value=\"http://101.101.217.170:3000/bot/slack/msg\" />\n          <camunda:property name=\"method\" value=\"POST\" />\n          <camunda:property name=\"content_type\" value=\"application/json\" />\n          <camunda:property name=\"header\" value=\"{}\" />\n          <camunda:property name=\"query\" value=\"{}\" />\n          <camunda:property name=\"auth_type\" value=\"\" />\n          <camunda:property name=\"api_key\" value=\"\" />\n          <camunda:property name=\"api_value\" value=\"\" />\n          <camunda:property name=\"category1_name\" value=\"Chatbot\" />\n          <camunda:property name=\"category_2\" value=\"CT012\" />\n          <camunda:property name=\"category2_name\" value=\"Slack\" />\n        </camunda:properties>\n        <camunda:inputOutput>\n          <camunda:inputParameter name=\"message\" alias=\"Text1\">#(StartEvent_1.event_msg)</camunda:inputParameter>\n          <camunda:inputParameter name=\"channel\" alias=\"Text1\">#bot-test</camunda:inputParameter>\n          <camunda:outputParameter name=\"ok\" alias=\"Text1\" />\n          <camunda:outputParameter name=\"next_msg\" alias=\"Text1\" />\n        </camunda:inputOutput>\n        <camunda:executionListener class=\"kr.co.ecoletree.boxwood.bpmn.execution.listener.start.ChatbotListener\" event=\"start\" />\n        <camunda:executionListener class=\"kr.co.ecoletree.boxwood.bpmn.execution.listener.end.ChatbotListener\" event=\"end\" />\n      </bpmn2:extensionElements>\n      <bpmn2:incoming>Flow_1vtimti</bpmn2:incoming>\n      <bpmn2:outgoing>Flow_0wg24k7</bpmn2:outgoing>\n    </bpmn2:serviceTask>\n    <bpmn2:sequenceFlow id=\"Flow_1vtimti\" sourceRef=\"StartEvent_1\" targetRef=\"Activity_1atj6ni\" />\n    <bpmn2:sequenceFlow id=\"Flow_0wg24k7\" sourceRef=\"Activity_1atj6ni\" targetRef=\"Gateway_1cp3ssv\" />\n    <bpmn2:exclusiveGateway id=\"Gateway_1cp3ssv\" default=\"Flow_0rf6jnx\">\n      <bpmn2:incoming>Flow_0wg24k7</bpmn2:incoming>\n      <bpmn2:outgoing>Flow_1qlemkr</bpmn2:outgoing>\n      <bpmn2:outgoing>Flow_0rf6jnx</bpmn2:outgoing>\n    </bpmn2:exclusiveGateway>\n    <bpmn2:serviceTask id=\"Activity_15b3lpi\" name=\"Send Slack message (Demo 0420)\" camunda:asyncBefore=\"true\" implementation=\"##WebService\" camunda:type=\"external\" camunda:topic=\"chatbot\" boxwood:taskType=\"SERVICE20250402104616a718262acf124a6eb05e714df8923\">\n      <bpmn2:extensionElements>\n        <camunda:properties>\n          <camunda:property name=\"service_cd\" value=\"SERVICE20250402104616a718262acf124a6eb05e714df8923\" />\n          <camunda:property name=\"service_name\" value=\"Send Slack message (Demo 0420)\" />\n          <camunda:property name=\"service_ext_cd\" value=\"SERVICEEXT20250402104616bbae577e3860454dbb311996ac\" />\n          <camunda:property name=\"connector_cd\" value=\"CONNECTOR202504021044003b74ec3001524115b03dfed1eae\" />\n          <camunda:property name=\"connector_name\" value=\"BOXWOOD Demo 0402\" />\n          <camunda:property name=\"version_num\" value=\"1\" />\n          <camunda:property name=\"version_tag\" value=\"version_tag\" />\n          <camunda:property name=\"sync_type_cd\" value=\"SY001\" />\n          <camunda:property name=\"sync_type\" value=\"SYNC\" />\n          <camunda:property name=\"error_handle_use_timeout\" value=\"Y\" />\n          <camunda:property name=\"error_handle_timeout\" value=\"1\" />\n          <camunda:property name=\"error_handle_use_action\" value=\"Y\" />\n          <camunda:property name=\"error_handle_on_error_action\" value=\"EA001\" />\n          <camunda:property name=\"error_action\" value=\"Terminate Process\" />\n          <camunda:property name=\"inputs\" value=\"[{&#34;name&#34;:&#34;message&#34;,&#34;alias&#34;:&#34;Text1&#34;,&#34;type&#34;:&#34;text&#34;,&#34;value&#34;:&#34;&#34;},{&#34;name&#34;:&#34;channel&#34;,&#34;alias&#34;:&#34;Text1&#34;,&#34;type&#34;:&#34;text&#34;,&#34;value&#34;:&#34;#bot-test&#34;}]\" />\n          <camunda:property name=\"outputs\" value=\"[{&#34;name&#34;:&#34;ok&#34;,&#34;alias&#34;:&#34;Text1&#34;,&#34;type&#34;:&#34;text&#34;,&#34;value&#34;:&#34;&#34;}]\" />\n          <camunda:property name=\"url\" value=\"http://101.101.217.170:3000/bot/slack/msg\" />\n          <camunda:property name=\"method\" value=\"POST\" />\n          <camunda:property name=\"content_type\" value=\"application/json\" />\n          <camunda:property name=\"header\" value=\"{}\" />\n          <camunda:property name=\"query\" value=\"{}\" />\n          <camunda:property name=\"auth_type\" value=\"\" />\n          <camunda:property name=\"api_key\" value=\"\" />\n          <camunda:property name=\"api_value\" value=\"\" />\n          <camunda:property name=\"category1_name\" value=\"Chatbot\" />\n          <camunda:property name=\"category_2\" value=\"CT012\" />\n          <camunda:property name=\"category2_name\" value=\"Slack\" />\n        </camunda:properties>\n        <camunda:inputOutput>\n          <camunda:inputParameter name=\"message\" alias=\"Text1\">#(Activity_1atj6ni.ok)</camunda:inputParameter>\n          <camunda:inputParameter name=\"channel\" alias=\"Text1\" boxwood:useService=\"N\">#boxwood</camunda:inputParameter>\n          <camunda:outputParameter name=\"ok\" alias=\"Text1\" />\n        </camunda:inputOutput>\n        <camunda:executionListener class=\"kr.co.ecoletree.boxwood.bpmn.execution.listener.start.ChatbotListener\" event=\"start\" />\n        <camunda:executionListener class=\"kr.co.ecoletree.boxwood.bpmn.execution.listener.end.ChatbotListener\" event=\"end\" />\n      </bpmn2:extensionElements>\n      <bpmn2:incoming>Flow_1qlemkr</bpmn2:incoming>\n      <bpmn2:outgoing>Flow_0vg1k9j</bpmn2:outgoing>\n    </bpmn2:serviceTask>\n    <bpmn2:sequenceFlow id=\"Flow_1qlemkr\" sourceRef=\"Gateway_1cp3ssv\" targetRef=\"Activity_15b3lpi\">\n      <bpmn2:conditionExpression xsi:type=\"bpmn2:tFormalExpression\" language=\"javascript\">execution.getVariable(\"Activity_1atj6ni.ok\")</bpmn2:conditionExpression>\n    </bpmn2:sequenceFlow>\n    <bpmn2:serviceTask id=\"Activity_1mc3ewp\" name=\"Send Slack message (Demo 0420)\" camunda:asyncBefore=\"true\" implementation=\"##WebService\" camunda:type=\"external\" camunda:topic=\"chatbot\" boxwood:taskType=\"SERVICE20250402104616a718262acf124a6eb05e714df8923\">\n      <bpmn2:extensionElements>\n        <camunda:properties>\n          <camunda:property name=\"service_cd\" value=\"SERVICE20250402104616a718262acf124a6eb05e714df8923\" />\n          <camunda:property name=\"service_name\" value=\"Send Slack message (Demo 0420)\" />\n          <camunda:property name=\"service_ext_cd\" value=\"SERVICEEXT20250402104616bbae577e3860454dbb311996ac\" />\n          <camunda:property name=\"connector_cd\" value=\"CONNECTOR202504021044003b74ec3001524115b03dfed1eae\" />\n          <camunda:property name=\"connector_name\" value=\"BOXWOOD Demo 0402\" />\n          <camunda:property name=\"version_num\" value=\"1\" />\n          <camunda:property name=\"version_tag\" value=\"version_tag\" />\n          <camunda:property name=\"sync_type_cd\" value=\"SY001\" />\n          <camunda:property name=\"sync_type\" value=\"SYNC\" />\n          <camunda:property name=\"error_handle_use_timeout\" value=\"Y\" />\n          <camunda:property name=\"error_handle_timeout\" value=\"1\" />\n          <camunda:property name=\"error_handle_use_action\" value=\"Y\" />\n          <camunda:property name=\"error_handle_on_error_action\" value=\"EA001\" />\n          <camunda:property name=\"error_action\" value=\"Terminate Process\" />\n          <camunda:property name=\"inputs\" value=\"[{&#34;name&#34;:&#34;message&#34;,&#34;alias&#34;:&#34;Text1&#34;,&#34;type&#34;:&#34;text&#34;,&#34;value&#34;:&#34;&#34;},{&#34;name&#34;:&#34;channel&#34;,&#34;alias&#34;:&#34;Text1&#34;,&#34;type&#34;:&#34;text&#34;,&#34;value&#34;:&#34;#bot-test&#34;}]\" />\n          <camunda:property name=\"outputs\" value=\"[{&#34;name&#34;:&#34;ok&#34;,&#34;alias&#34;:&#34;Text1&#34;,&#34;type&#34;:&#34;text&#34;,&#34;value&#34;:&#34;&#34;}]\" />\n          <camunda:property name=\"url\" value=\"http://101.101.217.170:3000/bot/slack/msg\" />\n          <camunda:property name=\"method\" value=\"POST\" />\n          <camunda:property name=\"content_type\" value=\"application/json\" />\n          <camunda:property name=\"header\" value=\"{}\" />\n          <camunda:property name=\"query\" value=\"{}\" />\n          <camunda:property name=\"auth_type\" value=\"\" />\n          <camunda:property name=\"api_key\" value=\"\" />\n          <camunda:property name=\"api_value\" value=\"\" />\n          <camunda:property name=\"category1_name\" value=\"Chatbot\" />\n          <camunda:property name=\"category_2\" value=\"CT012\" />\n          <camunda:property name=\"category2_name\" value=\"Slack\" />\n        </camunda:properties>\n        <camunda:inputOutput>\n          <camunda:inputParameter name=\"message\" alias=\"Text1\" boxwood:useService=\"N\">Failed to send message</camunda:inputParameter>\n          <camunda:inputParameter name=\"channel\" alias=\"Text1\">#bot-test</camunda:inputParameter>\n          <camunda:outputParameter name=\"ok\" alias=\"Text1\" />\n        </camunda:inputOutput>\n        <camunda:executionListener class=\"kr.co.ecoletree.boxwood.bpmn.execution.listener.start.ChatbotListener\" event=\"start\" />\n        <camunda:executionListener class=\"kr.co.ecoletree.boxwood.bpmn.execution.listener.end.ChatbotListener\" event=\"end\" />\n      </bpmn2:extensionElements>\n      <bpmn2:incoming>Flow_0rf6jnx</bpmn2:incoming>\n      <bpmn2:outgoing>Flow_1njox26</bpmn2:outgoing>\n    </bpmn2:serviceTask>\n    <bpmn2:sequenceFlow id=\"Flow_0rf6jnx\" sourceRef=\"Gateway_1cp3ssv\" targetRef=\"Activity_1mc3ewp\" />\n    <bpmn2:endEvent id=\"Event_10w321z\">\n      <bpmn2:extensionElements>\n        <camunda:executionListener class=\"kr.co.ecoletree.boxwood.bpmn.execution.listener.start.EndEventListener\" event=\"start\" />\n        <camunda:executionListener class=\"kr.co.ecoletree.boxwood.bpmn.execution.listener.end.EndEventListener\" event=\"end\" />\n      </bpmn2:extensionElements>\n      <bpmn2:incoming>Flow_0vg1k9j</bpmn2:incoming>\n      <bpmn2:incoming>Flow_1njox26</bpmn2:incoming>\n    </bpmn2:endEvent>\n    <bpmn2:sequenceFlow id=\"Flow_0vg1k9j\" sourceRef=\"Activity_15b3lpi\" targetRef=\"Event_10w321z\" />\n    <bpmn2:sequenceFlow id=\"Flow_1njox26\" sourceRef=\"Activity_1mc3ewp\" targetRef=\"Event_10w321z\" />\n  </bpmn2:process>\n  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"PROCESS202504021047428a4e8c7de9b04c698c7d3b6a2b323\">\n      <bpmndi:BPMNEdge id=\"Flow_1njox26_di\" bpmnElement=\"Flow_1njox26\">\n        <di:waypoint x=\"600\" y=\"400\" />\n        <di:waypoint x=\"680\" y=\"400\" />\n        <di:waypoint x=\"680\" y=\"276\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Flow_0vg1k9j_di\" bpmnElement=\"Flow_0vg1k9j\">\n        <di:waypoint x=\"600\" y=\"258\" />\n        <di:waypoint x=\"662\" y=\"258\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Flow_0rf6jnx_di\" bpmnElement=\"Flow_0rf6jnx\">\n        <di:waypoint x=\"420\" y=\"283\" />\n        <di:waypoint x=\"420\" y=\"400\" />\n        <di:waypoint x=\"500\" y=\"400\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Flow_1qlemkr_di\" bpmnElement=\"Flow_1qlemkr\">\n        <di:waypoint x=\"445\" y=\"258\" />\n        <di:waypoint x=\"500\" y=\"258\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Flow_0wg24k7_di\" bpmnElement=\"Flow_0wg24k7\">\n        <di:waypoint x=\"340\" y=\"258\" />\n        <di:waypoint x=\"395\" y=\"258\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Flow_1vtimti_di\" bpmnElement=\"Flow_1vtimti\">\n        <di:waypoint x=\"186\" y=\"258\" />\n        <di:waypoint x=\"240\" y=\"258\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNShape id=\"_BPMNShape_StartEvent_2\" bpmnElement=\"StartEvent_1\">\n        <dc:Bounds x=\"150\" y=\"240\" width=\"36\" height=\"36\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"Activity_1atj6ni_di\" bpmnElement=\"Activity_1atj6ni\">\n        <dc:Bounds x=\"240\" y=\"203\" width=\"100\" height=\"110\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"Gateway_05u49vv_di\" bpmnElement=\"Gateway_1cp3ssv\" isMarkerVisible=\"true\">\n        <dc:Bounds x=\"395\" y=\"233\" width=\"50\" height=\"50\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"Activity_15b3lpi_di\" bpmnElement=\"Activity_15b3lpi\">\n        <dc:Bounds x=\"500\" y=\"203\" width=\"100\" height=\"110\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"Activity_1mc3ewp_di\" bpmnElement=\"Activity_1mc3ewp\">\n        <dc:Bounds x=\"500\" y=\"345\" width=\"100\" height=\"110\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"Event_10w321z_di\" bpmnElement=\"Event_10w321z\">\n        <dc:Bounds x=\"662\" y=\"240\" width=\"36\" height=\"36\" />\n      </bpmndi:BPMNShape>\n    </bpmndi:BPMNPlane>\n  </bpmndi:BPMNDiagram>\n</bpmn2:definitions>\n",
        @JsonProperty("max_version")
        private Integer maxVersion;
        @JsonProperty("major_version")
        private Integer majorVersion;
        @JsonProperty("event_cd")
        private String eventCd;
        @JsonProperty("process_name")
        private String processName;
        @JsonProperty("delete_yn")
        private String deleteYn;
        @JsonProperty("event_name")
        private String eventName;
        @JsonProperty("process_cd")
        private String processCd;
        @JsonProperty("workspace_cd")
        private String workspaceCd;
        @JsonProperty("workspace_name")
        private String workspaceName;
    }
}