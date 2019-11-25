const PROXY_CONFIG = {
  onProxyReq(proxyReq, req, res) {
    console.log('tady');
    console.log(proxyReq);
  },
  "/api/**": {
    "target": "http://localhost:443/api",
    "logLevel": "debug",
    "bypass": function (req, res, proxyOptions) {
      req.headers["X-Custom-Header"] = "yes";      
      
      if (req.path.indexOf('/assets') > -1) {
        return req.url;
      }
      if (req.method === 'POST') {
        req.method = 'GET';
      }
      if (req.path.indexOf('/search') > -1) {

        return "/mock/search.json";
      } else if (req.path.indexOf('/users') > -1) {
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
