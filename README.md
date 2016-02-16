# RoboVM ContractR sample (updated)

Sample app for robovm. Place to code and test ORMlite support, and eventually add more libs for
RoboVM.

## ORMLite

Its complete-ish. The trouble revolves around support in the jdbc drivers. There's a driver that's used
in the core unit tests, and different drivers for android and iOS. They all support a different subset
of features. Thankfully the iOS driver crashes when it doesn't support a method. The android one does
not, which is upsetting.

Anyway, ORMLite support should be pretty solid, but its brand new, so some types may be iffy.

## RoboSqliteOpenHelper

Android's db management scheme is kind of weird, but we're pretty used to it, so I'm replicating that here.
You supply the context, name, and version, and it'll manage upgrades.

## RoboVMContext

Providing some similar functionality to Android's Context. This is likely to change, but one thing
at a time.
