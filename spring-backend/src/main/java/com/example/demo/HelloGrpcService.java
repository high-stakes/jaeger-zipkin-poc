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
import java.util.ArrayList;
import java.util.List;
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
        //kafkaTemplate.send("hello", request.getName());

      GrpcTracing grpcTracing = GrpcTracing.create(tracing);


    final ManagedChannel channel = ManagedChannelBuilder.forAddress("spring-backend-2", 6566)
          .intercept(grpcTracing.newClientInterceptor())
          .usePlaintext()
          .build();

    GreeterGrpc.newFutureStub(channel).sayHello(HelloRequest.newBuilder().setName("peti").build());
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    List<ListenableFuture<HelloReply>> responses = new ArrayList<>();

    for( int i =0;i< 20 ;i++) {
      responses.add(GreeterGrpc.newFutureStub(channel).sayHello(HelloRequest.newBuilder().setName("jani").build()));
      try {
        Thread.sleep(1L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    for (ListenableFuture<HelloReply> helloReplyListenableFuture : responses) {
      try {
        helloReplyListenableFuture.get();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }

    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    responseObserver.onNext(HelloReply.newBuilder().setMessage("jani").build());
    responseObserver.onCompleted();
  }
}
