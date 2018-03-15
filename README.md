# Scala Matsuri 2018

This repo holds all the code shown at the 2018 Scala Matsuri's talk "Using Functional Programming to improve your code: A working example"

## Getting Started

SBT is needed in order to run the code.

You should put your own Twitter secrets and keys for this to be able to work.
(Check twitter4j.properties and application.conf)

You should first start your Kafka miniCluster

```shell
sbt miniCluster/clean miniCluster/compile miniCluster/run
```

Then start the web server

```shell
sbt webserver/clean webserver/compile webserver/stage

webserver/target/universal/stage/bin/playserver -Dplay.http.secret.key=abcdefghijk
```

And then you can run any of the classes to check that it runs as it should.

If it doesn't, please let me know!

## References and further reading

### [P7h Spark MLlib Twitter Sentiment Analysis](https://github.com/P7h/Spark-MLlib-Twitter-Sentiment-Analysis)

If you are interested in analysing Twitter messages you'll probably want to head here where a comparison of both Stanford Core NLP and Spark MLlib is done.
The datamaps code used in the talk is from this github repository with some small amendments.

### [Stanford CoreNLP](https://github.com/shekhargulati/52-technologies-in-2016/blob/master/03-stanford-corenlp/README.md)

The Stanford CoreNLP code has been taken from Shekhar Gulati's github repository on Stanford CoreNLP. Go check it out! 

### [Bring your own effect](http://slides.com/cb372/bring-your-own-effect#/)

The part of abstraction over monads was inspired by the Habla computing course I did back in 2017 and Chris Birchall's London Scala meetup talk "Bring your own effect".

### [Functional programming and Spark](https://www.iravid.com/posts/fp-and-spark.html)

Itamar Ravid's blog post where you can see some nice advance techniques applied to Spark.

