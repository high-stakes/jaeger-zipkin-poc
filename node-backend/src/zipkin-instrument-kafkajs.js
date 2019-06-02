const { recordConsumeStop, recordConsumeStart, recordProducerStart, recordProducerStop } = require('./kafka-recorder.js');
const { Request } = require('zipkin');

const instrumentKafkaJs = (kafkaJs, {tracer, remoteServiceName}) => {
    const consumerRunHandler = {
       get: function (obj, prop) {
           if (prop === 'eachMessage') {
               return function(params) {
                    let id;
                    let promise;
                    tracer.scoped(() => {
                        id = recordConsumeStart(tracer, params);
                        promise = obj[prop](params);
                    });
                    promise.then(() => {
                        recordConsumeStop(tracer, id);
                    }).catch((error) => {
                        recordConsumeStop(tracer, id, error);
                    });
                    return promise;
               };
           }
           return obj[prop];
       }
    };

    const consumerHandler = {
        get: function (obj, prop) {
            if (prop === 'run') {
                return function(params) {
                    return obj[prop](new Proxy(params, consumerRunHandler));
                };
            }
            return obj[prop];
        }
    };

    const producerHandler = {
        get: function (obj, prop) {
            if (prop === 'send') {
                return function(params) {
                    let id;
                    let promise;
                    tracer.scoped(() => {
                        id = recordProducerStart( tracer, remoteServiceName, { topic: params.topic } );
                        params.messages = params.messages.map( (message) => Request.addZipkinHeaders(message, id));
                        promise = obj[prop](params);
                    });
                    promise.then(() => {
                        recordProducerStop(tracer, id);
                    }).catch((error) => {
                        recordProducerStop(tracer, id, error);
                    });
                    return promise;
                };
            }
            return obj[prop];
        }
    };

    const kafkaHandler = {
        get : function (obj, prop) {
            if (prop === 'consumer') {
                return function(params) {
                    return new Proxy(obj[prop](params), consumerHandler);
                };
            }
            if (prop === 'producer') {
                return function(params) {
                    return new Proxy(obj[prop](params), producerHandler);
                };
            }
            return obj[prop];
        }
    };

    return new Proxy(kafkaJs, kafkaHandler);
};

module.exports = instrumentKafkaJs;