XML Windows Event Source for Flume
==================================
Source of Flume NG for tailing files in a directory which has windows event xml format.

Notes
=====
This plugin is based on flume-tail-directory https://github.com/keedio/flume-taildirectory-source  

The module has been refactored in order to decoupled classes and has been created differents listeners.


Compilation
===========
```
mvn package
```

Use
===
Make the directory in flume installation path ```$FLUME_HOME/plugins.d/tail-directory-source/lib``` and copy the file   ```flume-taildirectory-source-1.1.0.jar``` in it.  
Edit flume configuration file with the parameters above.

Configuration
=============
| Property Name | Default | Description |
| ------------- | :-----: | :---------- |
| Channels | - |  |
| Type | - | org.keedio.watchdir.listener.WatchDirXMLWinEventSourceListener |
| dirs | - | list of directoriesto be monitorized comma separated |

* Example
```
flume.sources.r1.type = org.keedio.watchdir.listener.WatchDirXMLWinEventSourceListener
flume.sources.r1.dirs = /tmp/rolmo
```

TO DO:
======

* Include multi thread posibility to parallelize tasks
* Test synchronization between threads
