package controllers

import javax.inject._
import play.api.mvc._
import repositories.TicketRepository
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.{Json, __}
import scala.util.{Failure, Success}
import scala.concurrent.{ExecutionContext, Future}
import models.Ticket
import play.api.libs.json.JsValue

@Singleton
class TicketsController @Inject()(
    implicit executionContext: ExecutionContext,
    val ticketRepository: TicketRepository,
    val controllerComponents: ControllerComponents
)
    extends BaseController {
        def findAll():Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
            ticketRepository.findAll().map {
            tickets => Ok(Json.toJson(tickets))
        }
    }

    def findOne(id:String):Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
        val objectIdTryResult = BSONObjectID.parse(id)
        objectIdTryResult match {
            case Success(objectId) => ticketRepository.findOne(objectId).map {
                ticket => Ok(Json.toJson(ticket))
            }
            case Failure(_) => Future.successful(BadRequest("Cannot parse the ticket id"))
        }
    }

    def create():Action[JsValue] = Action.async(controllerComponents.parsers.json) { implicit request => {
        request.body.validate[Ticket].fold(
        _ => Future.successful(BadRequest("Cannot parse request body")),
        ticket =>
            ticketRepository.create(ticket).map {
            _ => Created(Json.toJson(ticket))
            }
        )
    }}

    def update(id: String):Action[JsValue]  = Action.async(controllerComponents.parsers.json) { implicit request => {
        request.body.validate[Ticket].fold(
            _ => Future.successful(BadRequest("Cannot parse request body")),
            ticket => {
                val objectIdTryResult = BSONObjectID.parse(id)
                objectIdTryResult match {
                case Success(objectId) => ticketRepository.update(objectId, ticket).map {
                    result => Ok(Json.toJson(result.ok))
                }
                case Failure(_) => Future.successful(BadRequest("Cannot parse the ticket id"))
                }
            }
        )
    }}

    def delete(id: String): Action[AnyContent]  = Action.async { implicit request => {
        val objectIdTryResult = BSONObjectID.parse(id)
        objectIdTryResult match {
            case Success(objectId) => ticketRepository.delete(objectId).map {
                _ => NoContent
            }
            case Failure(_) => Future.successful(BadRequest("Cannot parse the ticket id"))
        }
    }}
}
