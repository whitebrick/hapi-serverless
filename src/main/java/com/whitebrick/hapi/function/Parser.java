package com.whitebrick.hapi.function;

import com.serverless.ApiGatewayResponse;

import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.util.HashMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.parser.XMLParser;
import ca.uhn.hl7v2.parser.DefaultXMLParser;

import org.json.JSONObject;
import org.json.XML;

/**
 * hapi-serverless parser https://github.com/whitebrick/hapi-serverless Forked
 * from XML-HL7 parser by Matthew Vita
 * https://github.com/MatthewVita/node-hl7-complete
 */
public class Parser implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

  private static final Logger LOG = Logger.getLogger(Parser.class);

  private Boolean strictMode = true; // Defaults to strict mode (HL7 validation)

  // public Parser() {}

  @Override
  public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
    LOG.info("handleRequest input: " + input);

    JSONObject jsonObj = null;
    Map<String, Object> jsonMap = null;
    Response responseBody = null;
    String errorMessage = null;
    String xml = null;
    String er7 = null;
    String fwdEr7 = null;
    Map<String, Object> fwdJsonMap = null;
    String fwdXml = null;
    
    int statusCode = 200;

    Parser parser = new Parser();
    parser.setStrictMode(false);

    Map<String, String> requestHeaders = new HashMap<String, String>();

    try {

      requestHeaders = ((Map<String, String>) input.get("headers")).entrySet().stream()
          .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), entry -> entry.getValue()));

      String contentType;

      if (requestHeaders.containsKey("content-type")) {
        contentType = requestHeaders.get("content-type");
      } else {
        throw new Exception("Could not find Content-Type header");
      }

      LOG.info("Content-Type: " + contentType.toLowerCase());

      String body = String.valueOf(input.get("body"));

      // application/json
      if (contentType.toLowerCase().contains("json")) {
        jsonObj = new JSONObject(body);
        String msgType = jsonObj.keySet().iterator().next();
        LOG.info("Processing JSON, msgType: " + msgType);
        xml = XML.toString(jsonObj).replace("<" + msgType + ">", "<" + msgType + " xmlns=\"urn:hl7-org:v2xml\">");
        er7 = parser.xmlToHl7(xml);

        // application/xml
      } else if (contentType.toLowerCase().contains("xml")) {
        xml = body;
        jsonObj = XML.toJSONObject(xml, true);
        er7 = parser.xmlToHl7(xml);
        
        // text/plain
      } else if (contentType.toLowerCase().contains("text")) {
        er7 = body;
        xml = parser.hl7ToXml(body);
        jsonObj = XML.toJSONObject(xml, true);
      } else {
        throw new Exception("Content-Type header must include 'json' or 'xml' or 'text'");
      }

      jsonMap = new ObjectMapper().readValue(jsonObj.toString(), Map.class);

    } catch (Exception e) {
      LOG.info("Error: " + e.getMessage());
      statusCode = 500;
      errorMessage = e.getMessage();
    }

    if (requestHeaders.containsKey("mllp-gateway") && requestHeaders.containsKey("forward-to")) {
      
      LOG.info("MLLP-Gateway and Forward-To headers found... forwarding POST request to "+requestHeaders.get("mllp-gateway"));
      
      CloseableHttpClient httpClient = HttpClients.custom()
      .setSSLHostnameVerifier(new NoopHostnameVerifier())
      .build();
      
      HttpPost post = new HttpPost(requestHeaders.get("mllp-gateway"));
      post.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
      post.setHeader("forward-to", requestHeaders.get("forward-to"));
      StringEntity requestEntity = new StringEntity(er7, ContentType.DEFAULT_TEXT);
      post.setEntity(requestEntity);
      try {
        HttpResponse fwdResponse = httpClient.execute(post);
        HttpEntity fwdResponseBodyentity = fwdResponse.getEntity();
        String fwdResponseBodyString = EntityUtils.toString(fwdResponseBodyentity);
        JSONObject fwdResponseJsonObj = new JSONObject(fwdResponseBodyString);
        
        fwdEr7 = fwdResponseJsonObj.getString("er7");
        fwdXml = parser.hl7ToXml(fwdEr7);
        JSONObject fwdJsonObj = XML.toJSONObject(fwdXml, true);
        fwdJsonMap = new ObjectMapper().readValue(fwdJsonObj.toString(), Map.class);
        
      } catch (Exception e) {
        System.out.println("Error forwarding/parsing with "+requestHeaders.get("mllp-gateway"));
        e.printStackTrace();
      } finally {
        try {
          httpClient.close();
        } catch (IOException e) {
          System.out.println("Error closing stream after sending HTTP POST to "+requestHeaders.get("mllp-gateway"));
          e.printStackTrace();
        }
      }
    }

    Map<String, String> responseHeaders = new HashMap<>();
    responseHeaders.put("Content-Type", "application/json");

    responseBody = new Response(jsonMap, xml, er7, errorMessage, fwdJsonMap, fwdXml, fwdEr7);
    return ApiGatewayResponse.builder().setStatusCode(statusCode).setObjectBody(responseBody).setHeaders(responseHeaders)
        .build();
  }

  public void setStrictMode(Boolean value) {
    strictMode = value;
  }

  public String hl7ToXml(String hl7String) throws HL7Exception {
    String responseString = "";

    // Strips out HL7 pipes and transforms data into basic Hapi data structure
    PipeParser pipeParser;

    if (!strictMode) {
      pipeParser = PipeParser.getInstanceWithNoValidation();
    } else {
      pipeParser = new PipeParser();
    }

    Message hl7Message = pipeParser.parse(hl7String);

    // Represents data as XML
    XMLParser xmlParser = new DefaultXMLParser();
    String xmlString = xmlParser.encode(hl7Message);

    // Cleans up data so response has just the contents (no xml metadata)
    responseString = xmlString.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "")
        .replace(" xmlns=\"urn:hl7-org:v2xml\"", "");

    return responseString;
  }

  // TODO: remove the validation-less instance in favor of the validated one
  public String xmlToHl7(String xmlString) throws HL7Exception {
    String responseString = "";

    // Transforms into basic Hapi data structure
    GenericParser genericParser = GenericParser.getInstanceWithNoValidation();
    Message hl7Message = genericParser.parse(xmlString);

    // Applies all necessary HL7 pipes to the basic structure
    PipeParser pipeParser = new PipeParser();
    responseString = pipeParser.encode(hl7Message);

    return responseString;
  }

}