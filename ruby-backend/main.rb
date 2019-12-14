this_dir = File.expand_path(File.dirname(__FILE__))
lib_dir = File.join(this_dir, 'lib')
$LOAD_PATH.unshift(lib_dir) unless $LOAD_PATH.include?(lib_dir)

require 'grpc-opentracing'
require 'grpc'
require 'hello_services_pb'
require 'zipkin/tracer'
require 'opentracing'
require 'jaeger/client'
require 'logger'

# RubyLogger defines a logger for gRPC based on the standard ruby logger.
module RubyLogger
  def logger
    LOGGER
  end

  LOGGER = Logger.new(STDOUT)
  LOGGER.level = Logger::DEBUG
end

# GRPC is the general RPC module
module GRPC
  # Inject the noop #logger if no module-level logger method has been injected.
  extend RubyLogger
end

class B3TextMapCodec
  def self.extract(carrier)
    trace_id = Jaeger::TraceId.base16_hex_id_to_uint64(carrier['x-b3-traceid'])
    span_id = Jaeger::TraceId.base16_hex_id_to_uint64(carrier['x-b3-spanid'])
    parent_id = Jaeger::TraceId.base16_hex_id_to_uint64(carrier['x-b3-parentspanid'])
    flags = parse_flags(carrier['x-b3-flags'], carrier['x-b3-sampled'])

    return nil if span_id.nil? || trace_id.nil?
    return nil if span_id.zero? || trace_id.zero?

    Jaeger::SpanContext.new(
      trace_id: trace_id,
      parent_id: parent_id,
      span_id: span_id,
      flags: flags
    )
  end

  def self.parse_flags(flags_header, sampled_header)
    if flags_header == '1'
      Jaeger::SpanContext::Flags::DEBUG
    else
      Jaeger::TraceId.base16_hex_id_to_uint64(sampled_header)
    end
  end
  private_class_method :parse_flags

end

OpenTracing.global_tracer = Jaeger::Client.build(
                              host: 'simple-prod-collector',
                              port: 6831,
                              service_name: 'ruby-backend',
                              extractors: {
                                OpenTracing::FORMAT_TEXT_MAP => [B3TextMapCodec]
                              }
                            )

class GreeterServer < Helloworld::Greeter::Service
  def say_hello(hello_req, _unused_call)
    puts ("Request" + hello_req.name)
    Helloworld::HelloReply.new(message: "Hello #{hello_req.name}")
  end
end

def main
  tracing_interceptor = GRPC::OpenTracing::ServerInterceptor.new(tracer: OpenTracing.global_tracer)
  s = GRPC::RpcServer.new
  s.add_http2_port('0.0.0.0:50051', :this_port_is_insecure)
  s.handle(tracing_interceptor.intercept(GreeterServer))
  s.run_till_terminated_or_interrupted([1, 'int', 'SIGQUIT'])
end

main
