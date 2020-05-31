# BackPack
* **Create backpacks on your minecraft server!**
* **All commands have autocomplete so it's very easy to navigate through them :)**

*To download, go to https://github.com/prosteDeniGC/BackPack/releases*

## Commands

### Player Commands

* **/backpack**
gives you a backpack

* **/backpack retrieve (ID)**
gives you a backpack you own

### Admin Commands

* **/backpack (player)**
gives you a list of backpacks player owns

* **/backpack open (ID)**
opens backpack, works even if the owner is offline

* **/backpack give (player)**
gives player a new backpack

* **/backpack removecontents (ID)**
removes contents of specified backpack

* **/backpack reload**
reloads the config


## Config

* **BackPackCeption: true**
if set to true, you can put backpacks into open backpack. You can never put backpack into itself tho

* **SoundVolume: 1**
values range from 0 to 2, 0 meaning disabled sounds of chest opening/closing, 2 meaning the loudest

* **MaxBackPacks: 20**
when using backpack.make.(number) permission, what is the maximum number it should check for? If you don't know what this is, leaving it at 20 is fine for most purposes

* **OwnerOnly: false**
when set to true, only Owner of the backpack can open it. by default anyone can open anyone's backpacks


## Permissions

* **backpack.make**
this permission allows player to execute **/backpack** command to get backpack

* **backpack.make.(number)**
this permission restricts players to make only this amount of backpacks per player. For example, if all players get permission **backpack.make.3**, they will only be able to execute this command three times.

* **backpack.use**
this permission allows player to open a backpack

* **backpack.retrieve**
this permission allows player to retrieve their (lost) backpack with all of it's contents back

* **backpack.admin**
this permission allows player to circumvent any restrictions and allows him to execute all commands
