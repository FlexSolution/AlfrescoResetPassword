#!/bin/bash

BASEDIR=$(dirname "$0")
export HOST_ADDRESS=127.0.0.1

function finish {
 docker-compose down
}
trap finish EXIT



docker-compose down -v
docker-compose up -d --build
docker-compose logs -f --tail=0  >> ${BASEDIR}/logs.txt &

if [[  "$1" == "headless" ]]; then

    echo "waiting for the application to be ready"
    while true; do
        curl http://localhost:28080/app 1> /dev/null 2> /dev/null
        exit_code=$?

        if [[ "$exit_code" == "0" ]]; then
            echo "application is ready"
            break
        else
            sleep 0.1
        fi
    done

    echo "running tests"
    npx cypress run --headless --browser chrome | tee ./logs.cypress.txt
    docker-compose down -v
else
    ${BASEDIR}/node_modules/.bin/cypress open &
    tail -f ./logs.txt
fi

