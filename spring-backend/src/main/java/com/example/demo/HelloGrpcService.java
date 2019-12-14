package com.example.demo;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;
import io.grpc.stub.StreamObserver;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
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
    JdbcTemplate jdbcTemplate;

    @Autowired
    KafkaTemplate kafkaTemplate;

  @Autowired
  Tracing tracing;


  @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        logger.info("Hello: " + request.getName());
        //jdbcTemplate.execute("SELECT * FROM customers");
        Random rand = new Random();
    byte[] array = new byte[14]; // length is bounded by 7
    new Random().nextBytes(array);
    String generatedString = new String(array, Charset.forName("UTF-8"));
        kafkaTemplate.send("hellow", generatedString,request.getName());

      GrpcTracing grpcTracing = GrpcTracing.create(tracing);

    responseObserver.onNext(HelloReply.newBuilder().setMessage("jani").build());
    responseObserver.onCompleted();
  }
}
