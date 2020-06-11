package com.github.farrellw.models

case class Tweet(text: String, id: String)
case class EnrichedTweet(text: String, id: String, shorter: String, sent: String)