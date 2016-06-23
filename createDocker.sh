#!/bin/sh
#sh scriptname 1
docker build -t godspeed990/blog-service .
if [ $? = 0 ]
then
   docker run -it -p $1:8200 godspeed990/blog-service &  
else
   echo "Failed to build and deploy the docker container"
   exit 1
fi