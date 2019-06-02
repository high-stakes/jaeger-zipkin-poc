const grpc = require('grpc')
const grpcInstrumentation = require('./grpcClientInterceptor');
const {expressMiddleware, wrapExpressHttpProxy} = require('zipkin-instrumentation-express')
const {Tracer,BatchRecorder,ConsoleRecorder, jsonEncoder: {JSON_V2}, ExplicitContext} = require('zipkin');
const CLSContext = require('zipkin-context-cls');
const {HttpLogger} = require('zipkin-transport-http');

const tracer = new Tracer({
  supportsJoin : false,
  ctxImpl: new CLSContext(),
  recorder: new BatchRecorder({
    logger: new HttpLogger({
      endpoint: 'http://zipkin:9411/api/v2/spans',
      jsonEncoder: JSON_V2,
      httpInterval: 100
    })
  }),
  localServiceName: 'node-backend'
});
const zipkinGrcpInterceptor = grpcInstrumentation(grpc, {tracer, remoteServiceName: 'hello-service'} );
const Greeter = grpc.load(__dirname  + '/../../proto/hello.proto').helloworld.Greeter
const grcpClient = new Greeter('spring-backend:6565',grpc.credentials.createInsecure())
const zipkinExpressInterceptor = expressMiddleware({tracer, serviceName: 'node-backend'} );

module.exports = { grcpClient, zipkinGrcpInterceptor, zipkinExpressInterceptor, tracer }