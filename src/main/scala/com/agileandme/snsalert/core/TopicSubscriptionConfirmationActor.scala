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
import spray.http.{HttpEntity}
import scala.concurrent.duration.{ Duration, FiniteDuration }
import spray.client.pipelining._
import scala.concurrent.duration._
import spray.http._
import scala.concurrent.Future
import akka.event.Logging
import spray.httpx.unmarshalling._
import spray.json._
import DefaultJsonProtocol._
import scala.util.Try


class TopicSubscriptionConfirmationActor extends Actor {
  import context.dispatcher
  import AwsMessages._
  
  private[this] val log = Logging(context.system, this)
  
  private[this]val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  
  def receive = {
    case msg:AwsSnsSubscriptionConfirmationMsg => 
      log.info("Recievied topic subscription request. Confirming subscription to " + msg.topicArn)
      pipeline(Get(msg.subscribeURL))
  }
}
