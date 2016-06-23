#!/bin/sh
#sh scriptname 1
docker build -t godspeed990/blog-service .
if [ $? = 0 ]
then
   value=`cat ./lastPort`
   echo "Currently used port is $value"
   value=$((value+1))
   echo $value > ./lastPort
   echo "Invoking docker run with $value"
   docker run -e LISTEN_PORT=$value --net=host godspeed990/blog-service &  
else
   echo "Failed to build and deploy the docker container"
   exit 1
fi