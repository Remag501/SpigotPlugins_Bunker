# SpigotPlugins_Bunker
Spigot bunker plugin customized for a Minecraft server and developed by Remag501.

The bunker plugin allows players to visit a bunker within their world via multiverse core. Bunkers are created ahead of time by the admin to avoid lag. When a user buys a bunker, they are assigned a bunker file instead of creating a new world on the spot. 

This plugin uses the /bunker command with arguments to work
- /bunker or /bunker home to visit your bunker
- /bunker buy to purchase a bunker and have it assigned to you
- /bunker visit [player] to visit a player's bunker
* For admins only, be careful using this command as it can create lag*
- /bunker admin add [bunkerCount] to increase the total available bunkers on the server. More bunkers are more resources/lag.

Dependencies are handled with Maven and are expected to be provided:
- WorldEdit
- Multiverse-Core
- voidGen (Optional/Compiled)

Future Plans V1.2:
- optimize bunker admin add, by running each world each asynchronously *Done*
- visit player confirmation *Done, not yet customizable with config*
- world customization (peaceful, adventure, etc..) *Done, not yet customizable with config*
- Case insensitive player visitation and bunker data *Done*
- WorldGuard V1.3? (optional) *May be omitted*
- bunker admin update (automatically updates bunkers without manually removing and adding)
- bunker admin reset (removes the bunkers along the worlds and npcs. Comes with confirmation)
