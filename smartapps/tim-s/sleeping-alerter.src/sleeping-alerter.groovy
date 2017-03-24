/**
 *
 *  Description:  	This program is designed to work with the Aeotec Doorbell and Aeotec Siren 
 *					using the custom Device Type Handlers.
 *
 *					The track #'s of the Aeotec Doorbell are assigned by the order in which they are copied to the doorbell device.  Copy 01-99 in order
 *					Copy 01-99 in order and specify the track number in the settings below
 *
 *					Aeon Labs Multifunction Siren: https://community.smartthings.com/t/release-aeon-labs-multifunction-siren/40652/7
 *					Aeon Labs Aeotec Doorbell: https://community.smartthings.com/t/release-aeon-labs-aeotec-doorbell/39166
 *
 *  Author: Tim Soedarjatno
 */
 
definition(
    name: "Sleeping Alerter...",
    namespace: "Tim S",
    author: "Tim S",
    description: "Play sound when opened or closed",
    category: "Green Living",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home9-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home9-icn@2x.png"
)

preferences {
	section("During these modes..") {
		input "playMode", "mode", title: "Mode?", multiple: true
	}
    section ("Play alert on this speaker (Aeotec Doorbell)...") {
        input "speaker", "capability.musicPlayer", title: "Speaker", required: true
    }
    section ("Siren this alarm (Aeotec Siren)...") {
        input "siren", "capability.musicPlayer", title: "Siren", required: false
        input "sirenTime", "number", title: "Super loud noise for how long (seconds): ", defaultValue: 5, required: true
    }
    section ("When the following contacts (open/close)...") {
        input "openClose", "enum", title: "Open/Closed", options: ["open","closed"], multiple: true, required: true
    }
	section ("Contact Sensor1") {
		input "contactSensor1", "capability.contactSensor", title: "Which Sensor?", multiple: false, required: false
		input "contactSound1", "number", title: "Play sound #:", required: false
        input "contactRepeat1", "number", title: "Repeat # Times: ", defaultValue: 5, required: false
	}
    section ("Contact Sensor2") {
		input "contactSensor2", "capability.contactSensor", title: "Which Sensor?", multiple: false, required: false
		input "contactSound2", "number", title: "Play sound #:", required: false
        input "contactRepeat2", "number", title: "Repeat # Times: ", defaultValue: 5, required: false
	} 
    section ("Contact Sensor3") {
		input "contactSensor3", "capability.contactSensor", title: "Which Sensor?", multiple: false, required: false
		input "contactSound3", "number", title: "Play sound #:", required: false
        input "contactRepeat3", "number", title: "Repeat # Times: ", defaultValue: 5, required: false
	} 
    section ("Contact Sensor4") {
		input "contactSensor4", "capability.contactSensor", title: "Which Sensor?", multiple: false, required: false
		input "contactSound4", "number", title: "Play sound #:", required: false
        input "contactRepeat4", "number", title: "Repeat # Times: ", defaultValue: 5, required: false
			}
    section ("Contact Sensor5") {
		input "contactSensor5", "capability.contactSensor", title: "Which Sensor?", multiple: false, required: false
		input "contactSound5", "number", title: "Play sound #:", required: false
        input "contactRepeat5", "number", title: "Repeat # Times: ", defaultValue: 5, required: false
			}
}

def installed()
{
	initialize()
    subscribe(app, appTouch)
}

def updated()
{
	unsubscribe()
	initialize()
    subscribe(app, appTouch)
}

def initialize()
{
	subscribe(contactSensor1, "contact", contactChanged)
    subscribe(contactSensor2, "contact", contactChanged)
    subscribe(contactSensor3, "contact", contactChanged)
    subscribe(contactSensor4, "contact", contactChanged)
    subscribe(contactSensor5, "contact", contactChanged)
}

def contactChanged(evt) {
	log.debug "$evt.value, $location.mode"
    def currState = []
    currState = evt.value
	if(location.mode in playMode){
    	if(evt.value in openClose){
            switch(evt.displayName){
                case contactSensor1.displayName:
                    log.debug "Playing sound1"
                    speaker.playRepeatTrack(contactSound1, contactRepeat1)
					sirenAlert()
                    break
                case contactSensor2.displayName:
                    speaker.playRepeatTrack(contactSound2, contactRepeat2)
                    sirenAlert()
                    break
                case contactSensor3.displayName:
                    speaker.playRepeatTrack(contactSound3, contactRepeat3)
                    sirenAlert()
                    break
                case contactSensor4.displayName:
                    speaker.playRepeatTrack(contactSound4, contactRepeat4)
                    sirenAlert()
                    break
                case contactSensor5.displayName:
                    speaker.playRepeatTrack(contactSound5, contactRepeat5)
                    sirenAlert()
                    break
         	}
    	}
    }
}
def sirenAlert(){
	siren.siren()
	runIn(sirenTime, "sirenKillSwitch", [overwrite: false])		// Turn off Siren
    runIn(sirenTime + 1, "sirenKillSwitch", [overwrite: false])	// Let's be sure the siren turned off
    runIn(sirenTime + 2, "sirenKillSwitch", [overwrite: false])	// Let's be super paranoid that the siren didn't turn off!
}
def sirenKillSwitch(){
	siren.off()
}