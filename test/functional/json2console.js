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
    !process.argv.slice(2).toString().toLocaleLowerCase().includes("json")
  ) {
    s = obj;
  } else {
    if (obj["ORU_R01"]) delete obj["ORU_R01"]["xmlns"];
    if (
      obj["ORU_R01"] &&
      obj["ORU_R01"]["MSH"] &&
      obj["ORU_R01"]["MSH"]["MSH.7"]
    ){
      delete obj["ORU_R01"]["MSH"]["MSH.7"]["TS.1"];
    }
    s = stringify(obj, function (a, b) {
      return a.key > b.key ? 1 : -1;
    });
  }
  console.log(s)
});
