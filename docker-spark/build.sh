# -- Software Stack Version

SPARK_VERSION="3.0.0"
HADOOP_VERSION="2.7"
JUPYTERLAB_VERSION="2.1.5"

# -- Building the Images

docker build \
  -f cluster-base/Dockerfile \
  -t cluster-base:custom .

docker build \
  --build-arg spark_version="${SPARK_VERSION}" \
  --build-arg hadoop_version="${HADOOP_VERSION}" \
  -f spark-base/Dockerfile \
  -t spark-base:custom .

docker build \
  -f spark-master/Dockerfile \
  -t spark-master:staging .

docker build \
  -f spark-worker/Dockerfile \
  -t spark-worker:staging .

docker build \
  --build-arg spark_version="${SPARK_VERSION}" \
  --build-arg jupyterlab_version="${JUPYTERLAB_VERSION}" \
  -f jupyterlab/Dockerfile \
  -t jupyterlab:staging .
