# Geospatial-Processing

## Description
A multi maven module which includes the twitter-processing application, as well as dependencies and a dockerfile for building.

## Build
- mvn clean install
- mvn clean package
- docker build -t spark:latest .
- docker tag spark:latest 362637394601.dkr.ecr.us-east-1.amazonaws.com/spark:latest
- docker push 362637394601.dkr.ecr.us-east-1.amazonaws.com/spark:latest

## Submitting the structured streaming Application
```
spark-submit \
--master k8s://http://127.0.0.1:8001 \
--deploy-mode cluster \
--name will --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.5 \
--class com.github.labs1904.SparkStreamingApp \
--jars /opt/spark/jars/spark-sql-kafka-0-10_2.11-2.4.5.jar,/opt/spark/jars/kafka-clients-2.0.0.jar,opt/spark/jars/shadow-java-aws-sdk-1.0-SNAPSHOT.jar --conf spark.executor.instances=2 \
--conf spark.kubernetes.container.image=362637394601.dkr.ecr.us-east-1.amazonaws.com/spark:latest \
--conf spark.kubernetes.container.image.pullPolicy=Always --driver-cores 200m --conf spark.executor.memory=800m --conf spark.kubernetes.executor.request.cores=0.3 --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark --conf spark.kubernetes.driver.secretKeyRef.SECRET_KEY=will-credentials:SECRET_KEY  --conf spark.kubernetes.driver.secretKeyRef.ACCESS_KEY=will-credentials:ACCESS_KEY --conf spark.kubernetes.driver.secretKeyRef.BOOTSTRAP_SERVER=will-credentials:BOOTSTRAP_SERVER --conf spark.kubernetes.executor.secretKeyRef.SECRET_KEY=will-credentials:SECRET_KEY  --conf spark.kubernetes.executor.secretKeyRef.ACCESS_KEY=will-credentials:ACCESS_KEY --conf spark.kubernetes.executor.secretKeyRef.BOOTSTRAP_SERVER=will-credentials:BOOTSTRAP_SERVER \
local:///opt/spark/examples/jars/twitter-processing-1.0-SNAPSHOT.jar
```