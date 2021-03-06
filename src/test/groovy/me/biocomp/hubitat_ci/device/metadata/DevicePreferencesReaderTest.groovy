package me.biocomp.hubitat_ci.device.metadata

import me.biocomp.hubitat_ci.api.device_api.DeviceExecutor
import me.biocomp.hubitat_ci.device.HubitatDeviceSandbox
import me.biocomp.hubitat_ci.util.CapturingLog
import me.biocomp.hubitat_ci.validation.Flags
import spock.lang.Unroll

class DevicePreferencesReaderTest extends
        spock.lang.Specification
{
    private static List<DeviceInput> readPreferences(String script, List<Flags> extraFlags = []) {
        return new HubitatDeviceSandbox(script).run(
                validationFlags: extraFlags + [Flags.DontValidateDefinition, Flags.DontRequireParseMethodInDevice]).producedPreferences
    }

    private static DeviceInput readInput(String inputParameters, List<Flags> extraFlags = []) {
        return readPreferences("""
metadata {
    preferences {
        input ${inputParameters}
    }
}
""", extraFlags)[0]
    }


    def "Reading complex configuration with setting additional unrelated state should apparently work"() {
        setup:
            def log = new CapturingLog()
            DeviceExecutor api = Mock { _ * getLog() >> log }

            HubitatDeviceSandbox sandbox = new HubitatDeviceSandbox("""
metadata {
    preferences() {
        datetimeFormat = 1
        sourceastronomy = 1
        extSource = 0
        is_day = 0
        sourceImg = true 
        iconStore = "https://github.com/Scottma61/WeatherIcons/blob/master/"
        section("Query Inputs"){
            input "extSource", "enum", title: "Select External Source", required:true, defaultValue: "None", options: [1:"None", 2:"WeatherUnderground", 3:"Apixu", 4:"DarkSky"]
            input "pollIntervalStation", "enum", title: "Station Poll Interval", required: true, defaultValue: "3 Hours", options: ["Manual Poll Only", "5 Minutes", "10 Minutes", "15 Minutes", "30 Minutes", "1 Hour", "3 Hours"]
            input "stationLatHemi", "enum", title: "Station Northern or Southern hemisphere?", required: true, defaultValue: "North", options: ["North", "South"]
            input "stationLongHemi", "enum", title: "Station East or West of GMT (London, UK)?", required: true, defaultValue: "West", options: ["West", "East"]
            input "pollLocationStation", "text", required: true, title: "Station Data File Location:", defaultValue: "http://"
            LOGINFO("extSource: \${extSource}")
            if(extSource.toInteger() != 1){
                input "apiKey", "text", required: true, title: "API Key"
                input "pollIntervalForecast", "enum", title: "External Source Poll Interval", required: true, defaultValue: "3 Hours", options: ["Manual Poll Only", "5 Minutes", "10 Minutes", "15 Minutes", "30 Minutes", "1 Hour", "3 Hours"]
                input "pollLocationForecast", "text", required: true, title: "ZIP Code or Location"
\t\t\t\tLOGINFO("pollLocationForecast: \${pollLocationForecast}")                
                input "sourceImg", "bool", required: true, defaultValue: false, title: "Icons from: On = Standard - Off = Alternative"
                input "iconStore", "text", required: true, defaultValue: "https://github.com/Scottma61/WeatherIcons/blob/master/", title: "Alternative Icon Location:"
            }
            input "iconType", "bool", title: "Condition Icon: On = Current - Off = Forecast", required: true, defaultValue: false
\t    \tinput "tempFormat", "enum", required: true, defaultValue: "Fahrenheit (°F)", title: "Display Unit - Temperature: Fahrenheit (°F) or Celsius (°C)",  options: ["Fahrenheit (°F)", "Celsius (°C)"]
            input "datetimeFormat", "enum", required: true, defaultValue: "m/d/yyyy 12 hour (am|pm)", title: "Display Unit - Date-Time Format",  options: [1:"m/d/yyyy 12 hour (am|pm)", 2:"m/d/yyyy 24 hour", 3:"mm/dd/yyyy 12 hour (am|pm)", 4:"mm/dd/yyyy 24 hour", 5:"d/m/yyyy 12 hour (am|pm)", 6:"d/m/yyyy 24 hour", 7:"dd/mm/yyyy 12 hour (am|pm)", 8:"dd/mm/yyyy 24 hour", 9:"yyyy/mm/dd 24 hour"]
            input "distanceFormat", "enum", required: true, defaultValue: "Miles (mph)", title: "Display Unit - Distance/Speed: Miles or Kilometres",  options: ["Miles (mph)", "Kilometers (kph)"]
            input "pressureFormat", "enum", required: true, defaultValue: "Inches", title: "Display Unit - Pressure: Inches or Millibar",  options: ["Inches", "Millibar"]
            input "rainFormat", "enum", required: true, defaultValue: "Inches", title: "Display Unit - Precipitation: Inches or Millimetres",  options: ["Inches", "Millimetres"]
            input "summaryType", "bool", title: "Full Weather Summary", required: true, defaultValue: false
            input "logSet", "bool", title: "Create extended Logging", required: true, defaultValue: false
            

            input "sourcefeelsLike", "bool", required: true, title: "Feelslike from Weather-Display?", defaultValue: true
\t\t    input "sourceIllumination", "bool", required: true, title: "Illuminance from Weather-Display?", defaultValue: true
            input "sourceUV", "bool", required: true, title: "UV from Weather-Display?", defaultValue: true
            input "sourceastronomy", "enum", required: true, defaultValue: "Sunrise-Sunset.org", title: "Astronomy Source:", options: [1:"Sunrise-Sunset.org", 2:"Weather-Display", 3:"WeatherUnderground.com", 4:"APIXU.com", 5:"DarkSky.net"]
        }
    }
}


def LOGINFO(txt){
    try {
        if(state.logSet == true){log.info("Weather-Display Driver - INFO:  \${txt}") }
    } catch(ex) {
        log.error("LOGINFO unable to output requested data!")
    }
}
""")

        expect:
            sandbox.run(api: api,
                    validationFlags: [Flags.DontValidateDefinition, Flags.AllowSectionsInDevicePreferences, Flags.AllowWritingToSettings, Flags.DontRequireParseMethodInDevice])
    }

    def "preferences() with no inputs work"() {
        expect:
            readPreferences("""
metadata{
    preferences()
}
""").size() == 0
    }

    @Unroll
    def "input(#inputDef) fails when name or type are missing"(String inputDef, String[] failureMessages) {
        when:
            readInput(inputDef)

        then:
            AssertionError e = thrown()
            failureMessages.each { e.message.contains(it) }

        where:
            inputDef       | failureMessages
            "name: 'nam'"  | ["nam", "type", "missing"]
            "type: 'bool'" | ["bool", "name", "missing"]
            "title: 'tit'" | ["tit", "name", "type", "missing"]
            "'nam', null"  | ["nam", "type", "missing"]
    }

    @Unroll
    def "input(#inputDef) can be created if only name (= #expectedName) and type (= #expectedType) are specified. Flags: #extraFlags"(
            String inputDef, List<Flags> extraFlags, String expectedName, String expectedType)
    {
        when:
            def input = readInput(inputDef, extraFlags)

        then:
            input.readName() == expectedName
            input.readType() == expectedType

        where:
            inputDef                      | extraFlags                                || expectedName | expectedType
            "name: 'nam', type: 'bool'"   | []                                        || "nam"        | "bool"
            "title: 'tit', 'nam', 'bool'" | []                                        || "nam"        | "bool"
            "name: '', type: 'bool'"      | [Flags.DontValidateDeviceInputName]       || ""           | "bool"
            "type: 'bool'"                | [Flags.AllowMissingDeviceInputNameOrType] || null         | "bool"
            "title: 'tit', '', 'bool'"    | [Flags.DontValidateDeviceInputName]       || ""           | "bool"
            "null, 'bool'"                | [Flags.DontValidateDeviceInputName]       || null         | "bool"
    }

    @Unroll
    def "Calling with valid type: input(#type) succeeds"(String type, String extraOptions) {
        when:
            def input = readInput("name: 'nam', type: '${type}'${extraOptions ? " , " + extraOptions : ""}")

        then:
            input.readType() == type

        where:
            type       | extraOptions
            'bool'     | ""
            'decimal'  | ""
            'email'    | ""
            'enum'     | "options: ['a', 'b']"
            'number'   | ""
            'password' | ""
            'phone'    | ""
            'time'     | ""
            'text'     | ""
    }

    def "Calling with invalid input type fails"() {
        when:
            def input = readInput("'nam', 'badType'")

        then:
            AssertionError e = thrown()
            e.message.contains('not supported')
            e.message.contains('badType')
    }

    def "Calling input() with every valid option succeeds"() {
        when:
            def input = readInput(
                    """name: 'nam', type: 'enum', title: 'tit', description: 'desc', required: true, displayDuringSetup: true, range: '10..1000', options: ['val1', 'val2']""")

        then:
            input.options.name == 'nam'
            input.options.type == 'enum'
            input.options.title == 'tit'
            input.options.description == 'desc'
            input.options.required == true
            input.options.displayDuringSetup == true
            input.options.range == '10..1000'
            input.options.options == ['val1', 'val2']
    }

    @Unroll
    def "input type '#type' does not support options"(def type) {
        when:
            readInput("""name: 'nam', type: '$type', title: 'tit', options: ['val1', 'val2']""")

        then:
            AssertionError e = thrown()
            e.message.contains("only 'enum' input type needs 'options' parameter")
            e.message.contains("nam")
            e.message.contains(type)

        where:
            type       | _
            'bool'     | _
            'decimal'  | _
            'email'    | _
            'number'   | _
            'password' | _
            'phone'    | _
            'time'     | _
            'text'     | _
    }

    def "Calling input() with invalid option fails"() {
        when:
            def input = readInput("badOption: 123, 'nam', 'bool'")

        then:
            AssertionError e = thrown()
            e.message.contains("'badOption' is not supported")
    }

    def "Accessing 'inputs' that were not defined when running the script fails"() {
        setup:
            def script = new HubitatDeviceSandbox("""
metadata {
    preferences() {
         input "existingInput", "text"
    }
}

private def someInternalMethod()
{
    def good = existingInput
    def bad = missingInput
}

def methodThatUsesInputs()
{
    someInternalMethod()
}
""").run(validationFlags: [Flags.DontValidateDefinition, Flags.DontRequireParseMethodInDevice])

        when:
            script.methodThatUsesInputs()

        then:
            AssertionError e = thrown()
            e.message.contains(
                    "In 'someInternalMethod' settings were read that are not registered inputs: [missingInput]. These are registered inputs: [existingInput]. This is not allowed in strict mode (add Flags.AllowReadingNonInputSettings to allow this).")
            !e.message.contains("'methodThatUsesInputs'")
    }

    @Unroll
    def "Input (#type, #extraOptions) with defaults have default value, with no default have some meaningful value"(
            def type, Map extraOptions, def expectedValue)
    {
        setup:
            def script = new HubitatDeviceSandbox("""
metadata {
    preferences() {
         input 'myInput', '${type}' ${extraOptions.collect { k, v -> ", ${k.inspect()}: ${v.inspect()}" }.join('')}
    }
}

def readInput()
{
    myInput
}
""").run(validationFlags: [Flags.DontRequireParseMethodInDevice, Flags.DontValidateDefinition])
        expect:
            script.readInput() == expectedValue

        where:
            type       | extraOptions                                           || expectedValue
            'bool'     | [:]                                                    || true
            'bool'     | [defaultValue: false]                                  || false
            'decimal'  | [:]                                                    || 0
            'decimal'  | [defaultValue: 123]                                    || 123
            'email'    | [:]                                                    || "Input 'myInput' of type 'email'"
            'email'    | [defaultValue: 'default val']                          || "default val"
            'enum'     | [options: ['a', 'b']]                                  || "a"
            'enum'     | [options: [1: 'a', 2: 'b']]                            || "1"
            'enum'     | [options: ["1": 'a', "2": 'b']]                        || "1"
            'enum'     | [options: ['a', 'b'], defaultValue: 'b']               || "b"
            'enum'     | [options: [1: 'a', 2: 'b'], defaultValue: 'b']         || "2"
            'enum'     | [options: [[1: 'a'], [2: 'b']], defaultValue: 'b']     || "2"
            'enum'     | [options: [['1': 'a'], ['2': 'b']], defaultValue: 'b'] || "2"
            'enum'     | [options: ['1': 'a', '2': 'b'], defaultValue: 'b']     || "2"
            'enum'     | [options: ['1': 'a', '2': 'b'], defaultValue: '2']     || "2"
            'enum'     | [options: ['1': 'a', '2': 'b'], defaultValue: '1']     || "1"
            'number'   | [:]                                                    || 0
            'number'   | [defaultValue: 123]                                    || 123
            'password' | [:]                                                    || "Input 'myInput' of type 'password'"
            'password' | [defaultValue: 'default val']                          || "default val"
            'phone'    | [:]                                                    || 0
            'phone'    | [defaultValue: 123]                                    || 123
            'time'     | [:]                                                    || "Input 'myInput' of type 'time'"
            'time'     | [defaultValue: 'default val']                          || "default val"
            'text'     | [:]                                                    || "Input 'myInput' of type 'text'"
            'text'     | [defaultValue: 'default val']                          || "default val"
    }
}