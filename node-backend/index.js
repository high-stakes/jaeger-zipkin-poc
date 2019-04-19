const {grcpClient, zipkinGrcpInterceptor, zipkinExpressInterceptor, tracer} = require('./config');
const express = require('express');
const expressProxy = require('express-http-proxy');
const util = require('util');
const { Kafka } = require('kafkajs');
const { TraceId, option: {Some, None}, Annotation  } = require('zipkin');

const allowCors = (req, res, next) => {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS');
    res.header('Access-Control-Allow-Headers', ['Origin', 'Accept', 'X-Requested-With',
     'X-B3-TraceId', 'Content-Type', 'X-B3-ParentSpanId', 'X-B3-SpanId', 'X-B3-Sampled'].join(', '));
    next();
};

const zipkinProxy = express();
zipkinProxy.use(allowCors);
zipkinProxy.use(expressProxy('localhost:9411', {
   filter: (req, res) => req.method != 'OPTIONS'
}));
zipkinProxy.options('*', (req,res) => {
    res.sendStatus(200);
});
zipkinProxy.listen(3001);

const app = express();
app.use('/', express.static('public'))
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
        grcpClient.SayHello({
                name: new Date().toString()
            }, {interceptors: [zipkinGrcpInterceptor] },
            (error, resp2) => {
                res.send(resp + " @@@ " + resp2)
            });
    });
});
app.listen(3000);

const kafka = new Kafka({
  clientId: 'my-app',
  brokers: ['localhost:9092']
})

const consumer = kafka.consumer({ groupId: 'test-group' })
consumer.connect()
consumer.subscribe({ topic: 'hello' })
consumer.run({
  eachMessage: async ({ topic, partition, message }) => {

    const id = tracer.join(new TraceId({
      traceId: new Some(message.headers["X-B3-TraceId"].toString()),
      parentId: new Some(message.headers["X-B3-ParentSpanId"].toString()),
      spanId: message.headers["X-B3-SpanId"].toString(),
      sampled: new Some(message.headers["X-B3-Sampled"].toString())
    }));

    tracer.letId(id ,() => {
        tracer.letId(tracer.createChildId(), () => {
            tracer.recordServiceName('node-kafka');
            tracer.recordRpc('consume');

            tracer.recordAnnotation(new Annotation.ServerRecv());
            tracer.recordAnnotation(new Annotation.ServerSend());
        });
    });

    console.log(message.value.toString());
  },
});