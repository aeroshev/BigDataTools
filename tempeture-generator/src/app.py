from flask import Flask, Response
from flask_apscheduler import APScheduler
from kafka import KafkaProducer
from kafka.producer.future import FutureRecordMetadata

from config.base import KAFKA_ADDRESS, LATENCY, Config
from utils.generator import SensorData, generator_data

app = Flask(__name__)
app.config.from_object(Config())

scheduler = APScheduler()
scheduler.init_app(app)
scheduler.start()

producer = KafkaProducer(bootstrap_servers=KAFKA_ADDRESS)

generator = generator_data()


@app.route('/health')
def health() -> Response:
    return Response(response='OK', status=200)


@scheduler.task('interval', id='kafka-producer', seconds=LATENCY)
def send_to_kafka() -> None:
    data = next(generator)  # type: SensorData
    app.logger.info(f"Task send to kafka - {data}")
    future = producer.send(topic=data.area, value=data.get_bytes())  # type: FutureRecordMetadata
    record_metadata = future.get(timeout=10)
    app.logger.info(f"Future meta - {record_metadata}")


if __name__ == '__main__':
    app.run(debug=True)
