# TaskVisualizer
The goal of this app is to visualize your daily routine and determine whether you accomplish your daily task goals.

## Installation
This repo comes with a .jar and .app (MacOSX) for the GUI, but depends on Taskwarrior and Timewarrior to be installed as well as a hook script.

### Taskwarrior & Timewarrior
Follow the installation instructions for your platform:
* https://taskwarrior.org/download/
* https://timewarrior.net/docs/install/

### on-modify-timewarrior.py
This hook records tasks when starting a task via Taskwarrior e.g. `task 1 start` & `task 1 done`.

You might need to edit this file to change the full path to your own specific `timew` command location.
