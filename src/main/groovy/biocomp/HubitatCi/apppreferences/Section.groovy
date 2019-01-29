package biocomp.hubitatCi.apppreferences

import biocomp.hubitatCi.validation.Flags
import biocomp.hubitatCi.validation.NamedParametersValidator
import biocomp.hubitatCi.validation.Validator
import groovy.transform.TupleConstructor
import groovy.transform.TypeChecked

@TupleConstructor
@TypeChecked
class Section {
    int index
    String title
    Map options

    List children = []

    static private final NamedParametersValidator paramValidator = NamedParametersValidator.make{
        boolParameter(name:"hideable")
        boolParameter(name:"hidden")
        boolParameter(name:"mobileOnly")
        boolParameter(name:"hideWhenEmpty")
    }

    void validate(Validator validator)
    {
        if (!validator.hasFlag(Flags.DontValidatePreferences)) {
            paramValidator.validate(this.toString(), options, validator)
            assert children.size() != 0: "Section ${this} must have at least some content"
        }
    }
}