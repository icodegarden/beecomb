#!/bin/bash

VERSION=$1
if [ -z "$VERSION" ]; then
  echo "param version of $1 must not null"
  exit 1
fi

cp target/beecomb-worker-$VERSION.jar target/beecomb-worker.jar
tar -zcvf beecomb-worker-$VERSION.tar.gz bin config target/beecomb-worker.jar