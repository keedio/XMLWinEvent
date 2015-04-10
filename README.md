XML Windows Event Source for Flume
==================================
Source of Flume NG for tailing files in a directory which has windows event xml format. Directories to monitor can be configured and flume will be responsible for monitoring these directories recursively waiting for **only new** files.
The XML files must be well formed following next schema:

```
<xml ...>
  <Events>
    <Event>
      ....
    </ Event>
    <Event>
      ....
    </ Event>
  </Events>
```

The source is responsible for injecting into the flume channel the each Event section:

```
<Event>
...
</Event>
```

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
Make the directory in flume installation path ```$FLUME_HOME/plugins.d/xmlwinevent/lib``` and copy the file   ```xmlwineventsource-1.0.0.jar``` in it.  
Edit flume configuration file with the parameters above.

Configuration
=============
| Property Name | Default | Description |
| ------------- | :-----: | :---------- |
| Channels | - |  |
| Type | - | org.keedio.watchdir.listener.WatchDirXMLWinEventSourceListener |
| dirs | - | list of directories to be monitorized comma separated |

* Example
```
...
flume.sources.r1.type = org.keedio.watchdir.listener.WatchDirXMLWinEventSourceListener
flume.sources.r1.dirs = /tmp/dir1,/tmp/dir2
...
```

TO DO:
======

* Include multi thread posibility to parallelize tasks
