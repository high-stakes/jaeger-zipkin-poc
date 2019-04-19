const { BatchRecorder, jsonEncoder: {JSON_V2}} = require('zipkin');
const {HttpLogger} = require('zipkin-transport-http');
const {Tracer, ExplicitContext} = require('zipkin');
const wrapFetch = require('zipkin-instrumentation-fetch');
process.hrtime = require('browser-process-hrtime');

const recorder = new BatchRecorder({
  logger: new HttpLogger({
    endpoint: 'http://localhost:3001/api/v2/spans',
    jsonEncoder: JSON_V2
  })
});
const tracer = new Tracer({ctxImpl: new ExplicitContext(), recorder, localServiceName: 'browser'});
const zipkinFetch = wrapFetch(fetch, {tracer});
const logEl = document.getElementById('log');
const log = text => logEl.innerHTML = `${logEl.innerHTML}\n${text}`;

zipkinFetch('/api/hello')
  .then(response => (response.text()))
  .then(text => log(text))
  .catch(err => log(`Failed:\n${err.stack}`));