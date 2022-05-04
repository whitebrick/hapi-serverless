package com.whitebrick.hapi.function;

import com.serverless.ApiGatewayResponse;
import com.serverless.Response;

import java.util.Map;
import java.util.HashMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * hapi-serverless parser https://github.com/whitebrick/hapi-serverless
 * Forked from XML-HL7 parser by Matthew Vita https://github.com/MatthewVita/node-hl7-complete
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
    String hl7 = null;
    int statusCode = 200;

    Parser parser = new Parser();
    parser.setStrictMode(false);

    try {

      Object headers = input.get("headers");
      String contentType = ((Map<String, String>) headers).get("Content-Type");
      LOG.info("Content-Type: " + contentType.toLowerCase());

      String body = String.valueOf(input.get("body"));

      // application/json
      if (contentType.toLowerCase().contains("json")) {

        jsonObj = new JSONObject(body);
        String msgType = jsonObj.keySet().iterator().next();
        LOG.info("Processing JSON, msgType: " + msgType);

        xml = XML.toString(jsonObj).replace("<" + msgType + ">", "<" + msgType + " xmlns=\"urn:hl7-org:v2xml\">");
        hl7 = parser.xmlToHl7(xml);

        // x-application/hl7-v2+xml
      } else if (contentType.toLowerCase().contains("xml")) {

        xml = body;
        jsonObj = XML.toJSONObject(xml, true);
        hl7 = parser.xmlToHl7(xml);

        // x-application/hl7-v2+er7
      } else if (contentType.toLowerCase().contains("er7")) {

        hl7 = body;
        xml = parser.hl7ToXml(body);
        jsonObj = XML.toJSONObject(xml,true);

      } else {
        throw new Exception("Content-Type header must include 'json' or 'xml' or 'er7'");
      }

      jsonMap = new ObjectMapper().readValue(jsonObj.toString(), Map.class);

    } catch (Exception e) {
      LOG.info("Error: " + e.getMessage());
      statusCode = 500;
      errorMessage = e.getMessage();
    }

    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    responseBody = new Response(jsonMap, xml, hl7, errorMessage);
    return ApiGatewayResponse.builder().setStatusCode(statusCode).setObjectBody(responseBody).setHeaders(headers)
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