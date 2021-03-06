# Changelog

## Version 0.9
- Added trace() method to Log interface

## Version 0.8
- Refreshed Hubitat APIs
- Auto compare APIs with dumped files with methods
- Fixed issue with page ordering (and reachability detection)
- More tests added

## Version 0.7
- Refreshed Hubitat APIs
- userSettingsValues support for HubitatDeviceScript + tests.
- Added warn log level
- Debugger detection (to avoid spurious property readings)
- Groovydoc is now embedded into the package.

## Version 0.5
- Support for enum values syntax with strings:
```groovy
input(... type: "enum", options: ["0": "Val1", "1": "Val2"], defaultValue: "1" ...)
```
- Bugfixes for capabilities.

## Version 0.4
Added partial validation of device's `parse()` method.