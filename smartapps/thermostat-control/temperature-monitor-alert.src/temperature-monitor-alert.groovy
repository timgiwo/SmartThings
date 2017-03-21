/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Description: Monitor the temperature and alert if any of the thresholds are exceeded
 *
 *  Author: SmartThings
 */

definition(
    name: "Temperature Monitor Alert",
    namespace: "Thermostat Control",
    author: "Tim S",
    description: "Monitor and alert on temperature thresholds.",
    category: "Green Living",
    iconUrl: "http://cdn.device-icons.smartthings.com/Seasonal%20Winter/seasonal-winter-006-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Seasonal%20Winter/seasonal-winter-006-icn@2x.png"
)

preferences {
	section("Monitor the temperature..."){
        input "temperatureSensor1", "capability.temperatureMeasurement"
	}
	section("When the temperature drops below...") {
		input "lowThreshold", "decimal", title: "Temperature?", range: "*..*"
	}
    section("When the temperature rises above...") {
		input "highThreshold", "decimal", title: "Temperature?", range: "*..*"
	}
     section("Notification interval..") {
		input "interval", "number", title: "Interval?"
	}   
    section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phone1", "phone", title: "Send a Text Message?", required: false
        }
    }
}

def installed() {
    subscribe(temperatureSensor1, "temperature", temperatureHandler)
}

def updated() {
	unsubscribe()
    subscribe(temperatureSensor1, "temperature", temperatureHandler)
}

def temperatureHandler(evt) {
	log.trace "temperature: $evt.value, $evt"

	def tooCold = lowThreshold
    def tooHot = highThreshold

	// TODO: Replace event checks with internal state (the most reliable way to know if an SMS has been sent recently or not).
	if (evt.doubleValue <= tooCold || evt.doubleValue >= tooHot) {
		log.debug "Checking how long the temperature sensor has been reporting <= $tooCold"

		// Don't send a continuous stream of text messages
		def deltaMinutes = interval // TODO: Ask for "retry interval" in prefs?
		def timeAgo = new Date(now() - (1000 * 60 * deltaMinutes).toLong())
		def recentEvents = temperatureSensor1.eventsSince(timeAgo)?.findAll { it.name == "temperature" }
		log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaMinutes minutes"
		def alreadySentSms = recentEvents.count { it.doubleValue <= tooCold } > 1

		if (alreadySentSms) {
			log.debug "Notification already sent to $phone1 within the last $deltaMinutes minutes"
			// TODO: Send "Temperature back to normal" SMS, turn switch off
		} else {
			log.debug "Temperature exceeds threshold (Low: $tooCold | High: $tooHot):  sending SMS to $phone1 and activating $mySwitch"
			send("${temperatureSensor1.displayName} temperature exceeds threshold, reporting a temperature of ${evt.value}${evt.unit?:"F"}")
			switch1?.on()
		}
	}
}

private send(msg) {
    if (location.contactBookEnabled) {
        log.debug("sending notifications to: ${recipients?.size()}")
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage != "No") {
            log.debug("sending push message")
            sendPush(msg)
        }

        if (phone1) {
            log.debug("sending text message")
            sendSms(phone1, msg)
        }
    }

    log.debug msg
}