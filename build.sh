#!/bin/bash

ES_NAME=kifshare
ES_DIRNAME=${ES_NAME}-build
ES_TARNAME=${ES_DIRNAME}.tar.gz

echo "Copying in init.d script"
cp ${ES_NAME} ${ES_DIRNAME}

echo "Copying in builds"
cp -r target/ ${ES_DIRNAME}

echo "Copying in config"
cp -r conf/ ${ES_DIRNAME}

echo "Copying in the build directory, which contains the resources"
cp -r build/ ${ES_DIRNAME}

echo "Creating tarball."
tar czf ${ES_TARNAME} ${ES_DIRNAME} 

echo "Copying spec file to build tree."
cp ${ES_NAME}.spec /usr/src/redhat/SPECS/

echo "Copying tarball to SOURCES."
cp ${ES_NAME} /usr/src/redhat/SOURCES/

echo "Running rpmbuild..."
rpmbuild -ba /usr/src/redhat/SPECS/${ES_NAME}.spec

echo "Copying back RPMs..."
cp /usr/src/redhat/RPMS/noarch/${ES_NAME}*.rpm .

echo "Deleting RPMs from build tree."
rm /usr/src/redhat/RPMS/noarch/${ES_NAME}*.rpm

echo "Deleting spec file from build tree."
rm /usr/src/redhat/SPECS/${ES_NAME}.spec

echo "Deleting tarball from build tree."
rm /usr/src/redhat/SOURCES/${ES_TARNAME}

echo "Deleting buildroot"
rm -rf /usr/src/redhat/BUILDS/${ES_DIRNAME}

