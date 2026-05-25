# moba-server

For further information, please have a look at https://github.com/papparazzo/moba-docu

## Upgrade

```shell

./gradlew distZip
scp build/distributions/moba-server-[CURRENT_VERSION].zip [DEST_HOST]:
ssh [DEST_HOST]
sudo -s
mv moba-server-[CURRENT_VERSION].zip /opt/moba/server/
cd /opt/moba/server/
unzip moba-server-[CURRENT_VERSION].zip
mv moba-server-[CURRENT_VERSION] [CURRENT_VERSION]
rm current
ln -s [CURRENT_VERSION] current
cp [OLD_VERSION]/bin/config.yaml [CURRENT_VERSION]/bin/
systemctl stop moba-server
systemctl start moba-server
less /var/log/moba/server.log
```
