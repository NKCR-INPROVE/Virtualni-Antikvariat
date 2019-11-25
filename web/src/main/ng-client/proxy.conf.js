const PROXY_CONFIG = {
  "/api/**": {
    "target": "http://localhost:8080/vdk/api",
    "logLevel": "debug",
    "bypass": function (req, res, proxyOptions) {
      req.headers["X-Custom-Header"] = "yes";      
      console.log(req.path);
      if (req.path.indexOf('/assets') > -1) {
        return req.url;
      }
      if (req.method === 'POST') {
        req.method = 'GET';
      }
      if (req.path.indexOf('/search') > -1) {
        // return "/mock/search.json";
      } else if (req.path.indexOf('/users/login') > -1) {
        return "/mock/pingAuth.json";
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
