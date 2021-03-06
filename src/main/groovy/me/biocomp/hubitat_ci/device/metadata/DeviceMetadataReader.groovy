package me.biocomp.hubitat_ci.device.metadata

import me.biocomp.hubitat_ci.api.device_api.*
import me.biocomp.hubitat_ci.device.DeviceData
import me.biocomp.hubitat_ci.device.DeviceValidator
import me.biocomp.hubitat_ci.util.ReaderState
import me.biocomp.hubitat_ci.validation.DebuggerDetector
import me.biocomp.hubitat_ci.validation.SettingsContainer
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@TypeChecked
class DeviceMetadataReader implements
        DeviceMetadataSource
{
    DeviceMetadataReader(
            DeviceExecutor delegate,
            DeviceValidator validator,
            Map userSettingsValue,
            MetaClass scriptMetaClass,
            DeviceData deviceData,
            DebuggerDetector debuggerDetector)
    {
        this.delegate = delegate
        this.validator = validator
        this.scriptMetaClass = scriptMetaClass
        this.settingsContainer = new SettingsContainer({ null },
                validator,
                userSettingsValue,
                deviceData,
                debuggerDetector);
        this.deviceData = deviceData
    }

    @Override
    void capability(String capabilityName) {
        def definition = states.getCurrentState('definition()') as Definition
        validator.validateCapability(capabilityName)
        definition.addCapability(capabilityName)
    }

    @Override
    void attribute(String attributeName, String attributeType, List<String> possibleValues) {
        def definition = states.getCurrentState('definition()') as Definition
        def attribute = new Attribute(attributeName, attributeType, possibleValues)
        validator.validateAttribute(attribute)
        definition.addAttribute(attribute)
    }

    @Override
    void command(String commandName, List parameterTypes) {
        def definition = states.getCurrentState('definition()') as Definition
        def command = new Command(commandName, parameterTypes,
                validator.findMethodForCommand(scriptMetaClass, commandName, parameterTypes))
        validator.validateCommand(command)
        definition.addCommand(command)
    }

    @Override
    void fingerprint(Map options) {
        def definition = states.getCurrentState('definition()') as Definition
        validator.validateFingerprint(options)
        definition.addFingerprint(options)
    }

    @Override
    void simulator(Closure closure) {
        // Just do nothing, this is probably not supported, but method is sometimes called.
    }

    @Override
    def input(Map options, String name, String type) {
        def preferences = states.getOneOfCurrentStates('preferences()', 'section()') as List<DeviceInput>
        def input = new DeviceInput([name: name, type: type], options, validator.flags)
        preferences.add(input)
    }

    @Override
    @CompileStatic
    def input(String name, String type) {
        def preferences = states.getOneOfCurrentStates('preferences()', 'section()') as List<DeviceInput>
        def input = new DeviceInput([name: name, type: type], [:], validator.flags)
        preferences.add(input)
    }

    @Override
    @CompileStatic
    def input(Map options) {
        def preferences = states.getOneOfCurrentStates('preferences()', 'section()') as List<DeviceInput>
        def input = new DeviceInput([:], options, validator.flags)
        preferences.add(input)
    }


    @Override
    void main(String tileTitle) {

    }

    @Override
    void main(List multipleTileTitles) {

    }

    @Override
    void details(String tileTitle) {

    }

    @Override
    void details(List multipleTileTitles) {

    }

    @Override
    void standardTile(
            Map options, String name, String associatedAttributeName, @DelegatesTo(DeviceTile) Closure makeContents)
    {}

    @Override
    void standardTile(
            String name, String associatedAttributeName, @DelegatesTo(DeviceTile) Closure makeContents)
    {
        standardTile([:], name, associatedAttributeName, makeContents)
    }

    @Override
    void valueTile(
            Map options, String name, String associatedAttributeName, @DelegatesTo(DeviceTile) Closure makeContents)
    {
    }

    @Override
    void valueTile(String name, String associatedAttributeName, @DelegatesTo(DeviceTile) Closure makeContents) {
        valueTile([:], name, associatedAttributeName, makeContents)
    }

    @Override
    void controlTile(
            Map options, String name, String associatedAttributeName, String controlType,
            @DelegatesTo(DeviceTile) Closure makeContents)
    {
    }


    @Override
    void multiAttributeTile(Map options, @DelegatesTo(DeviceMultiAttributeTile) Closure makeContents) {

    }

    @Override
    void tileAttribute(Map options, String associatedAttributeName, Closure makeContents) {
    }

    @Override
    void tileAttribute(String associatedAttributeName, Closure makeContents) {
        tileAttribute([:], associatedAttributeName, makeContents)
    }

    @Override
    void attributeState(Map options, String stateNameOrAttributeNameOrValue) {

    }

    @Override
    void state(Map options, String stateNameOrAttributeNameOrValue) {

    }

    @Override
    def section(String name, @DelegatesTo(DevicePreferences) Closure makeContents) {
        validator.validateSection(name)
        states.withState('section()', states.getOneOfParentStates('preferences()'), makeContents)
    }

    @Override
    void definition(Map options, @DelegatesTo(DeviceDefinition) Closure makeContents) {
        def definition = new Definition(options)
        producedDefinition = states.withState('definition()', definition, makeContents)
        validator.validateDefinition(definition, scriptMetaClass)
    }

    @Override
    void preferences(@DelegatesTo(DevicePreferences) Closure makeContents) {
        states.withState('preferences()', deviceData.producedPreferences, makeContents)

        settingsContainer.preferencesReadingDone()
        settingsContainer.validateAfterPreferences("preferences")
    }

    @Override
    void tiles(Map options, @DelegatesTo(DeviceTiles) Closure makeContents) {

    }

    @Override
    void metadata(@DelegatesTo(DeviceMetadata) Closure makeContents) {
        this.@hasMetadataCall = true
        states.withState('metadata()', this, makeContents)
    }

    @Delegate
    private final DeviceExecutor delegate

    private final ReaderState states = new ReaderState(['metadata()'   : [],
                                                        'definition()' : ['metadata()'],
                                                        'preferences()': ['metadata()'],
                                                        'section()'    : ['metadata()', 'preferences()']])

    private final DeviceValidator validator

    private Definition producedDefinition
    private final DeviceData deviceData
    private final SettingsContainer settingsContainer
    private final MetaClass scriptMetaClass
    private boolean hasMetadataCall = false

    boolean getHasMetadataCall() {
        return hasMetadataCall
    }

    Definition getProducedDefinition() {
        return producedDefinition
    }

    SettingsContainer getSettings() {
        return settingsContainer
    }
}

