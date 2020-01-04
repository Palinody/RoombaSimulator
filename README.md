# RoombaSimulator

## Features
A Roomba (vacuum cleaner) simulator made to train agents in a 2D environment with the following features.

Agents: 
* 2 IR sensors (2 float)
* 1 fixed thermal camera (4 float)
* 2 bumpers (2 boolean)
* 4 action visualizers that help keeping track of the action beeing performed (forward, turn left, turn right, backward)

Agents do not collide with each other.

Environement:
* Walls (lines) generated at specified positions
* Walls (lines), generated randomly, that do not cross agents
* A heat source that the thermal camera can detect

Utils:
* Save simulation states as png

## Preview
![test161](https://user-images.githubusercontent.com/32341154/71769455-f17abb00-2f21-11ea-8247-f877d3d10b85.png)
