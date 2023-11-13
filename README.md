# Build Events (Fabric)

This server-side mod allows you to create build events to track the amount of block placements and breaks within an area using scoreboard objectives.

## Details
When a new build event is added, one or two objectives are created.

`/buildevents add foo 3 1 4 1 5 9 both` will create two objectives: `foo_place` and `foo_break`.

You can change how an objective's name is displayed on the sidebar using `/scoreboard objectives modify foo_place displayname <plaintext or json>`.

If an objective named `foo_place` exists prior to creating the `foo` event, then it will be used instead of creating a new one.

## Syntax
`/buildevents add <eventName> <from> <to> (place|break|both) [in] <dimension>`

`/buildevents remove <eventName> [remove_objectives]`

`/buildevents (pause|unpause) <eventName>`

`/buildevents list [active|paused]`

## Usage
`/buildevents add` to create events. Optionally, end with `in <dimension>` to track actions in the specified dimension instead of the one the executor's in.

`/buildevents remove` to remove events. Optionally, end with `remove_objectives` to also remove the objectives.

`/buildevents pause` to stop tracking actions until re-enabled with `/buildevents unpause`.

`/buildevents list` to view all existing events. Optionally, end with `active` or `paused` to view those specifically.

## Compiling
* Clone the repository
* Open a command prompt/terminal to the repository directory
* Run 'gradlew build'
* The built jar file will be in build/libs/
