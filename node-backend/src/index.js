const {grcpClient, zipkinGrcpInterceptor, zipkinExpressInterceptor, tracer} = require('./config');
const express = require('express');
const expressProxy = require('express-http-proxy');
const util = require('util');
const { Kafka } = require('kafkajs');
const instrumentKafkaJs = require('./zipkin-instrument-kafkajs');

const allowCors = (req, res, next) => {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS');
    res.header('Access-Control-Allow-Headers', ['Origin', 'Accept', 'X-Requested-With',
     'X-B3-TraceId', 'Content-Type', 'X-B3-ParentSpanId', 'X-B3-SpanId', 'X-B3-Sampled'].join(', '));
    next();
};

const app = express();
app.use('/', express.static(__dirname + '/public'))
app.use(allowCors);
app.use(function (req, res, next) {
    if (req.method != 'OPTIONS') {
        zipkinExpressInterceptor(req, res, next);
    } else {
        next();
    }
});
app.get('/api/hello', (req, res) => {
      grcpClient.SayHello({
          name: new Date().toString()
      }, {interceptors: [zipkinGrcpInterceptor] },
      (error, resp) => {
          res.send(":" + resp + " @@@ ")
      });
});
app.listen(3000);

const kafka = instrumentKafkaJs(new Kafka({
  clientId: 'my-app',
  brokers: ['kafkahost:9092']
}), {tracer, remoteServiceName : 'node-backend'});

const producer = kafka.producer();
producer.connect();

const consumer = kafka.consumer({ groupId: 'test-group' })
consumer.connect();
consumer.subscribe({ topic: 'hello' })
consumer.run({
  eachMessage: async ({ topic, partition, message }) => {
    console.log(topic + ": " + message.value.toString() + ": " + Object.values(message.headers).flatMap( (it) => it.toString() ));
  }
});