mvn -f ../../pom.xml clean package

rm ../logs.txt
./docker-compose-linux -f ../docker-compose.yml stop
./docker-compose-linux -f ../docker-compose.yml up --no-start --no-recreate
./docker-compose-linux -f ../docker-compose.yml start
./docker-compose-linux -f ../docker-compose.yml logs -f --tail=0  >> ../logs.txt &
sleep 5