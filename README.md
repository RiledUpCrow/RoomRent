# RoomRent

RoomRent is a Bukkit plugin and BetonQuest add-on. It lets you rent WorldGuard
regions to the players through conversations (using events). This way you can
create immersive inns on your server, where players can rent rooms without
using commands or clicking on signs.

If you're not interested in using BetonQuest then this plugin is not for you -
the only way to rent a region is using BetonQuest's events. Please google
"bukkit region rent" to find other plugins.

## Installation

You need to have WorldGuard, WorldEdit and BetonQuest installed in order to
use this plugin. Optionally, you can also install Citizens2, so the NPCs can
lead players to their rented rooms. Once you have those plugins just put
_RoomRent.jar_ into the _plugins_ directory and restart/reload the server.

## Region Sets

In RoomRent, regions are contained in "sets". You can think of those sets as
groups of similar rooms, in the same place or building, for example an inn.
When a player rents a room in this inn, he will get a random free room from
the `inn` set. You can have multiple rooms in each set and multiple sets on
each world (multiple rooms in different inns).

Renting a room is done with `rent` event in BetonQuest plugin. It's your call
what price you will set, what permission you will require etc. That is not a
concern of this plugin. If you use `rent` event when the player already has
a room in the set, it will extend renting time. If the set is full (no more
free rooms) and this event is still fired, an error message will be logged in
the console. You should check if the set has free rooms with `free` condition.
You can also leave the rented region with `leave` event. It will make it free
again. If this event is used when the player doesn't have any rented region in
this set, nothing happens. To check if there
are still free rooms in the set you can use `free` condition - this way your
NPC will be able to tell the player if there are no more rooms. You can also
check if the player has already rented a room with `room` condition and check
how long it is rented with `remaining` condition.

Each room has a sign, which shows who is currenty renting it and for how long.
If the room is free, the sign will show a message "For Rent". Of course these
messages are all customizable in the config.

Adding regions to sets is done with `/room add <set> <region>` command.
You have to look at the sign while issuing this command, so the plugin can
associate it with the region. You don't have to "create" sets. Just add a 
region to them, they will be automatically created for you. All regions and
signs in a set must be on the same world.

A player can have only one room in each set, but it's up to you to disable
having rooms in different sets if you want that. Just use conditions. The
status of the player after renting a region is "member", not "owner".

## BetonQuest instruction strings

`rent` event and `remaining` condition have two required arguments.
First is the name of the set, and second is amount of minutes.
`leave` event, `free` and `room` condition have only one argument,
name of the set. 

**Examples:**

* `rent inn 1440` - rents a room in the "inn" set for one day
* `leave inn` - leaves a room in the "inn" set
* `room inn` - checks if the player has rented a room in the "inn" set
* `free inn` - checks if there is still a free room in the "inn" set
* `remaining inn 10080` - checks if the remaining time of the room rented
  in "inn" set is greater than a week

## Configuration of regions

WorldGuard regions must be configured correctly so the rooms can work as
intended. You can read the complete flag documentation
[here](http://docs.enginehub.org/manual/worldguard/latest/regions/flags/).

Usually you will want to have a bigger region for the hole building (e.g.
an inn), which will contain all room regions. You can set whatever flags you
want there, including allowing chest access and using things. This region
should also have `block-break` and `block-place` set to deny, because
members of rooms would be able to destroy them otherwise.

On the rooms you will need to use
`/rg flag <region> -g nonmembers interact deny` command. It will
prevent all non-members from using anything inside rented region. Members will
be able to do everything they can do in the inn, so if interaction is blocked
there you will have to allow it separately with `-g members` option.

If you have any problems with using WorldGuard flags please read the docs
carefully. It's all explained there.

## Citizens NPCs and showing the way to the room

If you have installed Citizens2 plugin, you will be able to use special `show`
event, which will make the NPC walk to the rented region sign, leading the
player to their room (just like in Skyrim).

The syntax of this event's instruction string looks like this:

    show inn 0 &2follow_me &2here's_your_room

* First argument (here "inn") is the name of the set. If the player does not
  have a room rented in this set, there will be an error message logged in the
  console.
* Second argument is a numeric ID of the NPC. You can get it by
  selecting the NPC and typing `/npc`.
* Third argument is a text which will appear when the NPC starts walking. It
  will convert all `_` characters to spaces (because it cannot contain spaces).
* Fourth argument is a text which will appear once the NPC is near the sign.

## Compiling the plugin

You need JDK 1.7 and Maven 3. Download the source and issue `mvn install`
command inside the root directory. The compiled .jar will appear in _target_
directory.

## License

The plugin is licensed under GPLv3 (or above).