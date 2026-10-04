FROM eclipse-temurin:17-jdk AS builder

RUN apt-get update && apt-get install -y \
    wget ant groovy curl sed jq \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /opt/build/javadoc
COPY resources/ resources/
COPY scripts/ scripts/
COPY src/ src/

ARG LTS_RELEASES=""
ARG PLUGINS=""

RUN bash -ex scripts/generate-javadoc.sh \
 && bash -ex scripts/generate-shortnames.sh \
 && bash -ex scripts/default-to-latest.sh

# For testing of particular steps
#RUN groovy -cp src/main/groovy scripts/generate-javadoc-components.groovy
#RUN cp -R /opt/build/javadoc/build/site/* ${PUBLISH_PATH}

FROM nginx:1.24.0

ARG PUBLISH_PATH=/usr/share/nginx/html

COPY --from=builder /opt/build/javadoc/build/site/ ${PUBLISH_PATH}/

WORKDIR ${PUBLISH_PATH}/
