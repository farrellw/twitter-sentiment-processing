# Sentiment Twitter Processing
A Spark 2.4.5, written in Scala and setup using Maven.

## Running the Structured Streaming App
The structured streaming app requires a Kafka cluster. The data on the topic is raw twitter data.

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
## Example data structure

```json
{
  "created_at":"Wed Jun 10 15:59:26 +0000 2020",
  "id":1270747409170268161,
  "id_str":"1270747409170268161",
  "text":"@StateDept @realDonaldTrump Does the executive order make it legal to tear gas peaceful protesters to have a photo op at any church?",
  "user":
  {
    "id":790650278,
    "id_str":"790650278",
    "name":"Amaechi Nwakuche",
    "screen_name":"AmaechiNwakuche",
    "location":"Raleigh, NC",
    "url":"http:\/\/www.afri-logistics-llc.com",
    "description":"A Chartered Engineer and a certified Project Manager of the Association of Project Managers in the UK. He has over Seventeen and Half years of engineering exp.",
  },
  "geo":
    {
      "type":"Point",
      "coordinates": [41.5,-100.0]
    },
  "coordinates":
    {
      "type":"Point",
      "coordinates":[-100.0,41.5]
    },
  "place":
    {
      "id":"161d2f18e3a0445a",
      "url":"https:\/\/api.twitter.com\/1.1\/geo\/id\/161d2f18e3a0445a.json",
      "place_type":"city",
      "name":"Raleigh",
      "full_name":"Raleigh, NC",
      "country_code":"US",
      "country":"United States",
      "bounding_box":
        {
          "type":"Polygon",
           "coordinates":
           [
             [
               [-78.818343,35.715805],
               [-78.818343,35.972158],
               [-78.497331,35.972158],
               [-78.497331,35.715805]
             ]
           ]
        },
      "attributes":{}
    },
  "lang":"en",
  "timestamp_ms":"1591804766348"
}
```

You will need to specify the Kafka bootstrap servers as the first argument.