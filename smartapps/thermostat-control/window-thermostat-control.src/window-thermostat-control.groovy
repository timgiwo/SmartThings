/**
 *
 *  Description:  	This app turns off the thermostat if any window is open.
 *					When all the windows are closed, the thermostat will be set to "auto"
 *					note: written for CT100 Thermostat that supports auto.  This is easily configured to on or off
 *
 *  Author: Tim Soedarjatno
 */
 
definition(
    name: "Window Thermostat Control",
    namespace: "Thermostat Control",
    author: "Tim S",
    description: "Turn on/off thermostat if windows are open/closed.",
    category: "Green Living",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home9-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home9-icn@2x.png"
)

preferences {
	section ("Windows to monitor...") {
		input "sensors", "capability.contactSensor", title: "Which Windows?", multiple: true
	}
	section ("Turn on/off this thermostat") {
		input "thermostat", "capability.thermostat", title: "Which Thermostat?"
	}
	section( "Notifications" ) {
		input("recipients", "contact", title: "Send notifications to", required: false) {
			input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
			input "phone", "phone", title: "Send a Text Message?", required: false
		}
	}    
}

def installed()
{
	subscribe(sensors, "contact", contactChanged)
    subscribe(app, appTouch)
}

def updated()
{
	unsubscribe()
	subscribe(sensors, "contact", contactChanged)
    subscribe(app, appTouch)
}

def contactChanged(evt) {
	def windowsClosed = checkWindowsClosed()
	if (windowsClosed == true){
    	thermostat.setThermostatMode("auto")
        send("All windows closed. Turning on thermostat")
    }
    else if(windowsClosed == false){
    	thermostat.setThermostatMode("off")
        send("Window opened. Turning off thermostat")
    }

}

// Function to check the status of all the selected sensors
def checkWindowsClosed(){
	def result = true
	for (window in sensors) {
    	log.debug "window: $window Status: $window.currentContact"
		if (window.currentContact == "open") {
        	log.debug "Found an open window!"
			result = false
			break
		}
	}
	log.debug "checkWindowsClosed Results: $result"
	return result
}

// Check all the sensors manually when the button is clicked
def appTouch(evt) {
	log.debug "appTouch: $evt"
	contactChanged()
}

// Send a message or notification
private send(msg) {
	if (location.contactBookEnabled) {
        log.debug("sending notifications to: ${recipients?.size()}")
		sendNotificationToContacts(msg, recipients)
	}
	else  {
		if (sendPushMessage != "No") {
			log.debug("sending push message: $msg")
			sendPush(msg)
		}

		if (phone) {
			log.debug("sending text message: $msg")
			sendSms(phone, msg)
		}
	}
	log.debug msg
}