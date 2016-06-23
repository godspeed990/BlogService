#!/bin/sh
#sh scriptname 1
docker build -t godspeed990/blog-service .
if [ $? = 0 ]
then
   value=$((8100+$GO_PIPELINE_COUNTER))
   echo "HA-PROXY ENTRY============"
   echo "server server2 10.78.106.176:$value maxconn 32"
   echo "HA-PROXY ENTRY============"
   docker run -e LISTEN_PORT=$value --net=host godspeed990/blog-service &  
else
   echo "Failed to build and deploy the docker container"
   exit 1
fi