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

import akka.testkit.TestActorRef
import com.agileandme.snsalert.core.TopicSubscriptionConfirmationActor
import org.specs2.mutable.Specification
import scala.util.parsing.json.JSON
import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes
import spray.testkit.Specs2RouteTest
import com.agileandme.snsalert.core.TopicAlertActor
import akka.testkit.TestProbe
import com.agileandme.snsalert.core.AwsMessages._
import org.specs2.time.NoTimeConversions
import scala.concurrent.duration._
import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.specs2.mutable.SpecificationLike
import org.specs2.specification._


class SNSNotificationListenerServiceSpec extends SpecificationLike with Specs2RouteTest  {
  
  sequential
    
  val subscriptionConfirmationActor = TestProbe()
  val topicAlertActor = TestProbe()
  lazy val routing = new SNSNotificationListenerService(subscriptionConfirmationActor.ref, topicAlertActor.ref).route

  "JSON routes" should {
    "support AWS subscription messages to /topiclistener" in {
      val subscriptionMsg = """
        |{
		|"Type" : "SubscriptionConfirmation",
		|"MessageId" : "165545c9-2a5c-472c-8df2-7ff2be2b3b1b",
		|"Token" : "2336412f37fb687f5d51e6e241d09c805a5a57b30d712f794cc5f6a988666d92768dd60a747ba6f3beb71854e285d6ad02428b09ceece29417f1f02d609c582afbacc99c583a916b9981dd2728f4ae6fdb82efd087cc3b7849e05798d2d2785c03b0879594eeac82c01f235d0e717736",
		|"TopicArn" : "arn:aws:sns:us-east-1:123456789012:MyTopic",
		|"Message" : "You have chosen to subscribe to the topic arn:aws:sns:us-east-1:123456789012:MyTopic.\nTo confirm the subscription, visit the SubscribeURL included in this message.",
		|"SubscribeURL" : "https://sns.us-east-1.amazonaws.com/?Action=ConfirmSubscription&TopicArn=arn:aws:sns:us-east-1:123456789012:MyTopic&Token=2336412f37fb687f5d51e6e241d09c805a5a57b30d712f794cc5f6a988666d92768dd60a747ba6f3beb71854e285d6ad02428b09ceece29417f1f02d609c582afbacc99c583a916b9981dd2728f4ae6fdb82efd087cc3b7849e05798d2d2785c03b0879594eeac82c01f235d0e717736",
		|"Timestamp" : "2012-04-26T20:45:04.751Z",
		|"SignatureVersion" : "1",
		|"Signature" : "EXAMPLEpH+DcEwjAPg8O9mY8dReBSwksfg2S7WKQcikcNKWLQjwu6A4VbeS0QHVCkhRS7fUQvi2egU3N858fiTDN6bkkOxYDVrY0Ad8L10Hs3zH81mtnPk5uvvolIC1CXGu43obcgFxeL3khZl8IKvO61GWB6jI9b5+gLPoBc1Q=",
		|"SigningCertURL" : "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-f3ecfb7224c7233fe7bb5f59f96de52f.pem"
		|}
        """.stripMargin
        
      Post("/topiclistener", subscriptionMsg) ~> routing ~> check {
        response.status mustEqual StatusCodes.OK
        subscriptionConfirmationActor.expectMsgType[AwsSnsSubscriptionConfirmationMsg]
        success
      }
    }
    
    
    
    "support AWS notification messages to /topiclistener" in {
      val notificationMsg = """
        |{
        |"Type" : "Notification",
        |"MessageId" : "da41e39f-ea4d-435a-b922-c6aae3915ebe",
        |"TopicArn" : "arn:aws:sns:us-east-1:123456789012:MyTopic",
        |"Subject" : "test",
        |"Message" : "test message",
        |"Timestamp" : "2012-04-25T21:49:25.719Z",
        |"SignatureVersion" : "1",
        |"Signature" : "EXAMPLElDMXvB8r9R83tGoNn0ecwd5UjllzsvSvbItzfaMpN2nk5HVSw7XnOn/49IkxDKz8YrlH2qJXj2iZB0Zo2O71c4qQk1fMUDi3LGpij7RCW7AW9vYYsSqIKRnFS94ilu7NFhUzLiieYr4BKHpdTmdD6c0esKEYBpabxDSc=",
        |"SigningCertURL" : "https://sns.us-east-1.amazonaws.com/SimpleNotificationService-f3ecfb7224c7233fe7bb5f59f96de52f.pem",
        |"UnsubscribeURL" : "https://sns.us-east-1.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:us-east-1:123456789012:MyTopic:2bcfbf39-05c3-41de-beaa-fcfcc21c8f55"
        |} 
        """.stripMargin

      Post("/topiclistener", notificationMsg) ~> routing ~> check {
        response.status mustEqual StatusCodes.OK
        topicAlertActor.expectMsgType[AwsSnsNotification]
        success
      }
    }

  }


}
