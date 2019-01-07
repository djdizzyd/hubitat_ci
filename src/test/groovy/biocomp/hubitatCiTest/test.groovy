package biocomp.hubitatCiTest

import biocomp.hubitatCiTest.emulation.AppExecutorApi
import biocomp.hubitatCiTest.emulation.DeviceWrapper
import biocomp.hubitatCiTest.util.CapturingLog
import biocomp.hubitatCiTest.util.Log
import groovy.transform.TypeChecked
import spock.lang.Specification

class AppTemplateScriptTest extends
        Specification
{
    HubitatAppSandbox sandbox = new HubitatAppSandbox(new File("Scripts/New App Template.groovy"))

    def "Basic validation"() {
        expect:
            sandbox.runBasicValidations()
    }

    def "Installation succeeds and logs stuff"() {
        given:
            def log = Mock(Log)
            def api = Mock(AppExecutorApi)
            def script = sandbox.setupScript(true, api)
            script.getMetaClass().ventDevices = ["S1", "S2"]
            script.getMetaClass().numberOption = 123

        when:
            script.installed()

        then:
            _ * api.getLog() >> log
            1 * log.debug("initialize")
            1 * log.debug("ventDevices: " + ["S1", "S2"])
            1 * log.debug("numberOption: 123")
            1 * api.unschedule()
    }

    def "Update initializes again"() {
        given:
            def log = Mock(Log)
            def api = Mock(AppExecutorApi)
            def script = sandbox.setupScript(true, api)
            script.getMetaClass().ventDevices = ["S1", "S2"]
            script.getMetaClass().numberOption = 123

        when:
            script.updated()

        then:
            _ * api.log >> log
            1 * log.debug("updated")
            1 * log.debug("initialize")
            1 * log.debug("ventDevices: " + ["S1", "S2"])
            1 * log.debug("numberOption: 123")
            1 * api.unschedule()
    }

    def "Uninstallation succeeds"() {
        given:
            def log = Mock(Log)
            def api = Mock(AppExecutorApi)
            def script = sandbox.setupScript(true, api)

        when:
            script.uninstalled()

        then:
            _ * api.getLog() >> log
            1 * log.debug("uninstalled")
    }
}

class ThermostatDimerSyncHelperTest extends
        Specification
{
    def sandbox = new HubitatAppSandbox(new File("Scripts/ThermostatDimmerSyncHelper.groovy"))

    def "Basic validation"() {
        expect:
            sandbox.runBasicValidations()
    }
}