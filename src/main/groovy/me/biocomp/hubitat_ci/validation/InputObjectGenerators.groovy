package me.biocomp.hubitat_ci.validation

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

/**
 * Input objects generator.
 * 'Input object' is an object that script sees when it tries to read a value of input type.
 * By using something smarter than 'null', we can do better validation.
 * In particular, 'device' or 'capability' input object can have all the supported methods
 * and attributes generated, and fail if user calls incorrect ones.
 */
interface IInputObjectGenerator
{
    /**
        Makes an instance of input object that can be used in the script.
        In this case, user has provided a value, and it will be somehow incorporated in that object (or completely replaces it).

        @param userProvidedAndDefaultValues - map of 0-2 elements with possible values:
         <br/> userProvidedValue - value that user provided as mock (possibly null)
         <br/> defaultValue - value that was specified as default in input() (possibly null)
     */
    def makeInputObject(String inputName, String inputType, DefaultAndUserValues userProvidedAndDefaultValues)
}

/**
 * Produces any text type like 'email', 'text' and so on.
 */
class TextInputObjectGenerator implements IInputObjectGenerator
{
    final String inputName

    @Override
    @CompileStatic
    def makeInputObject(String inputName, String inputType, DefaultAndUserValues userProvidedAndDefaultValues) {
        return InputCommon.returnUserOrDefaultOrCustomValue(
                userProvidedAndDefaultValues,
                "Input '${inputName}' of type '${inputType}'")
    }
}

/**
 * Produces Boolean object
 */
class BooleanInputObjectGenerator implements IInputObjectGenerator
{
    final String inputName

    @Override
    @CompileStatic
    def makeInputObject(String inputName, String inputType, DefaultAndUserValues userProvidedAndDefaultValues) {
        return InputCommon.returnUserOrDefaultOrCustomValue(userProvidedAndDefaultValues, new Boolean(true))
    }
}

/**
 * Number input generator (produces Integer).
 */
class NumberInputObjectGenerator implements IInputObjectGenerator
{
    @Override
    def makeInputObject(String inputName, String inputType, DefaultAndUserValues userProvidedAndDefaultValues) {
        return InputCommon.returnUserOrDefaultOrCustomValue(userProvidedAndDefaultValues, new Integer(0))
    }
}

/**
 * When input validation is disabled, this generator is used for all types.
 * Produces either String or user-provided object.
 */
class UnvalidatedInputObjectGenerator implements IInputObjectGenerator
{
    @Override
    @CompileStatic
    def makeInputObject(String inputName, String inputType, DefaultAndUserValues userProvidedAndDefaultValues) {
        return InputCommon.returnUserOrDefaultOrCustomValue(
                userProvidedAndDefaultValues,
                "Input '${inputName}' type '${inputType}' was not validated, so this generic string is used as mock value")
    }
}

