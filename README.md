# hapi-serverless

#### HL7 2.x JSON/XML/ER7 (Pipe-delimited) Message Parser & Transformer

This is a fork of [node-hl7-complete](https://github.com/MatthewVita/node-hl7-complete) refactored as a Java-only function and packaged with the [Serverless](https://github.com/serverless/serverless) framework for easy deployment to AWS Lambda and other cloud platforms.

Deploying this stack stands up a HTTP endpoint that supports transforms of HL7 v2.x messages between `JSON`, `XML` and `ER7` (pipe-delimited) formats using [Java Hapi](https://hapifhir.github.io), the gold standard for HL7 parsing.

### Overview

The endpoint receives a HTTP POST request containing either:

1. A JSON representation of a HL7 message (see [test example](https://github.com/whitebrick/hapi-serverless/blob/main/test/cerner_ORU_R01.json))
```json
"PID.5": {
  "XPN.1": {
    "FN.1": "Miller"
  },
  "XPN.2": "Paul",
```

2. OR an XML representation of a HL7 message (see [test example](https://github.com/whitebrick/hapi-serverless/blob/main/test/cerner_ORU_R01.xml))
```xml
<PID.5>
  <XPN.1>
    <FN.1>Miller</FN.1>
  </XPN.1>
  <XPN.2>Paul</XPN.2>
```
3. OR an ER7 (pipe-delimited) representation of a HL7 message (see [test example](https://github.com/whitebrick/hapi-serverless/blob/main/test/cerner_ORU_R01.er7))
```

PID|1||9339683996^^^Baseline West MC&33D1234567&L^MR^Baseline West MC&33D1234567&L|7903^^^Cerner Corp|Miller^Paul^One^^^^L||20050715050000|M||White^Caucasian^HL70005|555 Flower Street^^Aurora^CO^80011^USA^C||^PRN^PH^^^303^5549936||||||765894312|||N^Non Hispanic^HL70189
```

The message is parsed and the server responds with JSON containing either all 3 formats of the HL7 message or an error.

```json
{
  "json": { "PID.5": { "XPN.1": { "FN.1": "Miller" }, "XPN.2": "Paul" } },
  "xml": "<PID.5>\n  <XPN.1>\n    <FN.1>Miller</FN.1>\n  </XPN.1>\n ...",
  "er7": "PID|1| ... |Miller^Paul^One^^^^L|| ...\r",
  "error": "null | <error message>"
}
```

### Sending Requests

Requests are switched using the `Content-Type` header

- Example `JSON` request
```
curl -X POST -H "Content-Type: application/json" --data-binary @./test/cerner_ORU_R01.json https://hapi.whitebrick.com
```
- Example `XML` request
```
curl -X POST -H "Content-Type: application/xml" --data-binary @./test/cerner_ORU_R01.xml https://hapi.whitebrick.com
```
- Example `ER7` request
```
curl -X POST -H "Content-Type: text/plain" --data-binary @./test/cerner_ORU_R01.er7 https://hapi.whitebrick.com
```

### Running Locally


```
# install dependencies for Serverless and testing
npm install

# compile and package the Java jar
mvn package

# serverless-offline uses docker to support java8
# pull the image first so it's ready rather than on the first server request
docker pull lambci/lambda:java8

# watch for changes of the jar and start the local server
npm start
```

### Testing

```
cd test
bash functional.bash
```

More tests available at [node-hl7-complete](https://github.com/MatthewVita/node-hl7-complete) but to match the output exactly the JSON needs to be refactored because [xml2js](https://github.com/Leonidas-from-XIV/node-xml2js) uses `explicitArray` by default and no Java XML-to-JSON libraries could be found to support this.

### Deploying

```
# check provider values in serverless.yml

serverless deploy [--stage <stage>]
```

### Troubleshooting

- Line-endings and escaping can cause issues between different operating systems when dealing with these formats. ER7 requires  `\r` so check text carefully.
