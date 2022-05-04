# !/usr/bin/env bash

ENDPOINT="http://localhost:3000/"

echo -e "\nTesting against endpoint: $ENDPOINT"

function check_result () {

  if [ $? -ne 0 ]; then
    echo -e "\nError fetching data, try the curl command without -s to debug\n"
    exit 1
  fi

  echo -e "\nSeparating out JSON response\n"

  cmd="cat tmp_response_all.json | node json2console.js json > tmp_response_json.json"
  echo $cmd
  eval $cmd
  cmd="cat tmp_response_all.json | node json2console.js xml > tmp_response_xml.xml"
  echo $cmd
  eval $cmd
  cmd="cat tmp_response_all.json | node json2console.js hl7  > tmp_response_hl7.hl7"
  echo $cmd
  eval $cmd
  cmd="cat tmp_response_all.json | node json2console.js error  > tmp_response_error.txt"
  echo $cmd
  eval $cmd

  no_error=$(grep -c "null" tmp_response_error.txt)
  if [ $no_error -ne 1 ]; then
    echo -e "\nError processing data, view tmp_response_error.txt for details\n"
    exit 1
  fi

  echo -e "\n- Checking JSON response..."
  cmd="cat cerner_ORU_R01.json | tr -d '\n' | node json2console.js | diff -q tmp_response_json.json -"
  echo $cmd
  eval $cmd

  if [ $? -ne 0 ]; then
    echo -e "\nError: JSON diff failed\n"
    exit 1
  else
    echo -e "OK"
  fi

  echo -e "\n- Checking XML response..."
  cmd="node xmldiff.js cerner_ORU_R01.xml tmp_response_xml.xml"
  echo $cmd
  eval $cmd

  if [ $? -ne 0 ]; then
    echo -e "\nError: XML diff failed\n"
    exit 1
  else
    echo -e "OK"
  fi

  echo -e "\n- Checking HL7 response..."
  cmd="diff -q --ignore-blank-lines tmp_response_hl7.hl7 cerner_ORU_R01.hl7"
  echo $cmd
  eval $cmd

  if [ $? -ne 0 ]; then
    echo -e "n\Error: HL7 diff failed\n"
    exit 1
  else
    echo -e "OK"
  fi
}

echo -e "\n\n==== Test 1/3 Sending JSON format..."

cmd="curl -s -X POST -H \"Content-Type: application/json\" --data-binary @./cerner_ORU_R01.json $ENDPOINT > tmp_response_all.json"
echo -e "\n$cmd\n"
eval $cmd
check_result

echo -e "\n\n==== Test 2/3 Sending XML format..."

cmd="curl -s -X POST -H \"Content-Type: x-application/hl7-v2+xml\" --data-binary @./cerner_ORU_R01.xml $ENDPOINT > tmp_response_all.json"
echo -e "\n$cmd\n"
eval $cmd
check_result

echo -e "\n\n==== Test 3/3 Sending HL7 format..."

cmd="curl -s -X POST -H \"Content-Type: x-application/hl7-v2+er7\" --data-binary @./cerner_ORU_R01.hl7 $ENDPOINT > tmp_response_all.json"
echo -e "\n$cmd\n"
eval $cmd
check_result

echo -e "\n\n==== Complete - removing tmp files...\n"

rm -f tmp_response_*
