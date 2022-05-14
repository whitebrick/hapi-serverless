package com.whitebrick.hapi.function;

import java.util.Map;

public class Response {

  private final Map<String, Object> jsonMap;
  private final String xml;
  private final String er7;
  private final String error;
  private final Map<String, Object> retJsonMap;
  private final String retXml;
  private final String retEr7;

  public Response(Map<String, Object> jsonMap, String xml, String er7, String error, Map<String, Object> retJsonMap,
      String retXml, String retEr7) {
    this.jsonMap = jsonMap;
    this.xml = xml;
    this.er7 = er7;
    this.error = error;
    this.retJsonMap = retJsonMap;
    this.retXml = retXml;
    this.retEr7 = retEr7;
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

  public Map<String, Object> getretJson() {
    return this.retJsonMap;
  }

  public String getretXml() {
    return this.retXml;
  }

  public String getretEr7() {
    return this.retEr7;
  }
}
