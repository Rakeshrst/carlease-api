# Car Lease API

## _Car Lease API with Identity and Access Management (IAM)_

## Design
Scope : 

Customer
Car
Lease --Quotation -> Contract

Not in Scope: 
Lease Payment -> Monthly
Yearly maintenance Partnership

Metrics: 
Users(Employees/Brokers) - 100 employees - 100 MB max
Customer - 1000/month * 12*5 - 60K for 5 years: Metadata    1KB per customer - 100 MB 
Cars - 50k for 5 years   
-  Metadata 1KB per car - 100MB 
- Picture per car - 5MB - 250GB
Signed Contract : 
- 100 KB - 100K contract - BLOB storage  - 10GB
Scalability Lease per day - 30 - 50 cars/day


## Plan:

- Create 4 APIs
    - IAM
    - CARLEASE
    - CUSTOMER
    - CAR
- Develop the API Gateway and Service Discovery Modules.

##  Implementation Status
- IAM Service:
    - Register : Add a new User preferably application user
        - /api/auth/register
    - Get Token : Login with a valid user and get a token
        - /api/auth/token
    - Validate Token : Login with a user and Validate token sent by the customer
        - /api/auth/introspect
- Car Lease API
  Every request needs to be sent with a Authorization Header
    - Customer: Add, Update, Get, Delete Customer
    - Car: Add, Update, Get, Delete Customer
    - Lease Quote: Using customer,  email car id and other details and get a quote on lease.
    - Lease Contract: Based on the quotation confirm it to a lease contract.

##  To-Do List
1. Split the combined service into smaller APIs and separate databases.
2. Integrate the API Gateway and implement Service Discovery.
3. Implement Behavior-Driven Development (BDD) testing.
4. Include a Swagger file for API documentation.
5. Add a docker-compose file to run the entire setup in containers.

## How to Run the Application
1. Make sure Docker is installed and running on your machine.
2. Navigate to the ***postgres*** folder:
```sh
cd postgres
```
3. Run the following docker-compose command:
```sh
docker-compose up -d
```
4. Move to the directories of iam-service and car-lease-api projects.
5. If you're using an IDE, use the terminal within the project directory; otherwise, open a command prompt at the root of each project and run:
```sh
mvn spring-boot:run
```
7. Import the CarLease API.postman_collection.json into Postman.
   The collection includes examples for:
  - Creating a Customer
  - Creating a Car
  - Creating a Lease Quote
  - Confirming a Lease Quote to a Contract
  - Fetching a Lease Quotation
  - Fetching a Lease Contract
  Each request requires a token. For Car creation, only an Employee token is valid. For the other requests, both Broker and Employee tokens work.
## Dockerization
To create a Docker image for iam-service:
```sh
docker build -t com.rstontherun/iam-service .
```
To create a Docker image for car-lease-api:
```
docker build -t com.rstontherun/car-lease-api .
```

