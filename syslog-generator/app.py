import random
import os
from pathlib import Path
from datetime import datetime, timedelta

from faker import Faker

COUNT_LINES = 10_000
COUNT_FILES = 10

PROJECT_PATH = Path(__file__).parent
OUTPUT_DIR = (PROJECT_PATH / "logs").resolve()
if not os.path.exists(OUTPUT_DIR):
    os.mkdir(OUTPUT_DIR)


fake = Faker()
devices = ["MacBook", "Ubuntu-18.04", "AstraLinux", "Windows 10", "Android", "iPhone"]


class GeneratorLogs:
    """Generate data logs"""

    log_format = "{timestamp} {device}: {facility}-{level}-{number}: {message}\n"
    filename = "output_{num}.log"

    @classmethod
    def run(cls) -> None:
        for file_num in range(COUNT_FILES):
            filename_ = cls.filename.format(num=file_num)
            abs_path = (OUTPUT_DIR / filename_).resolve()
            with open(abs_path, "w") as log:
                now = datetime.now()
                delta = timedelta(hours=1)
                for i in range(COUNT_LINES):
                    now += delta
                    event = random.randint(0, 10)
                    if event < 2:
                        log.write("Error while writing in log\n")
                    else:
                        log.write(cls.log_format.format(
                            timestamp=now.strftime("%Y-%m-%d %H:%M:%S"),
                            device=random.choice(devices),
                            facility=random.randrange(24),
                            level=random.randrange(8),
                            number=i,
                            message=fake.sentence(nb_words=10)
                        ))


if __name__ == "__main__":
    generator = GeneratorLogs()
    generator.run()
