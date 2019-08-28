package me.biocomp.hubitat_ci.app.preferences

import biocomp.hubitatCi.app.AppValidator
import biocomp.hubitatCi.validation.Flags
import biocomp.hubitatCi.validation.NamedParametersValidator
import groovy.transform.TypeChecked

@TypeChecked
class Mode {
    final Map options

    private static final NamedParametersValidator paramValidator = NamedParametersValidator.make {
        stringParameter("title", required(), mustNotBeEmpty())
        boolParameter("required", notRequired())
        boolParameter("multiple", notRequired())
        stringParameter("image", notRequired(), mustNotBeEmpty())
    }

    Mode(Map options, EnumSet<Flags> validationFlags)
    {
        this.options = options

        if (!validationFlags.contains(Flags.DontValidatePreferences))
        {
            paramValidator.validate(this.toString(), options, validationFlags)
        }
    }

    @Override
    String toString() { return "mode(${options})" }
}
