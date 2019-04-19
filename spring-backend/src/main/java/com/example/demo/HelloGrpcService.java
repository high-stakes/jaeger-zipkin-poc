package com.example.demo;

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
    JdbcTemplate jdbcTemplate;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        logger.info("Hello: " + request.getName());
        jdbcTemplate.execute("SELECT * FROM customers");
        kafkaTemplate.send("hello", request.getName());

        responseObserver.onNext(HelloReply.newBuilder().setMessage("Hello: " + request.getName()).build());
        responseObserver.onCompleted();
    }
}
