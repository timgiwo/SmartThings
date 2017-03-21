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
 *  Bon Voyage
 *
 *  Author: SmartThings
 *  Date: 2013-03-07
 *
 *  Monitors a set of presence detectors and triggers a mode change when everyone has left.
 */

definition(
    name: "Lock Up House on Mode Change",
    namespace: "Mode Based",
    author: "SmartThings",
    description: "Monitors for a mode change and checks all doors and windows",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld@2x.png"
)

preferences {
	section("When changed to these modes..") {
		input "newMode", "mode", title: "Mode?", multiple: true
	}
	section("Check these locks") {
		input "lock1","capability.lock", title: "Which Locks?", multiple: true
        input "autoLock", "enum", title: "Automatically Lock Doors?", options: ["Yes", "No"], required: false, defaultValue: "No"
    }
	section ("Check these windows") {
		input "sensors", "capability.contactSensor", title: "Which Windows?", multiple: true
	}
	section( "Notifications" ) {
			input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
			input "phone", "phone", title: "Send a Text Message?", required: false
	}

}

def installed() {
	log.debug "Installed with settings: ${settings}"
	log.debug "Current mode = ${location.mode}"
	subscribe(location, changedLocationMode)
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	log.debug "Current mode = ${location.mode}"
	unsubscribe()
	subscribe(location, changedLocationMode)
}

def changedLocationMode(evt)
{
	def result = false
    //Check the new mode against the list of modes to match against
	for (monitoredMode in newMode) {
		if (monitoredMode == evt.value) {
            log.debug "New Mode Value: $evt.value == Monitored Mode: $monitoredMode"
        	log.debug "Found a match!"
			result = true
			break
		}
        else {
        	log.debug "New Mode Value: $evt.value != Monitored Mode: $monitoredMode"
            result = false
        }
	}
    
    if (result == true){
    	log.debug "Result = True.  Locking Doors and Checking Windows"
    	lockDoors()
        checkWindows()
    }
}

def lockDoors()
{
    def devLabel
	for (door in lock1) {
    	log.debug "Door: $door Status: $door.currentLock"
		if ("unlocked" == door.currentLock) {
        	devLabel = door.displayName
        	log.debug "$door == $door.currentLock."
			if(autoLock == "Yes"){
            	door.lock()
                send("Auto-locked $devLabel after you left")
            }
            else{
            	send("You left $devLabel unlocked! Turn around!")
            }
		}
	}
}

def checkWindows()
{
    def devLabel
	for (window in sensors) {
    	log.debug "window: $window Status: $window.currentContact"
		if (window.currentContact == "open") 
        {
        	devLabel = window.displayName
        	log.debug "Found an open window! $devLabel"
            send("$devLabel still open! Turn around!")
			result = true
			break
		}
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
