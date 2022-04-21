#!/bin/bash

VERSION=$1
if [ -z "$VERSION" ]; then
  echo "param version of $1 must not null"
  exit 1
fi
echo "target version:$VERSION"
sudo docker build --build-arg VERSION="${VERSION}" -t icodegarden/beecomb-worker:"${VERSION}" .

#sudo docker login
#sudo docker push icodegarden/beecomb-worker:"${VERSION}"