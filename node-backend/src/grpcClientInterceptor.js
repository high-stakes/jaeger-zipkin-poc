/* eslint-disable new-cap,no-shadow */
const Instrumentation = require('./grpcClientInstrumentation');

const interceptor = (grpc, {tracer, remoteServiceName}) => {
  const instrumentation = new Instrumentation(grpc, {tracer, remoteServiceName});

  return (options, nextCall) => {
    const method = options.method_definition.path;

    return tracer.scoped(() =>
      new grpc.InterceptingCall(nextCall(options), {

        start(metadata, listener, next) {
          const traceId = instrumentation.start(metadata, method);
          const zipkinMetadata = Instrumentation.setHeaders(metadata, traceId);

          next(zipkinMetadata, {
            onReceiveStatus(status, next) {
              instrumentation.onReceiveStatus(traceId, status);
              next(status);
            }
          });
        }
      })
    );
  };
};

module.exports = interceptor;