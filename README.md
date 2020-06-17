# Geospatial-Processing

## Description
A multi maven module which includes the twitter-processing application, as well as dependencies and a dockerfile for building.

## Submitting the structured streaming Application
```
spark-submit \
--master k8s://http://127.0.0.1:8001 \
--deploy-mode cluster \
--name will --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.4.5 \
--class com.github.labs1904.SparkStreamingApp \
--jars /opt/spark/jars/spark-sql-kafka-0-10_2.11-2.4.5.jar,/opt/spark/jars/kafka-clients-2.0.0.jar,opt/spark/jars/ShadowJavaAwsSdk-1.0-SNAPSHOT.jar --conf spark.executor.instances=2 \
--conf spark.kubernetes.container.image=362637394601.dkr.ecr.us-east-1.amazonaws.com/spark:latest \
--conf spark.kubernetes.container.image.pullPolicy=Always --driver-cores 200m --conf spark.executor.memory=800m --conf spark.kubernetes.executor.request.cores=0.3 --conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
local:///opt/spark/examples/jars/twitter-processing-1.0-SNAPSHOT.jar
```