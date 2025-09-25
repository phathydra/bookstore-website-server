@echo off

cd accounts
start cmd /k mvn spring-boot:run
cd ..

cd books
start cmd /k mvn spring-boot:run
cd ..

cd orders
start cmd /k mvn spring-boot:run
cd ..

cd chatbot
start cmd /k mvn spring-boot:run
cd ..

exit

