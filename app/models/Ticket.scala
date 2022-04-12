package models

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import reactivemongo.play.json._
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson._
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._

case class Ticket(
    _id: Option[BSONObjectID],
    _creationDate: Option[DateTime],
    _modifiedOn: Option[DateTime],
    belongsTo: String,
    paid: Boolean,
)

object Ticket {
    implicit val fmt: Format[Ticket] = Json.format[Ticket]
    implicit object TicketBSONReader extends BSONDocumentReader[Ticket] {
        def read(doc: BSONDocument): Ticket = {
            Ticket(
                doc.getAs[BSONObjectID]("_id"),
                doc.getAs[BSONDateTime]("_creationDate").map(dt => new DateTime(dt.value)),
                doc.getAs[BSONDateTime]("_modifiedOn").map(dt => new DateTime(dt.value)),
                doc.getAs[String]("belongsTo").get,
                doc.getAs[Boolean]("paid").get,
            )
        }
    }

    implicit object TicketBSONWriter extends BSONDocumentWriter[Ticket] {
        def write(ticket: Ticket): BSONDocument = {
            BSONDocument(
                "_id" -> ticket._id,
                "_creationDate" -> ticket._creationDate.map(date => BSONDateTime(date.getMillis)),
                "_modifiedOn" -> ticket._modifiedOn.map(date => BSONDateTime(date.getMillis)),
                "belongsTo" -> ticket.belongsTo,
                "paid" -> ticket.paid,
            )
        }
    }
}
