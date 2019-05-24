#clear

#docker rm -f kafka
#docker rm -f zookeeper

#rm -rf /Users/javabrain/Documents/Docker/logs/*

#rm -rf /Users/javabrain/Documents/Docker/kafka/logs/*

#docker network create bunit


#docker run -d --name zookeeper -p 2181:2181 -v /home/ec2-user/kafka/ZK_Conf:/opt/zookeeper-3.4.13/conf -v /home/ec2-user/kafka/ZK_Data:/opt/zookeeper-3.4.13/data wurstmeister/zookeeper
docker restart zookeeper
#docker network connect bunit zookeeper

#docker run -d --name kafka -p 9092:9092 -v /var/run/docker.sock:/var/run/docker.sock -v /home/ec2-user/kafka/logs/:/opt/kafka/logs -e KAFKA_ADVERTISED_HOST_NAME=localhost -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 -e KAFKA_AUTO_CREATE_TOPICS_ENABLE=true --link=zookeeper wurstmeister/kafka
docker restart kafka
#docker network connect bunit kafka

docker run -d -p 8081:8081 -v /home/ec2-user/logs:/logs -e HOST_NAME=socat -e kafka.url=kafka:9092 -e AWS_DD=13.126.200.232 --name curious --link=socat --link=consul --link=kafka curious:1.0

docker network connect bunit curious

docker ps
