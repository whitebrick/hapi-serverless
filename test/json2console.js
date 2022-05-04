#!/usr/bin/env node

var stringify = require("json-stable-stringify");
const process = require("process");
process.stdin.on("data", (data) => {
  var obj = JSON.parse(data.toString());
  var s;
  if(process.argv.slice(2).length>0){
    obj = obj[process.argv.slice(2).toString()];
  }
  if (
    process.argv.slice(2).length > 0 &&
    process.argv.slice(2).toString() != "json"
  ) {
    s = obj;
  } else {
    delete obj["ORU_R01"]["xmlns"];
    s = stringify(obj, function (a, b) {
      return a.key > b.key ? 1 : -1;
    });
  }
  console.log(s)
});
