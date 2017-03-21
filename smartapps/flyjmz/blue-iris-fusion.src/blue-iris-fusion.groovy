/**
 *  Blue Iris Fusion  (parent app, child app is Blue Iris Camera Triggers - Trigger)
 *
 *  Created by FLYJMZ (flyjmz230@gmail.com)
 *
 *  Smartthings Community Thread: https://community.smartthings.com/t/release-blue-iris-fusion-integrate-smartthings-and-blue-iris/54226
 *  Github: https://github.com/flyjmz/jmzSmartThings/tree/master/smartapps/flyjmz/blue-iris-fusion.src
 *
 *  CHILD APP CAN BE FOUND ON GITHUB: https://github.com/flyjmz/jmzSmartThings/tree/master/smartapps/flyjmz/blue-iris-fusion-trigger.src
 *
 *  Based on work by:
 *  Tony Gutierrez in "Blue Iris Profile Integration"
 *  jpark40 at https://community.smartthings.com/t/blue-iris-profile-trigger/17522/76
 *  luma at https://community.smartthings.com/t/blue-iris-camera-trigger-from-smart-things/25147/9
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
 *
 *  Version 1.0 - 30July2016    Initial release
 *  Version 1.1 - 3August2016   Cleaned up Code
 *  Version 2.0 - 16Oct2016     Added Profile integration.  Also set up option for local connections, but doesn't work.  Standby for updates to make it work.
 *  Version 2.1 - 14Dec2016     Got local connection to work!  If you have issues, try external.  External is very stable.
 *  Version 2.2 - 2Jan2017		Found out the local connection issue, "Local Only" setting in Blue Iris Webserver Settings cannot be checked.
 *  Version 2.3 - 17Jan2017     Added preference to turn debug logging on or off.
 *  Version 2.4 - 22Jan2017     Fixed error in profile change notifications (they all said temporary even if it was a hold change)
 *  Version 2.5 - 23Jan2017     Slight tweak to notifications.
 *
 *  TODO:
 *      -Create failover (i.e. let user set up both local and external connections so you don't have to retype if you just want to switch, and also to let it try one, if it doesn't work, try the other - but have a switch to turn this option on/off)
 *      -Add localAction error checking and notifications so the user knows if the profile and/or trigger actually did change (right now it only checks for success and notifies if user is using external.  So is there any way to check via a local connection if it did change the profile to alert user?)
 */

definition(
    name: "Blue Iris Fusion",
    namespace: "flyjmz",
    author: "flyjmz230@gmail.com",
    description: "Full Smartthings mode integration with Blue Iris profiles, plus Smartthings can use motion or contact sensors, or switches to trigger Blue Iris camera recording.",
    category: "Safety & Security",
    iconUrl: "https://raw.githubusercontent.com/flyjmz/jmzSmartThings/master/resources/BlueIris_logo.png",
    iconX2Url: "https://raw.githubusercontent.com/flyjmz/jmzSmartThings/master/resources/BlueIris_logo%402x.png",
    singleInstance: true
)

preferences {
    page(name:"BITriggers")
}

