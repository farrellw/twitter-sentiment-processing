# Sentiment Twitter Processing
A Spark 2.4.5, written in Scala and setup using Maven.

# Running the Structured Streaming App

The structured streaming app requires a Kafka cluster. The data on the topic is raw twitter data.

TODO replace below with twitter schema
```json
{
  "marketplace": "US",
  "customer_id": 1,
  "review_id": "R26ZK6XLDT8DDS",
  "product_id": "B000L70MQO",
  "product_parent": 216773674,
  "product_title": "Product 1",
  "product_category": "Toys",
  "star_rating": 5,
  "helpful_votes": 1,
  "total_votes": 4,
  "vine": "N",
  "verified_purchase": "Y",
  "review_headline": "Five Stars",
  "review_body": "Cool.",
  "review_date": "2015-01-12T00:00:00.000-06:00"
}
```

You will need to specify the Kafka bootstrap servers as the first argument.