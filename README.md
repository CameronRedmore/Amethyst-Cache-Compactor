# Amethyst-Cache-Compactor
Amethyst Cache Compactor is a which enables Applied Energistics type storage in Bukkit &amp; Spigot.

## Open-Source?
I've recently open-sourced the plugin due to a lack of time to actually maintain the thing myself.
I'll slowly push things every so often, but not much and mostly just bug fixes.
I'm making this open-source so that other people can potentially help and create their own versions of the plugin.
This should hopefully allow the plugin to continue existence even though I don't have much time for it anymore.

The plugin is licensed under the GNU General Public License v3.0. More details can be found in the [LICENSE](LICENSE) file at the root of the repoistory.

## Re-Writing
Firstly, I'd like to apologise for the absolute mess that is this codebase, it's all very mish-mash and spaghettified.
It uses tons of deprecated methods and the SQL usage is just abusive. In order to fix this, I'm thinking of doing a full re-write of the project,
I have a little more experience in programming now and hopefully this time I can make something that's a little bit less amateur.

If you can help with this process, please let me know!Sending me a message on something like the BukkitDev forums

## Developing
This repo contains a full NetBeans project that is capable of development and building of the plugin.
It's probably possible to convert the project to work in something like Eclipse or IntelliJ IDEA but I presonally use NetBeans for my Java development so that's what this is in.

Your NetBeans will need two libraries adding:
- Spigot, which should just contain a Spigot server jar file created using the Spigot BuildTools
- SensibleToolbox, which should contain a jar file for the SensibleToolbox Bukkit plugin.

## Releases
I aim to match GitHub releases to the releases on the [plugin website](https://www.amethystdevelopment.co.uk/BukkitPlugins/ACC), the [BukkitDev Project](https://dev.bukkit.org/projects/camstorage) and the [Spigot project](https://www.spigotmc.org/resources/camstorage.10850/)
This may not always be the case however, and it is possible that there may be more GitHub releases than in other places for testing and the likes.
