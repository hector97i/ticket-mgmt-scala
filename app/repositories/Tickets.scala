package repositories

import models.Ticket
import javax.inject._
import reactivemongo.api.bson.collection.BSONCollection
import play.modules.reactivemongo.ReactiveMongoApi
import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import org.joda.time.DateTime
import reactivemongo.api.commands.WriteResult

@Singleton
class TicketRepository @Inject()(
    implicit executionContext: ExecutionContext,
    reactiveMongoAPi: ReactiveMongoApi,
) {
    def collection: Future[BSONCollection] = reactiveMongoAPi.database.map(db => db.collection("tickets"))

    def findAll(limit: Int = 100): Future[Seq[Ticket]] = {
        collection.flatMap(
            _.find(BSONDocument(), Option.empty[Ticket])
            .cursor[Ticket](ReadPreference.Primary)
            .collect[Seq](limit, Cursor.FailOnError[Seq[Ticket]]())
        )
    }

    def findOne(id: BSONObjectID): Future[Option[Ticket]] = {
        collection.flatMap(_.find(BSONDocument("_id" -> id), Option.empty[Ticket]).one[Ticket])
    }

    def create(ticket: Ticket): Future[WriteResult] = {
        collection.flatMap(_.insert(ordered = false)
        .one(ticket.copy(_creationDate = Some(new DateTime()), _modifiedOn = Some(new DateTime()))))
    }

    def update(id: BSONObjectID, ticket: Ticket):Future[WriteResult] = {
        collection.flatMap(
        _.update(ordered = false).one(BSONDocument("_id" -> id),
            ticket.copy(
            _modifiedOn = Some(new DateTime())))
        )
    }

    def delete(id: BSONObjectID):Future[WriteResult] = {
        collection.flatMap(
        _.delete().one(BSONDocument("_id" -> id), Some(1))
        )
    }
}
