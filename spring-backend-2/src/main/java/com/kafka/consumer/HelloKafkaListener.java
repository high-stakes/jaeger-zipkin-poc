package com.kafka.consumer;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import com.google.common.annotations.Beta;
import io.grpc.*;
import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@EnableKafka
public class HelloKafkaListener {

    @Autowired
    Tracing tracing;

    @KafkaListener(topics = "hello")
    public void processMessage(@Payload String content, @Headers Map<String, Object> headers) {

        headers.keySet().forEach( it -> {
            System.out.println("Header: " + it + " Value: " + headers.get(it));
        });

        GrpcTracing grpcTracing = GrpcTracing.create(tracing);

        System.out.println("Consumed" + content);

        final ManagedChannel channel = ManagedChannelBuilder.forAddress("ruby-backend", 50051)
                .intercept(grpcTracing.newClientInterceptor())
                .usePlaintext()
                .build();

        GreeterGrpc.GreeterFutureStub stub = GreeterGrpc.newFutureStub(channel);
        stub.sayHello(HelloRequest.newBuilder().setName(content).build());
    }
}
