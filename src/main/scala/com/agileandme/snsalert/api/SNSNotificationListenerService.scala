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
package com.agileandme.snsalert.api

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import com.agileandme.snsalert.core.AwsMessages._
import com.agileandme.snsalert.core.TopicAlertProtocol._

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import spray.http.StatusCodes
import spray.routing.Directives

class SNSNotificationListenerService(subscriptionConfirmationActor: ActorRef, topicAlertActor: ActorRef)(implicit val execctutionContext: ExecutionContext) extends Directives {

  implicit val timeout = Timeout(5 second)

  val route = path("topiclistener") {
    post {
      entity(as[AwsSnsSubscriptionConfirmationMsg]) { msg =>
        complete {
          subscriptionConfirmationActor ! msg
          StatusCodes.OK
        }
      }
    } ~ post {
      entity(as[AwsSnsNotification]) { msg =>
        complete {
          topicAlertActor ! msg
          StatusCodes.OK
        }
      }
    }
  } ~ get {
    complete {
      (topicAlertActor ? GetLastMessage).mapTo[Option[AwsSnsNotification]]
    }
  } ~ delete {
    complete {
      topicAlertActor ! ClearMessages
      StatusCodes.OK
    }
  }

}