def BITriggers() {
    dynamicPage(name:"BITriggers", title: "Triggers", install: true, uninstall: true, submitOnChange: true) {
        section("") {
            app(name: "Blue Iris Fusion - Trigger", appName: "Blue Iris Fusion - Trigger", namespace: "flyjmz", title: "Add Camera Trigger", multiple: true)
        }
        section("Blue Iris Server Login Settings") {
            paragraph "Local or External Connection to Blue Iris Server (i.e. LAN vs WAN)?"
            input "localOnly", "bool", title: "Local connection?", required: true, submitOnChange: true
            if (localOnly) {
                paragraph "NOTE: When using a local connection, you need to ensure 'Secure Only' is not checked in Blue Iris' Webserver settings."
                paragraph "Since you're using a local connection, use the local IP address for Webserver Host, do not include http:// or anything but the IP address."
            } else {
                paragraph "Since you're using an external connection, use the external IP address for Webserver Host, and be sure to include the full address (i.e. include http:// or https://, .com, etc)."
                paragraph "If you are using Stunnel, ensure the SSL certificate is from a Certificate Authority (CA), it cannot be self-signed. You can create a free CA signed certificate at www.letsencrypt.org"
            }
            input "host", "text", title: "BI Webserver Host (only include http:// if using an external address)", required:true
            input "port", "number", title: "BI Webserver Port (e.g. 81)", required:true
            paragraph "Note: Blue Iris only allows Admin Users to toggle profiles."
            input "username", "text", title: "BI Username", required: true
            input "password", "password", title: "BI Password", required: true
        }
        section("Blue Iris Profile/Smartthings Mode Integration") {
            paragraph "Enter the number (1-7) of the Blue Iris Profile for each of your Smartthings modes below. To ignore a mode leave it blank.  Entering '0' sets Blue Iris to 'inactive.' If you don't want to integrate Smartthings Modes with Blue Iris Profiles, leave them all blank."
            location.modes.each { mode ->
                def modeId = mode.id.toString()  
                input "mode-${modeId}", "number", title: "Mode ${mode}", required: false
            }
        }
        section ("Make hold or temporary profile changes?") {
            paragraph "Hold changes remain until the next change is made (e.g. you change it or this app does).  Temporary changes will only be in effect for the 'Temp Time' duration set for each profile in Blue Iris Settings > Profiles. At the end of that time, Blue Iris will change profiles according to your schedule."
            paragraph "Note: if Blue Iris restarts when the profile was made with a temp change, it will start in whatever profile your schedule dictates. A hold change will remain even after a restart."
            input "holdTemp", "bool", title: "Make Hold changes?", required: true
        }
        section("Notifications") {
            paragraph "You can choose to receive push notifications or a SMS."
            input("recipients", "contact", title: "Send notifications to") {
            input "pushAndPhone", "enum", title: "Also send SMS? (optional, it will always send push)", required: false, options: ["Yes", "No"]      
            input "phone", "phone", title: "Phone Number (only for SMS)", required: false
            paragraph "If outside the US please make sure to enter the proper country code"
            }
            paragraph "Each trigger can send it's own notifications.  Do you also want to receive notificaitons for profile changes?"
            input "receiveAlerts", "enum", title: "Receive Profile Change Notifications?", options: ["Yes", "No"], required: true
        }
        section("Debug"){
            paragraph "You can turn on debug logging, viewed in Live Logging on the API website."
            def loggingOn = false
            input "loggingOn", "bool", title: "Debug Logging On?"
        }
    }
}

def installed() {
    if (loggingOn) log.debug "Installed with settings: ${settings}"
    subscribe(location, modeChange)
}

def updated() {
    if (loggingOn) log.debug "Updated with settings: ${settings}"
    unsubscribe()
    subscribe(location, modeChange)
}

def modeChange(evt) {
    if (evt.name != "mode") {return;}
    if (loggingOn) log.debug "BI_modeChange detected. " + evt.value
    def checkMode = ""
    
    location.modes.each { mode ->
        if (mode.name == evt.value){
            checkMode = "mode-" + mode.id
            if (loggingOn) log.debug "BI_modeChange matched to " + mode.name
        }
    }
    
    if (checkMode != "" && settings[checkMode]){
        if (loggingOn) log.debug "BI_Found profile " + settings[checkMode]
        if(localOnly){
            localAction(settings[checkMode].toInteger())
        } else externalAction(settings[checkMode].toInteger())
    }
}

def localAction(profile) {
    def biHost = "${host}:${port}"
    def biRawCommand = "/admin?profile=${profile}&user=${username}&pw=${password}"
    if (loggingOn) log.debug "Changed Blue Iris Profile to ${profile} via GET to URL $biHost/$biRawCommand"
    if(!holdTemp) {
        if(receiveAlerts == "No") sendNotificationEvent("Temporarily changed Blue Iris to profile ${profileName(BIprofileNames,profile)}")
        if(receiveAlerts == "Yes") send("Temporarily changed Blue Iris to profile ${profileName(BIprofileNames,profile)}")
    }
    def httpMethod = "GET"
    def httpRequest = [
        method:     httpMethod,
        path:       biRawCommand,
        headers:    [
                    HOST:       biHost,
                    Accept:     "*/*",
                    ]
        ]
    def hubAction = new physicalgraph.device.HubAction(httpRequest)
    sendHubCommand(hubAction)
    if(holdTemp) {
        sendHubCommand(hubAction)
        if(receiveAlerts == "No") sendNotificationEvent("Blue Iris Fusion hold changed Blue Iris to profile ${profileName(BIprofileNames,profile)}")
        if(receiveAlerts == "Yes") send("Blue Iris Fusion hold changed Blue Iris to profile ${profileName(BIprofileNames,profile)}")
    }
    //todo - add error notifications (need to figure out how to check for errors first!)
}
    
