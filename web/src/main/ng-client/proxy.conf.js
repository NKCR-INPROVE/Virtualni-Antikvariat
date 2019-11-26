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
      if (req.path.indexOf('/search') > -1) {
        // return "/mock/search.json";
      } else if (req.path.indexOf('/users/login') > -1) {
        if (req.method === 'POST') {
          req.method = 'GET';
        }
        return "/mock/user.json";
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
