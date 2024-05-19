#!/bin/bash

NEWVERCODE=$(($(cat app/build.gradle.kts | grep versionCode | tr -s ' ' | cut -d " " -f 4 | tr -d '\r')+1))
NEWVERNAME="beta-$NEWVERCODE"

sed -i 's/versionCode.*/versionCode = '$NEWVERCODE'/' app/build.gradle.kts
sed -i 's/versionName =.*/versionName = "'$NEWVERNAME'"/' app/build.gradle.kts

sed -i 's/"version":.*/"version": "'$NEWVERNAME'",/' latestBeta.json
sed -i 's/"versionCode":.*/"versionCode": '$NEWVERCODE',/' latestBeta.json

# module changelog
echo "**$NEWVERNAME**  " > newChangeLog.md
cat changeLog.md >> newChangeLog.md
echo "  " >> newChangeLog.md
cat BetaChangelog.md >> newChangeLog.md
mv  newChangeLog.md BetaChangelog.md

echo "*$NEWVERNAME* released in beta channel  " > telegram.msg
echo "  " >> telegram.msg
echo "*Changelog:*  " >> telegram.msg
cat changeLog.md >> telegram.msg
echo 'TMessage<<EOF' >> $GITHUB_ENV
cat telegram.msg >> $GITHUB_ENV
echo 'EOF' >> $GITHUB_ENV