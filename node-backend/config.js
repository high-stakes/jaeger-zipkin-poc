const grpc = require('grpc')
const grpcInstrumentation = require('./grpcClientInterceptor');
const {expressMiddleware, wrapExpressHttpProxy} = require('zipkin-instrumentation-express')
const {Tracer,BatchRecorder,ConsoleRecorder, jsonEncoder: {JSON_V2}, ExplicitContext} = require('zipkin');
const CLSContext = require('zipkin-context-cls');
const {HttpLogger} = require('zipkin-transport-http');

const tracer = new Tracer({
  ctxImpl: new CLSContext(),
  recorder: new BatchRecorder({
    logger: new HttpLogger({
      endpoint: 'http://localhost:9411/api/v2/spans',
      jsonEncoder: JSON_V2,
      httpInterval: 100
    })
  }),
  localServiceName: 'node-backend'
});
const zipkinGrcpInterceptor = grpcInstrumentation(grpc, {tracer, remoteServiceName: 'hello-service'} );
const Greeter = grpc.load('../api-protos/hello.proto').helloworld.Greeter
const grcpClient = new Greeter('localhost:6565',grpc.credentials.createInsecure())
const zipkinExpressInterceptor = expressMiddleware({tracer, serviceName: 'node-backend'} );

module.exports = { grcpClient, zipkinGrcpInterceptor, zipkinExpressInterceptor, tracer }