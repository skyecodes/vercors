# Vercors

### Simple Minecraft launcher with fully integrated modded support

This repository contains all the code related to Vercors, separated into 2 parts:

* [launcher](launcher): the desktop application
* [meta](meta): the metadata API backend

To build and run locally, you first need to uncomment the following properties
in [gradle.properties](gradle.properties):

* `curseForgeApiKey`: set its value to an API key that you can generate [here](https://console.curseforge.com/).
* `microsoftClientId`: leave empty - Microsoft authentication will be disabled
* `vercorsApiKey`: leave the default value
* `vercorsApiUrl`: leave the default value

You can then use the following command:

* `./gradlew meta:backend:run` to run the metadata backend service
* `./gradlew launcher:app:run` to run the launcher application