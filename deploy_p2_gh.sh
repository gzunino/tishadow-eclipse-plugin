#!/bin/sh
echo "PULLING CHANGES"
git pull

echo "----- BUILDING ----- "
mvn install

echo "----- PULLING LAST P2 SITE CHANGES ----- "
rm -rf ../gh-pages
mkdir ../gh-pages
git clone -b gh-pages git@github.com:gzunino/tishadow-eclipse-plugin.git ../gh-pages

echo "----- COPYING NEW PLUGINS ----- "
cd ../gh-pages
rm -rf ./p2/*
git rm -r p2/*

mkdir -p p2
cp -r ../tishadow-eclipse-plugin/com.belatrixsf.tishadow.p2/target/repository/* ./p2/

git add *

echo "----- PUSHING P2 ----- "
git status
git commit -m "Upgrade plugins"
git push
