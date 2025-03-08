# vercors-launcher

### Launcher desktop application

This repository is split into several modules:

* `app` is the entrypoint of the Launcher, contains the code regarding app initialization and navigation
* `core` contains some common modules that are included in other modules
* `feature` contains most of the launcher data, UI and logic
    * `account` contains code regarding account management and authentication
    * `game` contains code regarding Minecraft versions and mod loaders
    * `home` contains code regarding the launcher Home page
    * `instance` contains code regarding instance management
    * `project` contains code regarding mods, modpacks, resource packs and shader packs
    * `settings` contains code regarding the Settings page
    * `setup` contains code regarding the setup wizard

Each feature follows
a [layered architecture](https://developer.android.com/topic/architecture/recommendations#layered-architecture) and is
split into 3 submodules:

* The `data` layer contains code regarding application data, whether it is stored locally or fetched from the network
* The `domain` layer contains business logic and acts as an interface between the `data` and `presentation` layers
* The `presentation` layer contains UI and UI logic