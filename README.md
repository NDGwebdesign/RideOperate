# RideOperate Plugin

RideOperate is a powerful Minecraft plugin designed for theme park and attraction servers. It allows server operators to create interactive control panels for managing rides and attractions through both in-game interfaces and a Windows application.

## Features

- Create custom control panels with interactive buttons
- Add commands to panel buttons
- Live camera views integration (coming soon)
- Windows application integration
- Permission-based access control
- API support for external applications

## Requirements

- Minecraft Server version 1.20+
- Java 17 or higher

## Installation

1. Download the latest version of RideOperate.jar
2. Place the jar file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin using the config.yml file

## Commands

### Main Command
- `/rp` - Main command for RideOperate (alias: `/rideoperate`)

### Panel Management
- `/createpanel` - Create a new control panel
- `/deletepanel` - Delete an existing panel
- `/panel` - Open a specific panel
- `/panels` - View all available panels
- `/rpaddbutton <panelname> <buttonname> <material>` - Add a new button to a panel
- `/rpdeletebutton <panelName> <buttonName>` - Delete a button from a panel
- `/rpchangeitem <panelName> <itemName> <item>` - Change the item of a panel button
- `/rpsetlore <panelName> <itemName> <lore>` - Set the lore (description) of a panel item

### Command Management
- `/rpaddcommand <panelName> <itemName> <command>` - Add a command to a panel item

### Camera Management
- `/rpcreatecam <camera name> <panel name>` - Set up a camera view for a panel
- `/rpdeletecam <camera name>` - Delete a camera view

### System Commands
- `/rpreload` - Reload the plugin configuration
- `/rphelp` - View all available commands
- `/rpinfo` - Display plugin information
- `/genapikey` - Generate a new API key for external applications

## Permissions

### General Permissions
- `rideoperate.*` - Grants access to all RideOperate commands
- `rideoperate.main` - Access to the main /rp command (default: true)
- `rideoperate.help` - Access to help command (default: true)
- `rideoperate.info` - Access to plugin information (default: true)
- `rideoperate.panels` - Access to view all panels (default: true)
- `rideoperate.openpanel` - Permission to open panels (default: true)

### Administrative Permissions
- `rideoperate.createpanel` - Permission to create panels (default: op)
- `rideoperate.deletepanel` - Permission to delete panels (default: op)
- `rideoperate.reload` - Permission to reload the plugin (default: op)

## External Application Integration

RideOperate supports integration with a Windows application for remote control. The application can be found at [RideOperateApp](https://github.com/dafrijder/RideOperateApp).

### API Configuration
1. Generate an API key using `/genapikey`
2. Configure the API port in config.yml (default: 5555)
3. Use the generated API key to connect the Windows application

## Support

For issues, feature requests, or questions:
1. Create an issue on the GitHub repository
2. Contact the plugin authors: FriendsparkMC, NDG-Webdesign

## Version Information

- Current Version: 2.0.2
- API Version: 1.20
- Authors: FriendsparkMC, NDG-Webdesign
