# Build Events (Fabric)

This server-side mod allows you to create build events to track the amount of block placements and breaks within an area using scoreboard objectives.

## Details
When a new build event is added, one or two objectives are created.

`/buildevents add foo 3 1 4 1 5 9 both` will create two objectives: `foo_place` and `foo_break`.

You can change how an objective's name is displayed on the sidebar using `/scoreboard objectives modify foo_place displayname <plaintext or json>`.

If an objective named `foo_place` exists prior to creating the `foo` event, then it will be used instead of creating a new one.

## Usage
`/buildevents add ...` to create events. Optionally, end with `in <dimension>` to track actions in the specified dimension or with `in !!global` for every dimension, instead of the one the executor is in.

`/buildevents remove ...` to remove events. Optionally, end with `remove_objectives` to also remove the objectives.

`/buildevents set <eventName> predicate ...` to attach a [predicate](https://minecraft.wiki/w/Predicate), in order to conditionally track actions. [Here](https://misode.github.io/predicate/?share=FWh0Z0tvRO) is an example for tracking specific blocks.

`/buildevents set <eventName> predicate` to remove an event's predicate.

`/buildevents set <eventName> dimension ...` to change an event's dimension or make it global(every dimension).

`/buildevents set <eventName> total ...` to toggle displaying the total amount of actions on the sidebar for each of the two actions.

`/buildevents pause ...` to stop tracking actions until re-enabled with `/buildevents unpause ...`.

`/buildevents list` to view all existing events. Optionally, end with `active` or `paused` to view those specifically.

## [Syntax](https://minecraft.wiki/w/Commands#Syntax)
`/buildevents add <eventName> <from> <to> (place|break|both) [in] (!!global|<dimension>)`

`/buildevents remove <eventName> [remove_objectives]`

`/buildevents set <eventName> predicate [<predicate>]`

`/buildevents set <eventName> dimension (!!global|<dimension>)`

`/buildevents set <eventName> total (true|false)`

`/buildevents (pause|unpause) <eventName>`

`/buildevents list [active|paused]`

## Compiling
* Clone the repository
* Open a command prompt/terminal to the repository directory
* Run 'gradlew build'
* The built jar file will be in build/libs/
