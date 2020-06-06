var express = require('express');
var app = express();

// Web API:
var calendar_api = require('calendar_api.js');
app.use(calendar_api.router);

app.listen(3000, () => {
    console.log("Server started on port 3000");
});