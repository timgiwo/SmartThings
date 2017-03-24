/**
 *
 *  Description:  	Program intended to change mode to "Home" from "Sleeping"  at a set time every day
 *
 *  Author: Tim Soedarjatno
 */
 
definition(
    name: "Auto Change Mode When...",
    namespace: "Tim S",
    author: "Tim S",
    description: "Play sound when opened or closed",
    category: "Green Living",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home9-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home9-icn@2x.png"
)

preferences {
	section("Change mode to..."){
    	input "newMode", "mode", title: "New Mode?", multiple: false, required: true
        input "executeTime", "time", title: "Enter a time to execute every day"
        }
	section("During these modes..") {
		input "duringMode", "mode", title: "Only During Mode?", multiple: true
        }
    section( "Notifications" ) {
        input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
        input "phone", "phone", title: "Send a Text Message?", required: false
    }
}

def installed()
{
	initialize()
}

def updated()
{
	unsubscribe()
	initialize()
}

def initialize() {
    schedule(executeTime, handler)
}

// called every day at the time specified by the user
def handler() {
    def currMode = location.mode
    log.debug "checking if $location.mode is one of $duringMode"
    if(currMode in duringMode){
    	log.debug "Changing mode from $location.mode to $newMode"
    	location.setMode(newMode)
        send("Mode changed from $currMode to $newMode")
    }
}

private send(msg) {
		if (sendPushMessage != "No") {
			log.debug("sending push message")
			sendPush(msg)	//first try sending a push notification and hello home notification (included in sendPush command)
		}
		if (phone) {
			log.debug("sending text message")
			sendSms(phone, msg)
		}
	log.debug msg
}