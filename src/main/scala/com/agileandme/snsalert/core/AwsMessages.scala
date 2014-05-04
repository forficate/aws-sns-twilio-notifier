package com.agileandme.snsalert.core

import spray.httpx.unmarshalling.Unmarshaller
import spray.http.HttpEntity
import spray.httpx.unmarshalling.MalformedContent
import scala.util.Try
import spray.json._
import DefaultJsonProtocol._
import spray.httpx.marshalling.Marshaller
import spray.http.ContentTypes

/**
 * Model AWS messages sent via SNS and deal with Spray's marshalling/unmarshalling of these.
 * See AWS docs at http://docs.aws.amazon.com/sns/latest/dg/SendMessageToHttp.html for details
 */
object AwsMessages {
  
  //When a SNS HTTP endpoint is created AWS sends a message with a url to call back to verify. This acts as a msg wrapper
  case class AwsSnsSubscriptionConfirmationMsg(topicArn: String, subscribeURL: String)
  
  //Msg wrapper for standard AWS SNS Notifications
  case class AwsSnsNotification(topicArn: String, subject: String, message: String)

  implicit val awsSnsSubscriptionConfirmationMsgUM: Unmarshaller[AwsSnsSubscriptionConfirmationMsg] = simpleJsonUM { data =>
    for {
      JsString(messageType) <- data.get("Type") if messageType == "SubscriptionConfirmation"
      JsString(arn) <- data.get("TopicArn")
      JsString(url) <- data.get("SubscribeURL")
    } yield AwsSnsSubscriptionConfirmationMsg(arn, url)
  }

  implicit val awsSnsNotificationUM: Unmarshaller[AwsSnsNotification] = simpleJsonUM { data =>
    for {
      JsString(messageType) <- data.get("Type") if messageType == "Notification"
      JsString(arn) <- data.get("TopicArn")
      JsString(subject) <- data.get("Subject")
      JsString(message) <- data.get("Message")
    } yield AwsSnsNotification(arn, subject, message)
  }
  

  //Renders plain text for use by Twilio text to speach SIP service
  implicit val awsSnsNotificationTwilioMS: Marshaller[AwsSnsNotification] =
    Marshaller.delegate[AwsSnsNotification, String](ContentTypes.`text/plain`) { msg =>
        "Amazon cloud watch error notification triggered. \n" +
        "Subject: " + msg.subject + '\n' +
        "Message: " + msg.message + '\n'
    }
  
  
  /**
   * AWS Request JSON unmarshaller template function.
   * AWS sends down "Content-Type: text/plain" header and we also need to decide to bind on
   * the "Type" field so this makes it a little more generic for unmarshalling JSON than
   * the built in Spray methods...from what I can see.
   */
  private[this] def simpleJsonUM[A](f: Map[String, JsValue] => Option[A]) : Unmarshaller[A] = new Unmarshaller[A] {
    def apply(entity: HttpEntity) = {
      val json = Try(entity.asString.parseJson.asJsObject).toOption
      val transformedJson = json.map(_.fields).flatMap(f)
      transformedJson toRight MalformedContent("Invalid json")
    }
  }


}