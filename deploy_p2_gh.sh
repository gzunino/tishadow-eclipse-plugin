#!/bin/sh
echo "PULLING CHANGES"
git pull

echo "----- BUILDING ----- "
mvn install

echo "----- PULLING LAST P2 SITE CHANGES ----- "
cd ../gh-pages
git pull

echo "----- COPYING NEW PLUGINS ----- "
rm -rf ./p2/*
git rm -r p2/*

mkdir -p p2
cp -r ../tishadow-eclipse-plugin/com.belatrixsf.tishadow.p2/target/repository/* ./p2/

git add *

echo "----- PUSHING P2 ----- "
git status
git commit -m "Upgrade plugins"
git push