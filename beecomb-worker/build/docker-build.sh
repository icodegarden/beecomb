#!/bin/bash

VERSION=$1
echo "target version:$VERSION"
sudo docker build --build-arg VERSION="${VERSION}" -t icodegarden/beecomb-worker-"${VERSION}" .

#sudo docker login
#sudo docker push icodegarden/beecomb-worker:"${VERSION}"