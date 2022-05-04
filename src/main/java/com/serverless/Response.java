package com.serverless;

import java.util.Map;

public class Response {

  private final Map<String, Object> jsonMap;
  private final String xml;
  private final String hl7;
  private final String error;

  public Response(Map<String, Object> jsonMap, String xml, String hl7, String error) {
    this.jsonMap = jsonMap;
    this.xml = xml;
    this.hl7 = hl7;
    this.error = error;
  }

  public Map<String, Object> getJson() {
    return this.jsonMap;
  }

  public String getXml() {
    return this.xml;
  }

  public String getHl7() {
    return this.hl7;
  }

  public String getError() {
    return this.error;
  }
}
