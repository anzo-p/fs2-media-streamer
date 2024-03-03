FROM amazoncorretto:11.0.17 as builder

WORKDIR /app

RUN yum -y install unzip

COPY target/universal/crates-0.1.zip .

RUN unzip crates-0.1.zip

FROM amazoncorretto:11-alpine

WORKDIR /app

RUN apk add --no-cache bash libstdc++

COPY --from=builder /app/crates-0.1 /crates-0.1

ENTRYPOINT ["/crates-0.1/bin/crates"]
