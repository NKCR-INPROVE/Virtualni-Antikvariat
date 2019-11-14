const PROXY_CONFIG = {
  "/api/**": {
    "target": "http://localhost:443/api",
    "logLevel": "debug",
    "bypass": function (req, res, proxyOptions) {
      //req.headers["X-Custom-Header"] = "yes";
      
      if (req.method === 'POST') {
        req.method = 'GET';
      }
      if (req.path.indexOf('/search') > -1) {
        //serve static json instead
        return "/mock/search.json";
      } else if (req.path.indexOf('/lg') > -1) {
        //serve static json instead
        return "/mock/pingAuth.json";
      } else {
        return req.url;
      }
    },
    "pathRewrite": {
      "^/api":""
    },
    "changeOrigin": false,
    "secure": false
  }
};
module.exports = PROXY_CONFIG;
