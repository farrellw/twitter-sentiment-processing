FROM 362637394601.dkr.ecr.us-east-1.amazonaws.com/spark:latest

COPY target/twitter-processing-1.0-SNAPSHOT.jar /opt/spark/examples/jars/
COPY jars/spark-sql-kafka-0-10_2.11-2.4.5.jar /opt/spark/jars
COPY jars/kafka-clients-2.0.0.jar /opt/spark/jars

COPY jars/hadoop-aws-2.7.3.jar /opt/spark/jars
COPY jars/ShadowJavaAwsSdk-1.0-SNAPSHOT.jar /opt/spark/jars