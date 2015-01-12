# SHR Datasense
To run in local machine

create a symlink for dhis_config to  /opt/datasense/lib/dhis_config/

```
sudo ln -sf <path to projects>/SHR-Datasense/dhis_config /opt/datasense/lib/dhis_config
```

To run :
```
gradle runDatasense
```

To debug :
```
gradle debugDatasense
```