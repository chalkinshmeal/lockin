# Description
lockin is a Minecraft mini-game plugin based on SmallAnt's lockin challenge.

Each team races to complete a series of tasks within a time limit. Once a task is completed, it is locked out so that no other team can complete the task. The team with the most points at the end of the time limit wins. In the event of a tie, a sudden death round is held to determine the winner. Tasks are chosen randomly from a programmed list, limited by the `maxTaskCount` field.

# Commands
- `/lockin compass` - Gives a lockin compass, used to specify teams and look at tasks
- `/lockin help` - Shows a help message
- `/lockin start` - Starts a lockin game
- `/lockin stop` - Stops a lockin game
- `/lockin team <team name> <material name>` - Creates a team

# Config
A `config.yml` file is auto-generated if there is none. Global and task-specific settings can be found here.  Some tasks may not have a config entry.

- **`maxTaskCount`**:
Task-specific settings may have the field `maxTaskCount`. This the maximum amount of tasks, for the task type, that may appear in a single lockin challenge. For example, the below specifies a `maxTaskCount` of 3, meaning at most there can be 3 `breakItemsTask` tasks in the challenge at once.
```
# Task: Break an item. Possible items to break, and how many to break
breakItemsTask:
  maxTaskCount: 3
  materials:
    ACACIA_LOG: 10
    ACACIA_LEAVES: 64
    AMETHYST_BLOCK: 3
    ANDESITE: 32
    ANDESITE_WALL: 1
    ...
```

# Q&A/Issues
If you find any issues, please file a GitHub issue.

# Credit
- SmallAnt, for the idea: https://www.youtube.com/@Smallant
- mr_stretch, for initial implementation ideas: https://www.spigotmc.org/resources/lockin.112607/
- Lobster13, for task ideas + testing
