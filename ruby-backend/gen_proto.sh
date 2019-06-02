#!/usr/bin/env bash

mkdir -p ./lib
grpc_tools_ruby_protoc -I ../proto --ruby_out=./lib --grpc_out=./lib ../proto/hello.proto
