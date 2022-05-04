curl -X POST \
  'localhost:3000' \
  -H 'Accept: */*' \
  -H 'Content-Type: x-application/hl7-v2+er7' \
  -d 'MSH|^~\&|EHR Application^2.16.840.1.113883.3.72.7.1^HL7|EHR Facility^2.16.840.1.113883.3.72.7.2^HL7|PH Application^2.16.840.1.113883.3.72.7.3^HL7|PH Facility^2.16.840.1.113883.3.72.7.4^HL7|20100929110702||ORU^R01^ORU_R01|NIST-100929110701660|P|2.5.1|||||||||PHLabReport-NoAck^^2.16.840.1.114222.4.10.3^ISO
SFT|NIST Lab, Inc.|3.6.23|A-1 Lab System|6742873-12||20080303
PID|||9817566735^^^MPI&2.16.840.1.113883.19.3.2.1&ISO^MR||Johnson^Philip||20070526|M||2106-3^White^HL70005|3345 Elm Street^^Aurora^Colorado^80011^USA^M||^PRN^^^^303^5548889|||||||||N^Not Hispanic or Latino^HL70189
ORC|RE|||||||||||1234^Admit^Alan^^^^^^ABC Medical Center&2.16.840.1.113883.19.4.6&ISO|||||||||Level Seven Healthcare^L^^^^ABC Medical Center&2.16.840.1.113883.19.4.6&ISO^XX^^^1234|1005 Healthcare Drive^^Ann Arbor^MI^48103^^B|^^^^^734^5553001|4444 Healthcare Drive^^Ann Arbor^MI^48103^^B
OBR|1||9700123^Lab^2.16.840.1.113883.19.3.1.6^ISO|10368-9^Lead BldC-mCnc^LN^3456543^Blood lead test^99USI|||200808151030-0700||||||Diarrhea|||1234^Admit^Alan^^^^^^ABC Medical Center&2.16.840.1.113883.19.4.6&ISO||||||200808181800-0700|||F||||||787.91^DIARRHEA^I9CDX
OBX|1|NM|10368-9^Lead BldC-mCnc^LN|1|50|ug/dL^micro-gram per deci-liter^UCUM|<9 mcg/dL: Acceptable background lead exposure|H|||F|||200808151030-0700|||||200808181800-0700||||Lab^L^^^^CLIA&2.16.840.1.113883.19.4.6&ISO^XX^^^1236|3434 Industrial Lane^^Ann Arbor^MI^48103^^B
SPM||||122554006^Capillary blood specimen^SCT^BLDC^Blood capillary^HL70070^20080131^2.5.1'