FROM cluster-base:custom

# -- Layer: JupyterLab

ARG spark_version=3.0.0
ARG jupyterlab_version=2.1.5

RUN apt-get update -y && \
    apt-get install -y python3-pip && \
    pip3 install wget pyspark==${spark_version} jupyterlab==${jupyterlab_version}

RUN apt-get update -y && \
    apt-get install curl -y && \
    curl -fLo cs https://git.io/coursier-cli-"$(uname | tr LD ld)" && \
    chmod +x cs && \
    ./cs launch --fork almond:0.10.9 --scala 2.12 -- --install

# -- Runtime

EXPOSE 8888
WORKDIR ${SHARED_WORKSPACE}
CMD jupyter lab --ip=0.0.0.0 --port=8888 --no-browser --allow-root --NotebookApp.token=
