#!/usr/bin/env node

const fs = require("fs");
const tool = require("diff-js-xml");
const params = process.argv.slice(2);
let lhs = fs.readFileSync(params[0], "utf8");
let rhs = fs.readFileSync(params[1], "utf8");
tool.diffAsXml(lhs, rhs, null, null, (result) => {
  // console.log(result);
  process.exit(result.length);
});
