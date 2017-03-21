/**
 *  Run circulate in HVAC
 *
 *  Copyright 2014 Bob Sanford
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
 */
definition(
    name: "Run circulate in HVAC",
    namespace: "Thermostat Control",
    author: "Bob Sanford - Modified by Tim Soedarjatno",
    description: "Run circulate every X minutes if AC or heat has not been on",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png")


preferences {
	section("Title") {
		paragraph "Run circulate in HVAC"
	}
	section("About") {
        	paragraph "Run circulate every X minutes if AC or heat has not been on"
            paragraph "Additional setpoint can be used to determine minimum run temperature."
    	}
    	section("Thermostat") {
        	input "thermostat", "capability.thermostat", title:"Select thermostat to be controlled"
        	input "interval", "number", title:"Set time between circulation cycles (in minutes)", defaultValue:30
        	input "length", "number", title:"Set of length of circulation cycle (in minutes)", defaultValue:5
		}
        section("Choose a temperature sensor... "){
			input "sensor", "capability.temperatureMeasurement", title: "Temperature Sensor used to establish minimum run temperature"
		}
		section("Operation") {
			input "runTemp", "number", title:"Choose a temperature to set the minimum run temperature.", defaultValue:70
			input "onoff", "enum", title:"Enable or Disable this program.", options:["Enabled","Disabled"], multiple: false, required:true 
        }
}

def installed() {
	DEBUG("Installed with settings: ${settings}")
    initialize()
}

def updated() {
	DEBUG("Updated with settings: ${settings}")
	unsubscribe()
	unschedule()
   	initialize()

}

def initialize() {
	DEBUG("initialize()")
	if(onoff == "Enabled"){
   		DEBUG("Program Enabled.  Starting Scheduler.")
		scheduler()
    }
}

def scheduler(){
	DEBUG ("scheduler()")
	DEBUG("fanOn Interval in minutes: ${interval}, Run length in minutes: ${length}")
    DEBUG("fanAuto Interval (Interval+Length) = ${interval+length}")
	schedule("0 0/${interval} * * * ?", start_circulate)
	schedule("0 0/${interval+length} * * * ?", stop_circulate)
}
        
def start_circulate(){
	DEBUG("start_circulate()")
    DEBUG ("running_state: ${thermostat.currentValue("thermostatOperatingState")}")
    DEBUG ("On/Off Switch: $onoff")
	if (thermostat.currentValue("thermostatOperatingState") == "idle" && sensor.currentValue("temperature") >= runtemp)
	{	LOG("Turning Thermostat fanOn")
		thermostat.fanOn()
   	}
}

def stop_circulate() {
	LOG("Turning Thermostat fanAuto")
    thermostat.fanAuto()
}



private def LOG(message){
	log.info message
}

private def DEBUG(message){
	log.debug message
}