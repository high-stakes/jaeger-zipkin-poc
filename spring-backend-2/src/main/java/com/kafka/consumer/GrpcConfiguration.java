package com.kafka.consumer;

import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import java.util.concurrent.TimeUnit;
import org.lognet.springboot.grpc.GRpcServerBuilderConfigurer;
import org.springframework.stereotype.Component;

@Component
public class GrpcConfiguration extends GRpcServerBuilderConfigurer {
  @Override
  public void configure(ServerBuilder<?> serverBuilder){

    ((NettyServerBuilder)serverBuilder)
        .maxConnectionAge(1L, TimeUnit.SECONDS)
                    .maxConnectionAgeGrace(100L, TimeUnit.SECONDS);



  }
}
