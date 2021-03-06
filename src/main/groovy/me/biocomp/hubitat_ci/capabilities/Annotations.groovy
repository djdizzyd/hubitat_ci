package me.biocomp.hubitat_ci.capabilities

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.ElementType
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@interface CustomDeviceSelector { 
    String deviceSelector() 
}

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@interface CustomDriverDefinition { 
    String driverDefinition() 
}

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@interface CustomPrettyName {
    String prettyName()
}
