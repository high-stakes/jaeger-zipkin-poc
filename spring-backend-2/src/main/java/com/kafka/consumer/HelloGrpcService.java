package com.kafka.consumer;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;

@GRpcService
public class HelloGrpcService extends GreeterGrpc.GreeterImplBase {
    Logger logger = LoggerFactory.getLogger(HelloGrpcService.class);

  @Autowired
  Tracing tracing;

  @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {

    try {
      if (request.getName().equals("peti")) {
        Thread.sleep(1000L);
      }
      else {
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    logger.info("Hello : " + request.getName());

    responseObserver.onNext(HelloReply.newBuilder().setMessage("Hello: " + request.getName()).build());
        responseObserver.onCompleted();
    }
}
