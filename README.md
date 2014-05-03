AWS SNS messages to Twilio text to speech VOIP service
======================================================

This application is a small micro service to act as a AWS SNS HTTP endpoint and then use http://www.twilio.com text to speech service to forward on the message to a phone number.

The main use case behind this is to aid in out of hours support when using AWS services. Any SNS topic configured to use this app as an endpoint will initialise a phonecall with the details. This is useful (or not) if you have an on call phone, it's 3 am in the morning and your app has died. The phone ringing out should hopefully be enough to wake you up.

This app is mostly proof of concept experimenting with http://spray.io/ and akka for creating micro services. In production you probably *want to* verify the *TopicArn* incoming requests are made from against a white list.

## Getting started

You will need to sign up to http://www.twilio.com .

When you have a user account you need to edit the *src/main/resources/application.conf* file and insert your account details.

    twilio {
      accountId = ""
      authTokern = ""
      twilioNumber = ""
      alertNumber = ""
      statusUrl = ""
    }

accountId and authTokern are self explanatory, these should be listed on your account details page. twilioNumber is the number Twilio provided you and is the phone number used to make calls from, alertNumber is the number to phone when a message comes in. This needs to be the number you registered with on Twilio unless you have payed for a full account which removes the restrictions. statusUrl is http location Twilio will call to get the text to convert in to speech, this url should point to this app eg: http://www.example.com/topiclistener .

On the AWS side you need to confiure your SNS topics to publish to the /topiclistener endpoint on this app, eg the endpoint should be set to http://www.example.com/topiclistener if this app is hosted on example.com .