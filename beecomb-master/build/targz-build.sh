#!/bin/bash

VERSION=$1
if [ -z "$VERSION" ]; then
  echo "param version of $1 must not null"
  exit 1
fi

cp ../target/beecomb-master-$VERSION.jar ../target/beecomb-master.jar
tar -zcvf beecomb-master-$VERSION.tar.gz bin config ../target/beecomb-master.jar