package com.github.labs1904.models

case class Tweet(text: String, id: String, created_at: String, truncated: Option[Boolean], coordinates: Coordinates, place: Place, user: User)
case class User(id: String, name: String, screen_name: String, location: String, url: String, description: String, verified: Option[Boolean], followers_count: Option[Int], friends_count: Option[Int], created_at: String)
case class Coordinates(coordinates: Array[Float], `type`: String)
case class Place(id: String, place_type: String, name: String, full_name: String, bounding_box: BoundingBox)
case class BoundingBox(`type`: String, coordinates: Array[Array[Array[Float]]])
case class TweetWithSentiment(text: String, id: String, created_at: String, truncated: Option[Boolean], coordinates: Coordinates, place: Place, user: User, sentiment: String)
