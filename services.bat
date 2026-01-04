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

cd shipping
start cmd /k mvn spring-boot:run
cd ..

cd RAG
start cmd /k call rag.py
cd ..

cd recommend
start cmd /k call recommended.bat
cd ..

exit

