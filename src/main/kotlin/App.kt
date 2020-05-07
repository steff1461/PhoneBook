import com.fasterxml.jackson.databind.SerializationFeature
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.bson.types.ObjectId


private val uri= MongoClientURI(
        "mongodb+srv://william:iss121212@cluster0-aarti.gcp.mongodb.net/test?retryWrites=true&w=majority")
val client= MongoClient(uri)
private val mongoDataService= MongoDataService(client, "smscollector")
fun main(args: Array<String>) {


    val port= System.getenv("PORT")?.toInt() ?:23567
    embeddedServer(Netty, port) {
        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.INDENT_OUTPUT)
            }
        }
        routing {

            get {

                call.respond("test")
            }

            route("/smscollector/buildcoll"){

            post {

                val collName= call.receiveText()
                mongoDataService.createCollection(collName)
                call.respond(HttpStatusCode.Created)
            }
        }


            route("/smscollector/primroute"){

                get{

                    call.respond(mongoDataService.allCollection())
                }

                get("/{collName}") {

                    val collName: String? = call.parameters["collName"]
                    call.respond(mongoDataService.allfromCollection(collName!!))
                }

                post("/{collName}") {

                    val collName: String? = call.parameters["collName"]
                    val docAsString= call.receiveText()
                    val oidError=
                            mongoDataService.createNewDocument(collName!!,docAsString)

                    if(ObjectId.isValid(oidError)) call.respond(HttpStatusCode.Created, oidError)
                    else call.respond(HttpStatusCode.BadRequest, oidError)
                }
            }
        }
    }
        .start(wait = true)
}