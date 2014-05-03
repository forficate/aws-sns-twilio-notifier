/**
 * The MIT License
 *
 * Copyright (c) 2014 Adam Evans
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.agileandme.snsalert.core

import akka.actor.Actor
import akka.event.Logging
import scala.concurrent.duration.{ Duration, FiniteDuration }
import spray.client.pipelining._
import scala.concurrent.duration._
import spray.http._
import scala.concurrent.Future

object TopicAlertProtocol {
  case object GetLastMessage
  case object ClearMessages
}

case class TwilioConf(accountId: String, authToken: String, twilioNumber: String, alertNumber: String, statusUrl: String)


class TopicAlertActor(twilioConf: TwilioConf) extends Actor {
  import context.dispatcher
  import AwsMessages._
  import TopicAlertProtocol._

  private[this] val log = Logging(context.system, this)
    
  private[this] val pipeline: HttpRequest => Future[HttpResponse] =  
    addCredentials(BasicHttpCredentials(twilioConf.accountId, twilioConf.authToken)) ~> 
    sendReceive

  private[this] var messages = List.empty[AwsSnsNotification]

  def receive = {
    case alert: AwsSnsNotification => addAlertToListAndNotify(alert)
    case ClearMessages => messages = List.empty
    case GetLastMessage => sender() ! messages.lastOption
  }

  private[this] def addAlertToListAndNotify(msg: AwsSnsNotification) = {
    log.info(msg.toString)
    messages = messages :+ msg

    pipeline(Post(s"https://api.twilio.com/2010-04-01/Accounts/${twilioConf.accountId}/Calls.json",
      FormData(Map(
        "To" -> twilioConf.alertNumber,
        "From" -> twilioConf.twilioNumber,
        "Url" -> twilioConf.statusUrl,
        "Method" -> "GET",
        "FallbackMethod" -> "GET",
        "StatusCallbackMethod" -> "GET",
        "Record" -> "false"))))    
  }
}

