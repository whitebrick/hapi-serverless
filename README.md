# hapi-serverless

#### HL7 2.x JSON/XML/ER7 (Pipe-delimited) Message Parser & Transformer

This is a fork of [node-hl7-complete](https://github.com/MatthewVita/node-hl7-complete) refactored as a Java-only function and packaged with the [Serverless](https://github.com/serverless/serverless) framework for easy deployment to AWS Lambda with API Gateway and other cloud platforms.

When deployed this stack stands up a HTTP endpoint that supports transforms of HL7 v2.x messages between `JSON`, `XML` and `ER7` (pipe-delimited) formats using [Java Hapi](https://hapifhir.github.io), the gold standard for HL7 parsing.

##### MLLP

ER7 messages can also be forwarded directly to a HTTP-MLLP gateway (eg. [http-mllp-node](https://github.com/whitebrick/http-mllp-node)) using headers (see below) and the ACK response parsed and returned in `JSON`, `XML` and `ER7` formats.

A simple MLLP HL7 V2.x ACK response server is available for testing, more information [here](https://hl7v2-test.whitebrick.com).

### Overview

The endpoint receives a HTTP POST request containing either:

1. A JSON representation of a HL7 message (see [test example](https://github.com/whitebrick/hapi-serverless/blob/main/test/functional/cerner_ORU_R01.json))
```json
"PID.5": {
  "XPN.1": {
    "FN.1": "Miller"
  },
  "XPN.2": "Paul",
```

2. OR an XML representation of a HL7 message (see [test example](https://github.com/whitebrick/hapi-serverless/blob/main/test/functional/cerner_ORU_R01.xml))
```xml
<PID.5>
  <XPN.1>
    <FN.1>Miller</FN.1>
  </XPN.1>
  <XPN.2>Paul</XPN.2>
```
3. OR an ER7 (pipe-delimited) representation of a HL7 message (see [test example](https://github.com/whitebrick/hapi-serverless/blob/main/test/functional/cerner_ORU_R01.er7))
```
PID|1||9339683996^^^Baseline West MC&33D1234567&L^MR^Baseline West MC&33D1234567&L|7903^^^Cerner Corp|Miller^Paul^One^^^^L||20050715050000|M||White^Caucasian^HL70005|555 Flower Street^^Aurora^CO^80011^USA^C||^PRN^PH^^^303^5549936||||||765894312|||N^Non Hispanic^HL70189
```

The message is parsed and the server responds with JSON containing either all 3 formats of the HL7 message or an error. If this response is too verbose, the `?prune_for=json|xml|er7` request parameter can also be passed to nullify the other keys.

```json
{
  "json": { "PID.5": { "XPN.1": { "FN.1": "Miller" }, "XPN.2": "Paul" } },
  "xml": "<PID.5>\n  <XPN.1>\n    <FN.1>Miller</FN.1>\n  </XPN.1>\n ...",
  "er7": "PID|1| ... |Miller^Paul^One^^^^L|| ...\r",
  "error": "null | <error message>"
}
```

##### ER7 Message Forwarding

If `MLLP-Gateway` and `Forward-To` headers are included, the er7 message is forwarded to the host and port specified in the `MLLP-Gateway`  header value, along with the `Forward-To` header and value. The response from the gateway is parsed and added to the HTTP response as below (see [test example](https://github.com/whitebrick/hapi-serverless/blob/main/test/functional/cerner_ORU_R01_ACK.json)).

```json
{
  "json": { "PID.5": { "XPN.1": { "FN.1": "Miller" }, "XPN.2": "Paul" } },
  "xml": "<PID.5>\n  <XPN.1>\n    <FN.1>Miller</FN.1>\n  </XPN.1>\n ...",
  "er7": "PID|1| ... |Miller^Paul^One^^^^L|| ...\r",
  "error": "null | <error message>",
  "retJson": { "MSH.22": "AA", "MSH.12": { "VID.1": "2.5.1" }, "MSH.23": "20101001091300" },
  "retXml": "<MSH.22>AA</MSH.22>\n <MSH.23>201010010913000772</MSH.23>\n </MSH>\n ...",
  "retEr7": "... ^^2.16.840.1.114222.4.10.3^ISOMSA|AA|201010010913000772 ...\r",
}
```

### Sending Requests

Requests are switched using the `Content-Type` header

- Example `JSON` request
```
curl -X POST -H "Content-Type: application/json" --data-binary @./test/functional/cerner_ORU_R01.json https://hapi.whitebrick.com
```
- Example `XML` request
```
curl -X POST -H "Content-Type: application/xml" --data-binary @./test/functional/cerner_ORU_R01.xml https://hapi.whitebrick.com
```
- Example `ER7` request
```
curl -X POST -H "Content-Type: text/plain" --data-binary @./test/functional/cerner_ORU_R01.er7 https://hapi.whitebrick.com
```

- Example forwarding request (can be used with any format)

```
curl -X POST -H "Content-Type: application/json" -H "Forward-To: "mllp://ack.whitebrick.com:2575" -H "MLLP-Gateway: http://host.docker.internal:3030" --data-binary @./cerner_ORU_R01.json http://localhost:3000/dev
```

### Running Locally


```
# install dependencies for Serverless and testing
npm install

# compile and package the Java jar
mvn package

# serverless-offline uses docker to support java8
# pull the image first so it's ready rather than keeping the initial server request hanging
docker pull lambci/lambda:java8

# watches for changes of the jar and start the local server
npm start
```

### Testing

```
cd test/functional
bash test_forwarding.bash
bash test_parsing.bash
```

**NB:** Serverless in local/offline mode uses a Docker container for the JRE so the initial request may take up to 20 seconds to respond.

More tests available at [node-hl7-complete](https://github.com/MatthewVita/node-hl7-complete) but in order to match the output exactly the JSON needs to be refactored because [xml2js](https://github.com/Leonidas-from-XIV/node-xml2js) uses `explicitArray` by default and no Java XML-to-JSON libraries could be found to support this.

### Deploying

```
# check provider values in serverless.yml

serverless deploy [--stage <stage>]
```

### Troubleshooting

- Line-endings and escaping can cause issues between different operating systems when dealing with these formats. ER7 requires  `\r` so some text post-processing may be needed.

### Contributing

- Questions, comments, suggestions and contributions welcome - contact: _hello_ at _whitebrick_ dot _com_
