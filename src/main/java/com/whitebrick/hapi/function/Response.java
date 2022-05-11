package com.whitebrick.hapi.function;

import java.util.Map;

public class Response {

  private final Map<String, Object> jsonMap;
  private final String xml;
  private final String er7;
  private final String error;
  private final Map<String, Object> fwdJsonMap;
  private final String fwdXml;
  private final String fwdEr7;

  public Response(Map<String, Object> jsonMap, String xml, String er7, String error, Map<String, Object> fwdJsonMap,
      String fwdXml, String fwdEr7) {
    this.jsonMap = jsonMap;
    this.xml = xml;
    this.er7 = er7;
    this.error = error;
    this.fwdJsonMap = fwdJsonMap;
    this.fwdXml = fwdXml;
    this.fwdEr7 = fwdEr7;
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

  public Map<String, Object> getFwdJson() {
    return this.fwdJsonMap;
  }

  public String getFwdXml() {
    return this.fwdXml;
  }

  public String getFwdEr7() {
    return this.fwdEr7;
  }
}
