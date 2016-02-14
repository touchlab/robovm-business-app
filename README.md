# RoboVM ContractR sample (updated)

Sample app for robovm. It seemed really complex to me, and I wanted to try something with Ormlite and code style
similar to how we build android apps internally. Turns out the jdbc driver for robo doesn't implement setObject,
so there's work to be done on OrmLite. I did a basic version, but it still needs work.

Will be adding more stuff in the future. I'd like to build a reference app. I'd like to maybe move away from the bus
communication.  See how it goes. Ideally as much logic is put in shared code.

## iOS app

The iOS app is built using native UI components and APIs available through the
Cocoa bindings in RoboVM. This project depends on the core project which holds
all domain objects and services for managing clients, tasks and the work
performed.

### THE IOS APP IS CURRENTLY BROKEN!!!

## Android app

The Android app is built using standard Android components such as view XMLs,
Fragments and ActionBar.
