# !/usr/bin/env bash

ENDPOINT="http://localhost:3000/dev"
#ENDPOINT="https://hapi.whitebrick.com/"
#MLLP_GATEWAY="http://localhost:3030"
MLLP_GATEWAY="http://host.docker.internal:3030"
FORWARD_TO="mllp://ack.whitebrick.com:2575"

echo -e "\nTesting forwarding against endpoint: $ENDPOINT"

echo -e "\n\n==== Test 1/1 Sending JSON format with Forward-To and MLLP-Gateway headers..."

cmd="curl -s -X POST -H \"Content-Type: application/json\" -H \"Forward-To: $FORWARD_TO\" -H \"MLLP-Gateway: $MLLP_GATEWAY\" --data-binary @./cerner_ORU_R01.json $ENDPOINT > tmp_response_gateway_all.json"
echo -e "\n$cmd\n"
eval $cmd

cmd="cat tmp_response_gateway_all.json | node json2console.js retJson > tmp_response_gateway_json.json"
echo $cmd
eval $cmd

echo -e "\n- Checking JSON response..."
cmd="cat cerner_ORU_R01_ACK.json | tr -d '\n' | node json2console.js | diff -q tmp_response_gateway_json.json -"
echo $cmd
eval $cmd

if [ $? -ne 0 ]; then
echo -e "\nError: JSON diff failed\n"
exit 1
else
echo -e "OK"
fi

echo -e "\n\n==== Complete - removing tmp files...\n"

rm -f tmp_response_*
