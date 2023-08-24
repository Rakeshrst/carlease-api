# carlease-api
Car Lease API with IAM

***Design***

1. Plan :
    2. Create 4 API's
        3. IAM
        4. CARLEASE
        5. CUSTOMER
        6. CAR
    7. Create the API Gateway and the Service Discovery Modules

***Implementation Till Now***

1. IAM Service
2. Combined service for Customer Car and Leasing. These will be split into 3

***Left To Do***
1. Splitting into smaller Apis and also separate Databases.
2. Add the Gateway and service Discovery
3. Add BDD testing
4. Add swagger file
5. Add docker-compose to run the whole setup in containers.


**How to Run this application**

1. Make sure you have docker running in your machine. 
2. Go to the folder postgres
   ```cd postgres```
3. Run the docker compose command
```docker-compose up -d```
4. Now go to the projects **iam-service** and **car-lease-api**
5. If you are in an IDE use the terminal or in the root folder of each folder  open a command prompt and run 
    ```mvn spring-boot:run```
6. Now since the applications are running Import **CarLease API.postman_collection.json** to POSTMAN 
7. There are examples for : 
   8. Creating Customer
   9. Creating Car
   10. Creating a Lease Quote 
   11. Confirming it to a contract
   12. Getting a leaseQuotation
   13. Getting a lease contract. 
14. For each request there will be a token needed. For Creation of Car alone only Employee token works for the rest Both the Broker and the Employee token works.



