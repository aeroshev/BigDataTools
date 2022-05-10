import random
from dataclasses import dataclass
from datetime import datetime, timedelta
from typing import Iterator

from src.config.base import ENCODING, LATENCY

_start_date = datetime(year=2000, month=1, day=1)
_delta = timedelta(seconds=LATENCY)


@dataclass
class SensorData:
    date: datetime
    area: str
    sensor: str
    value: int

    def __str__(self) -> str:
        return f"{self.date}, {self.area}, {self.sensor}, {self.value};"

    def get_bytes(self) -> bytes:
        return str(self).encode(encoding=ENCODING)


params = ["hum", "temp", "pres"]
areas_list = [f"area_{num}" for num in range(1, 11)]
sensors_list = [f"sensor{random.randint(1, 1000)}_{random.choice(params)}" for _ in range(1, 41)]


def generator_data() -> Iterator[SensorData]:
    _current_time = _start_date
    while True:
        _current_time += _delta
        yield SensorData(
            date=_current_time,
            area=random.choice(areas_list),
            sensor=random.choice(sensors_list),
            value=random.randint(1, 1000)
        )
