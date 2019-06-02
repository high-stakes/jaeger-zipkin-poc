const {Annotation, HttpHeaders} = require('zipkin');

function requiredArg(name) {
  throw new Error(`GrpcClientInstrumentation: Missing required argument ${name}.`);
}

class GrpcClientInstrumentation {

  constructor(grpc, {tracer = requiredArg('tracer'), remoteServiceName}) {
    this.grpc = grpc;
    this.tracer = tracer;
    this.serviceName = tracer.localEndpoint.serviceName;
    this.remoteServiceName = remoteServiceName;
  }

  static setHeaders(originalMetadata, traceId) {
    const metadata = originalMetadata.clone();
    metadata.add(HttpHeaders.TraceId, traceId.traceId);
    metadata.add(HttpHeaders.SpanId, traceId.spanId);

    traceId._parentId.ifPresent(psid => {
      metadata.add(HttpHeaders.ParentSpanId, psid);
    });
    traceId.sampled.ifPresent(sampled => {
      metadata.add(HttpHeaders.Sampled, sampled ? '1' : '0');
    });
    if (traceId.isDebug()) {
      metadata.add(HttpHeaders.Flags, '1');
    }

    return metadata;
  }

  start(metadata, method) {
    const traceId = this.tracer.createChildId()

    this.tracer.letId(traceId, () => {
        this.tracer.recordServiceName(this.serviceName);
        this.tracer.recordRpc(method);
        this.tracer.recordAnnotation(new Annotation.ClientSend());
        if (this.remoteServiceName) {
          this.tracer.recordAnnotation(new Annotation.ServerAddr({
            serviceName: this.remoteServiceName
          }));
        }
    });
    return traceId;
  }

  onReceiveStatus(traceId, status) {
    const {code} = status;
    this.tracer.letId(traceId, () => {
        if (code !== this.grpc.status.OK) {
          this.tracer.recordBinary('grpc.status_code', String(code));
          this.tracer.recordBinary('error', String(code));
        }
        this.tracer.recordAnnotation(new Annotation.ClientRecv());
    });
  }
}

module.exports = GrpcClientInstrumentation;