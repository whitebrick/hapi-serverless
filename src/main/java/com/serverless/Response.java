package com.serverless;

import java.util.Map;

public class Response {

  private final Map<String, Object> jsonMap;
  private final String xml;
  private final String er7;
  private final String error;

  public Response(Map<String, Object> jsonMap, String xml, String er7, String error) {
    this.jsonMap = jsonMap;
    this.xml = xml;
    this.er7 = er7;
    this.error = error;
  }

  public Map<String, Object> getJson() {
    return this.jsonMap;
  }

  public String getXml() {
    return this.xml;
  }

  public String getEr7() {
    return this.er7;
  }

  public String getError() {
    return this.error;
  }
}
