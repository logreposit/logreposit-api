# logreposit-api

| branch | CI build | test coverage |
|--------|:--------:|--------------:|
| master  | [![CircleCI](https://circleci.com/gh/logreposit/logreposit-api/tree/master.svg?style=shield)](https://circleci.com/gh/logreposit/logreposit-api/tree/master)   | [![codecov.io](https://codecov.io/gh/logreposit/logreposit-api/branch/master/graphs/badge.svg)](https://app.codecov.io/gh/logreposit/logreposit-api/branch/master)   |
| develop | [![CircleCI](https://circleci.com/gh/logreposit/logreposit-api/tree/develop.svg?style=shield)](https://circleci.com/gh/logreposit/logreposit-api/tree/develop) | [![codecov.io](https://codecov.io/gh/logreposit/logreposit-api/branch/develop/graphs/badge.svg)](https://app.codecov.io/gh/logreposit/logreposit-api/branch/develop) |

## Service Description

TODO

## Environment Requirements

* MongoDB
* RabbitMQ

## Configuration

This service has to be configured via environment variables. 
MongoDB is used to store the data like users and devices along with their definition and their tokens.
The communication to the [influxdb-service](https://github.com/logreposit/influxdb-service) happens over RabbitMQ.

|Environment Variable Name     | default value |
|------------------------------|---------------|
| SPRING_DATA_MONGODB_HOST     | localhost     |
| SPRING_DATA_MONGODB_PORT     | 27017         |
| SPRING_DATA_MONGODB_DATABASE | logrepositapi |
| SPRING_RABBITMQ_HOST         | localhost     |
| SPRING_RABBITMQ_PORT         | 5672          |
| SPRING_RABBITMQ_USERNAME     | guest         |
| SPRING_RABBITMQ_PASSWORD     | guest         |

## TODO

Description of service, API Documentation and setup along with maintenance instructions
