XML Windows Event Source for Flume
==================================
Source of Flume NG for tailing xml files in a directory. Directories to monitor can be configured and flume will be responsible for monitoring these directories recursively waiting for **only new** files.
The XML files must be well formed. You should specify the xml tag and level in the schema from which event to inyect to channel is condidered. The source is responsible for injecting into the flume channel.


Notes
=====
This plugin is based on flume-tail-directory https://github.com/keedio/flume-taildirectory-source  

The module has been refactored in order to decoupled classes.


Compilation
===========
```
mvn clean package
```

Use
===
Make the directory in flume installation path ```$FLUME_HOME/plugins.d/xmlwinevent/lib``` and copy the file   ```xmlwineventsource-{version}.jar``` in it.  
Edit flume configuration file with the parameters above.

Configuration
=============
| Property Name | Default | Description |
| ------------- | :-----: | :---------- |
| Channels | - |  |
| Type | - | org.keedio.watchdir.listener.WatchDirXMLWinEventSourceListener |
| dirs.1.dir | - | directory to be monitorized (only one) |
| dirs.1.whitelist | - | regex pattern indicating whitelist files to be monitorized (ex. \\.xml) |
| dirs.1.blacklist | - | regex pattern indicating blacklist files to be excluded (ex. \\.xml) |
| dirs.1.tag | - | tag of th event (ex. Event) |
| dirs.1.taglevel | - | level of the tag for nested tags in the XML (ex. 2) |
| dirs.2.dir | - | second directory configuration... |
| dirs.2.whitelist | - | ... |
| dirs.2.blacklist | - | ... |
| dirs.2.tag | - | ... |
| dirs.2.taglevel | - | ... |
| whitelist | - | regex pattern indicating whitelist files to be monitorized (ex. \\.xml). If it is set it will rewrite the directory one |
| blacklist | - | regex pattern indicating blacklist files to be excluded (ex. \\.xml). If it is set it will rewrite the directory one |
| maxworkers | - | max number of workers to parse the xml files. Use for performance improvements |
| buffersize | - | Size for the buffer inputstream.  |
|readonstartup|false|Used in order to indicate if the agent have to proccess files existing in the directory on startup|
|suffix|.finished|Once a file is proccessed it will be renamed to file{suffix}|

* Example
```
...
flume.sources.r1.type = org.keedio.watchdir.listener.WatchDirXMLWinEventSourceListener
flume.sources.r1.dirs.1.dir = /tmp/dir1
flume.sources.r1.dirs.1.blacklist =
flume.sources.r1.dirs.1.whitelist = \\.xml
flume.sources.r1.dirs.1.taglevel = 1
flume.sources.r1.dirs.1.tag = Event
flume.sources.r1.dirs.2.dir = /tmp/dir2
flume.sources.r1.dirs.2.blacklist =
flume.sources.r1.dirs.2.whitelist = \\.xml,\\.wxml
flume.sources.r1.dirs.2.tag = Obj
flume.sources.r1.dirs.2.taglevel = 2
flume.sources.r1.blacklist =
flume.sources.r1.whitelist =
flume.sources.r1.maxworkers = 10
flume.sources.r1.buffersize = 1024
flume.sources.r1.readonstartup = true
flume.sources.r1.suffix = .completed
...
```

TEST
====

There is a test plan available on https://drive.google.com/open?id=1WqIOZU5z-iOvCEfbtAgdOGyJ0DrEpumrjcMSY-ktZXQ&authuser=0

TODO
====

-