def externalAction(profile) {
    def errorMsg = "Blue Iris Fusion could not adjust Blue Iris Profile"
    if (!holdTemp) {
        try {
            httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"login"]) { response ->
                if (loggingOn) log.debug response.data

                if (response.data.result == "fail") {
                   if (loggingOn) log.debug "BI_Inside initial call fail, proceeding to login"
                   def session = response.data.session
                   def hash = username + ":" + response.data.session + ":" + password
                   hash = hash.encodeAsMD5()

                   httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"login","session":session,"response":hash]) { response2 ->
                        if (response2.data.result == "success") {
                            def BIprofileNames = response2.data.data.profiles
                            if (loggingOn) log.debug ("BI_Logged In")
                            if (loggingOn) log.debug response2.data
                            httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"status","session":session]) { response3 ->
                                if (loggingOn) log.debug ("BI_Retrieved Status")
                                if (loggingOn) log.debug response3.data
                                if (response3.data.result == "success"){
                                    if (response3.data.data.profile != profile){
                                        httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"status","profile":profile,"session":session]) { response4 ->
                                            if (loggingOn) log.debug response4.data
                                            if (response4.data.result == "success") {
                                                if (response4.data.data.profile.toInteger() == profile.toInteger()) {
                                                    if (loggingOn) log.debug ("Blue Iris to profile ${profileName(BIprofileNames,profile)}!")
                                                    if(receiveAlerts == "No") sendNotificationEvent("Blue Iris Fusion temporarily changed Blue Iris to profile ${profileName(BIprofileNames,profile)}")
                                                    if (receiveAlerts == "Yes") send("Blue Iris Fusion temporarily changed Blue Iris to profile ${profileName(BIprofileNames,profile)}")
                                                } else {
                                                    if (loggingOn) log.debug ("Blue Iris ended up on profile ${profileName(BIprofileNames,response4.data.data.profile)}? Temp change to ${profileName(BIprofileNames,profile)}. Check your user permissions.")
                                                    if (receiveAlerts == "No") sendNotificationEvent("Blue Iris Fusion failed to change Profiles, it is in ${profileName(BIprofileNames,response4.data.data.profile)}? Check your user permissions.")
                                                    if (receiveAlerts == "Yes") send("Blue Iris Fusion failed to change Profiles, it is in ${profileName(BIprofileNames,response4.data.data.profile)}? Check your user permissions.")
                                                }
                                                httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"logout","session":session]) { response5 ->
                                                    if (loggingOn) log.debug response5.data
                                                    if (loggingOn) log.debug "Logged out"
                                                }
                                            } else {
                                                if (loggingOn) log.debug "BI_FAILURE"
                                                if (loggingOn) log.debug(response4.data.data.reason)
                                                if (receiveAlerts == "No") sendNotificationEvent(errorMsg)
                                                if (receiveAlerts == "Yes") send(errorMsg)
                                            }
                                        }
                                    } else {
                                        if (loggingOn) log.debug ("Blue Iris is already at profile ${profileName(BIprofileNames,profile)}.")
                                        sendNotificationEvent("Blue Iris is already in profile ${profileName(BIprofileNames,profile)}.")
                                    }
                                } else {
                                    if (loggingOn) log.debug "BI_FAILURE"
                                    if (loggingOn) log.debug(response3.data.data.reason)
                                    if (receiveAlerts == "No") sendNotificationEvent(errorMsg)
                                    if (receiveAlerts == "Yes") send(errorMsg)
                                }
                            }
                        } else {
                            if (loggingOn) log.debug "BI_FAILURE"
                            if (loggingOn) log.debug(response2.data.data.reason)
                            if (receiveAlerts == "No") sendNotificationEvent(errorMsg)
                            if (receiveAlerts == "Yes") send(errorMsg)
                        }
                    }
                } else {
                    if (loggingOn) log.debug "FAILURE"
                    if (loggingOn) log.debug(response.data.data.reason)
                    if (receiveAlerts == "No") sendNotificationEvent(errorMsg)
                    if (receiveAlerts == "Yes") send(errorMsg)
                }
            }
        } catch(Exception e) {
            if (loggingOn) log.debug(e)
            if (receiveAlerts == "No") sendNotificationEvent(errorMsg)
            if (receiveAlerts == "Yes") send(errorMsg)
        }
    } else {
        try {
            httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"login"]) { response ->
                if (loggingOn) log.debug response.data

                if (response.data.result == "fail") {
                   if (loggingOn) log.debug "BI_Inside initial call fail, proceeding to login"
                   def session = response.data.session
                   def hash = username + ":" + response.data.session + ":" + password
                   hash = hash.encodeAsMD5()

                   httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"login","session":session,"response":hash]) { response2 ->
                        if (response2.data.result == "success") {
                            def BIprofileNames = response2.data.data.profiles
                            if (loggingOn) log.debug ("BI_Logged In")
                            if (loggingOn) log.debug response2.data
                            httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"status","session":session]) { response3 ->
                                if (loggingOn) log.debug ("BI_Retrieved Status")
                                if (loggingOn) log.debug response3.data
                                if (response3.data.result == "success"){
                                    if (response3.data.data.profile != profile){
                                        httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"status","profile":profile,"session":session]) { response4 ->
                                            if (loggingOn) log.debug response4.data
                                            if (response4.data.result == "success") {
                                                if (loggingOn) log.debug "Set profile to ${profileName(BIprofileNames,profile)} via temp change, trying to set via hold"
                                                if (response4.data.data.profile.toInteger() == profile.toInteger()) {
                                                    httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"status","profile":profile,"session":session]) { response5 ->
                                                        if (loggingOn) log.debug response5.data
                                                        if (response5.data.result == "success") {
                                                            if (loggingOn) log.debug ("Set profile to ${profileName(BIprofileNames,profile)} with a hold change!")
                                                            if (receiveAlerts == "No") sendNotificationEvent("Blue Iris Fusion hold changed Blue Iris to profile ${profileName(BIprofileNames,profile)}")
                                                            if (receiveAlerts == "Yes") send("Blue Iris Fusion hold changed Blue Iris to profile ${profileName(BIprofileNames,profile)}")
                                                        } else {
                                                            if (loggingOn) log.debug ("Blue Iris Fusion failed to hold profile, it is in ${profileName(BIprofileNames,response5.data.data.profile)}? but is only temporarily changed.")
                                                            if (receiveAlerts == "No") sendNotificationEvent("Blue Iris Fusion failed to hold profile, it is in ${profileName(BIprofileNames,response5.data.data.profile)}? but is only temporarily changed.")
                                                            if (receiveAlerts == "Yes") send("Blue Iris Fusion failed to hold profile, it is in ${profileName(BIprofileNames,response5.data.data.profile)}? but is only temporarily changed.")
                                                        }
                                                   }
                                                } else {
                                                    if (loggingOn) log.debug ("Blue Iris ended up on profile ${profileName(BIprofileNames,response4.data.data.profile)}? Attempt to set ${profileName(BIprofileNames,profile)} failed, also unable to attempt hold. Check your user permissions.")
                                                    if (receiveAlerts == "No") sendNotificationEvent("Blue Iris Fusion failed to change Profiles, it is in ${profileName(BIprofileNames,response4.data.data.profile)}? Check your user permissions.")
                                                    if (receiveAlerts == "Yes") send("Blue Iris Fusion failed to change Profiles, it is in ${profileName(BIprofileNames,response4.data.data.profile)}? Check your user permissions.")
                                                }
                                                httpPostJson(uri: host + ':' + port, path: '/json',  body: ["cmd":"logout","session":session]) { response6 ->
                                                    if (loggingOn) log.debug response6.data
                                                    if (loggingOn) log.debug "Logged out"
                                                }
                                            } else {
                                                if (loggingOn) log.debug "BI_FAILURE"
                                                if (loggingOn) log.debug(response4.data.data.reason)
                                                if (receiveAlerts == "No") sendNotificationEvent(errorMsg)
                                                if (receiveAlerts == "Yes") send(errorMsg)
                                            }
                                        }
                                    } else {
                                        if (loggingOn) log.debug ("Blue Iris is already at profile ${profileName(BIprofileNames,profile)}.")
                                        sendNotificationEvent("Blue Iris is already in profile ${profileName(BIprofileNames,profile)}.")
                                        }
                                } else {
                                    if (loggingOn) log.debug "BI_FAILURE"
                                    if (loggingOn) log.debug(response3.data.data.reason)
                                    if (receiveAlerts == "No") sendNotificationEvent(errorMsg)
                                    if (receiveAlerts == "Yes") send(errorMsg)
                                }
                            }
                        } else {
                            if (loggingOn) log.debug "BI_FAILURE"
                            if (loggingOn) log.debug(response2.data.data.reason)
                            if (receiveAlerts == "No") sendNotificationEvent(errorMsg)
                            if (receiveAlerts == "Yes") send(errorMsg)
                        }
                    }
                } else {
                    if (loggingOn) log.debug "FAILURE"
                    if (loggingOn) log.debug(response.data.data.reason)
                    if (receiveAlerts == "No") sendNotificationEvent(errorMsg)
                    if (receiveAlerts == "Yes") send(errorMsg)
                }
            }
        } catch(Exception e) {
            if (loggingOn) log.debug(e)
            if (receiveAlerts == "No") sendNotificationEvent(errorMsg)
            if (receiveAlerts == "Yes") send(errorMsg)
        }
    }
}

def profileName(names, num) {
    if (names[num.toInteger()]) {
        names[num.toInteger()] + " (#${num})"
    } else {
        '#' + num
    }
}

private send(msg) {
    if (location.contactBookEnabled) {
        if (loggingOn) log.debug("sending notifications to: ${recipients?.size()}")
        sendNotificationToContacts(msg, recipients)
    }
    else {
        Map options = [:]
        if (phone) {
            options.phone = phone
            if (loggingOn) log.debug 'sending SMS'
        } else if (pushAndPhone == 'Yes') {
            options.method = 'both'
            options.phone = phone
        } else options.method = 'push'
        sendNotification(msg, options)
    }
    if (loggingOn) log.debug msg
}