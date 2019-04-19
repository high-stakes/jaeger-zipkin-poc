#!/usr/bin/env bash

mkdir -p ./lib
grpc_tools_ruby_protoc -I ../api-protos --ruby_out=./lib --grpc_out=./lib ../api-protos/hello.proto
