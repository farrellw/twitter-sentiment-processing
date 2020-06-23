package com.github.labs1904

import java.util

import org.geotools.data.{DataStoreFinder, DataUtilities}
import org.locationtech.geomesa.fs.data.FileSystemDataStore
import org.locationtech.geomesa.fs.storage.common.interop.ConfigurationUtils
import org.opengis.feature.simple.SimpleFeatureType

import java.util.Collections

object DataStoreGenerator {
    def dsParams(): util.HashMap[String, String] = {
        val parameters = new util.HashMap[String, String]
        parameters.put("fs.path", "s3a://geospatial-project-data/will-spark-dump/datastore")
        parameters.put("fs.encoding", "parquet")
        parameters.put("fs.config.paths", "/Users/wFarrell/dev/geospatial-processing/data-store-generator/src/main/resources/additional-properties.xml")
        parameters
    }

    def getOrCreateDs(parameters: util.HashMap[String, String]): FileSystemDataStore = {
        val dataStore = DataStoreFinder.getDataStore(parameters).asInstanceOf[FileSystemDataStore]
        val sft = createFeatureType()
        ConfigurationUtils.setScheme(sft, "daily", Collections.singletonMap("dtg-attribute", "dtg"))
        dataStore.createSchema(sft)
        dataStore
    }

    def createFeatureType(): SimpleFeatureType = {
        DataUtilities.createType("twitter-sentiment", "geom:Point:srid=4326," + "text:String," + "user:String," + "place:String," + "tweet_id:String," + "dtg:Date," + "sentiment:String")
    }
    def main(args: Array[String]): Unit = {
        val p = dsParams()
        val ds = getOrCreateDs(p)
        println("Success")
    }
}
