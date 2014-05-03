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
package com.agileandme.snsalert.boot

import spray.routing.SimpleRoutingApp
import com.agileandme.snsalert.core.{CoreActors}
import com.agileandme.snsalert.api.Api
import com.agileandme.snsalert.core.Config
import com.agileandme.snsalert.core.TwilioConf


object Main extends App with SimpleRoutingApp with BootedCore with CoreActors with Api with Config {
  implicit def executionContext = actorRefFactory.dispatcher
  
  lazy val twilioConfig = {
    val conf = system.settings.config.getConfig("twilio")

    TwilioConf(
      accountId = conf.getString("accountId"),
      authToken = conf.getString("authTokern"),
      twilioNumber = conf.getString("twilioNumber"),
      alertNumber = conf.getString("alertNumber"),
      statusUrl = conf.getString("statusUrl")
    )
  }
  
  startServer(interface = "localhost", port = 8080)(routes)
}
