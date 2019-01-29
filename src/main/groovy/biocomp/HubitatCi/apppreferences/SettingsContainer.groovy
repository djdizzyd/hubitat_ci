package biocomp.hubitatCi.apppreferences


import biocomp.hubitatCi.validation.Validator
import biocomp.hubitatCi.validation.Flags

import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode

@TypeChecked
class SettingsContainer implements Map<String, Object>
{
    /**
     * @param delegate - forward all of AppExecutor's methods to this delegate (except for getSettings).
     * @param userSettingsValue - user can define settings in UTs, and rest of the settings will be added to it too.
     * @param mode - how to validate user actions with settings.
     */
    SettingsContainer(PreferencesReaderState preferencesState, Validator validator, Map userSettingsValue) {
        this.preferencesState = preferencesState
        this.validator = validator
        this.settingValues = userSettingsValue
    }


    /**
     * After all the relevant script methods were run, execute this method to (if in strict mode):

     * 1. Verify that settings were only read, and not set.
     * 2. Verify that only settings from 'inputs' param were read.*/
    void validateAfterPreferences() {
        if (!validator.hasFlag(Flags.AllowReadingNonInputSettings)) {
            def readSettingsThatAreNotInputs = settingsRead - registeredInputs
            assert !readSettingsThatAreNotInputs : "Settings were read that are not registered inputs: ${readSettingsThatAreNotInputs}. These are registered inputs: ${registeredInputs}. This is not allowed in strict mode (add Flags.AllowReadingNonInputSettings to allow this)"
        }
    }

    /**
     * Registeres data for future validation + gets actual value from user-provided data.
     * User data may contain either name-value pair for input values,
     * or value could itself be a map with page names as key and corresponding values.
     * @param name
     * @return
     */
    @TypeChecked(TypeCheckingMode.SKIP) // Skip for easier working with maps
    @Override
    Object get(Object name) {
        name = name as String
        settingsRead << name

        // We have per page mapping of values
        def currentPageName = preferencesState.hasCurrentPage() ? preferencesState.currentPage.readName() : null
        if (currentPageName && settingValues.get(name) instanceof Map && settingValues.get(name).containsKey('_') && settingValues.get(name).containsKey(currentPageName))
        {
            return settingValues.get(name)."${currentPageName}"
        }
        else if (currentPageName && settingValues.get(name) instanceof Map && settingValues.get(name).containsKey('_'))
        {
            return settingValues.get(name)."_"
        }

        return settingValues.get(name)
    }

    @Override
    Object put(String name, Object value) {
        if (!validator.hasFlag(Flags.AllowWritingToSettings)) {
            assert false: "You tried assigning '${value}' to '${name}', but writing to settings is not allowed in strict mode (add Flags.AllowWritingToSettings to allow this)."
        }

        return settingValues.put(name, value)
    }

    /**
     * Call this method to provide information about valid user input name.
     * It's called by AppPreferencesReader, during reading the preferences.
     *
     * This allows (in strict checking mode) to make sure that only
     * inputs provided so far were read.
     * @param p
     */
    void userInputFound(String name) {
        registeredInputs << name
    }

    private final PreferencesReaderState preferencesState
    private final Validator validator

    private final Set<String> settingsRead = []
    private final Set<String> registeredInputs = []

    @Delegate
    private final Map settingValues
}