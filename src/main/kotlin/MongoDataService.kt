
import com.mongodb.MongoClient
import com.mongodb.util.JSONParseException
import org.bson.BsonDocument
import org.bson.BsonObjectId
import org.bson.Document
import org.bson.types.ObjectId

class MongoDataService( mongoClient : MongoClient, database: String) {

    private val database= mongoClient.getDatabase(database)

    fun allfromCollection(collection: String): ArrayList<Map<String,Any>> {

        val mongoResult= database.getCollection(collection,Document::class.java)
        val result= ArrayList<Map<String, Any>>()

        mongoResult.find().forEach {

            val asMap: Map<String, Any> = mongoDocumentToMap(it)
            result.add(asMap)
        }
        return  result
    }

    fun allCollection() :ArrayList<Map<String,Any>> {

        val mongoResult=   database.listCollections()
        val result:ArrayList<Map<String,Any>> = ArrayList()
        mongoResult.forEach {

            result.add( it.toMap())
        }

        return result
    }

    fun getDocuById(collection: String, id: String?):Map<String, Any>?{

        if(!ObjectId.isValid(id)) return null

        val document= database.getCollection(collection)
                .find(Document("_id",ObjectId(id)))

        if (document.first() != null) return  mongoDocumentToMap((document.first()))

        return null

    }
    fun createNewDocument(collection: String, document:String): String{

        try {

            val bsonDocument= BsonDocument.parse(document)

            bsonDocument.remove("_id")
            val oid= ObjectId()
            bsonDocument["_id"] = BsonObjectId(oid)
            database.getCollection(collection, BsonDocument::class.java).insertOne(bsonDocument)
            return  oid.toHexString()
        }

        catch (ex: JSONParseException){

            return  "Invalid JSON ${ex.localizedMessage}"
        }
    }

    fun createCollection(collName: String){

        database.createCollection(collName)
    }


    private fun mongoDocumentToMap(document: Document): Map<String,Any >{

        val asMap: MutableMap<String, Any> = document.toMutableMap()

        if(asMap.containsKey("_id")){

            val id= asMap.getValue("_id")

            if (id is ObjectId){

                asMap.set("_id", id.toHexString())
            }
        }

        return  asMap
    }
}

