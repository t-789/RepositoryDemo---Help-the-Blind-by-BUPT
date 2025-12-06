#!/bin/bash

TITLE="title"
CONTENT="content"

echo "Generate data......"
for i in {11..30}
do
  echo "Round ${i}"
  echo "Login 1......"
  curl -X POST http://182.92.78.163:8080/api/users/login \
    -H "Content-Type: application/json" \
    -d '{"username":"api_test_user","password":"password123"}' \
    -c cookie.txt
  for j in {1..3}
  do
    curl -X POST http://182.92.78.163:8080/api/forum/create \
      -H "Content-Type: application/json" \
      -b cookie.txt \
      -d '{"title":"'"$TITLE"${i}_${j}'","content":"'"$CONTENT"${i}_${j}'"}'
    sleep 0.2
  done
  echo "Log out 1"
  curl -X POST http://182.92.78.163:8080/api/users/logout \
    -b cookie.txt
  echo "Login 2......"
  curl -X POST http://182.92.78.163:8080/api/users/login \
    -H "Content-Type: application/json" \
    -d '{"username":"api_test_user2","password":"password123"}' \
    -c cookie2.txt
  for j in {1..3}
  do
    curl -X POST http://182.92.78.163:8080/api/forum/create \
      -H "Content-Type: application/json" \
      -b cookie2.txt \
      -d '{"title":"'"$TITLE"${i}_$((j+3))'","content":"'"$CONTENT"${i}_$((j+3))'"}'
    sleep 0.2
  done
  echo "Log out 2"
  curl -X POST http://182.92.78.163:8080/api/users/logout \
    -b cookie2.txt
  sleep 5
done
echo "Done"

