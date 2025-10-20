**Are you still comparing coordinates to find your friends in game?<br>Are you tired of searching ravines for the last two remaining raiders?<br>Is there a command block somewhere that you can't seem to find?**<br>
Don't be frustrated, EchoTrace has got you covered!

\- [configuration](https://github.com/Mitality/EchoTrace/blob/master/src/main/resources/config.yml) -

**Examples:**
|                    command / action                   |      description / explanation     |
|:-----------------------------------------------------:|:----------------------------------:|
|                    `/trace cancel`                    |      cancel all active traces      |
|                 `/trace player Notch`                 |      find players named Notch      |
|               `/trace position 0 100 0`               |       trace absolute position      |
|               `/trace position ~3 ~5 ~7`              |       trace relative position      |
|           `/trace entity @e[type=#raiders]`           |     find all remaining raiders     |
|         `/trace block redstone_wire[power=0]`         |    find unpowered redstone wires   |
|       `/trace block repeating_command_block 10`       | scan a 10 block radius for targets |
| `/trace entity @e[type=!player,sort=nearest,limit=1]` | find the nearest non-player entity |

*on default settings, only the player running the command can see and hear the resulting trace(s)*
