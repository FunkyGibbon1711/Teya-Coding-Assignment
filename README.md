# Simple Ledger API

My simple ledger application that provides APIs for recording monetary transactions, viewing the current balance, and
retrieving transaction history.
The project is built using Java 25 and Spring Boot 4.0.7

## Overview

The API provides 3 endpoints for performing its functionality:

- **GET** - `/api/balance` (fetching current account balance)
- **GET** - `/api/transactions` (fetching transaction history)
- **POST** - `/api/transactions` (making a transaction)

All data is stored in the application's memory and does not require any external database or additional software to run.

## Getting Started

I have fixed the Spring application to run on port `5555`. Below I will include some example requests you can perform to
test its functionality

### Getting current balance

```curl -X GET http://localhost:5555/api/balance``` -
This will return the current balance of the account

`curl -X GET http://localhost:5555/api/transactions` - This will return the transaction history for the account.

```
curl -X POST http://localhost:5555/api/transactions -H "txnId:<your-id-here>" -H "Content-Type:application/json"
-d @src/test/resources/reviewertestdata/exampledeposit.json
```

This will take a JSON file containing a request body (pre-written for your use) and send it to the application.

## Assumptions

Before I explain any further about my design considerations, I am going to explain the assumptions I made in
designing this application:

- The system manages a single ledger/account
- The starting balance is zero
- Transaction identifiers are provided automatically in requests (more generally the assumption being that in reality
  requests would be forwarded from an asynchronous message queue that generates its own unique identifiers)
- Transaction history is returned in the order transactions were performed
- Data is not persisted between application restarts
- There is no consideration of authentication or authorisation as instructed

## Design Considerations

### Layered Architecture

I designed this application using a standard Spring Boot architecture layout: separately splitting each component
functionally and logically into different classes. For this simple solution, these are:

- Controller layer
- Service layer
- Model/DTO layer
- Exception handling layer

### Validation Rules

You may notice in my integration tests that I have a number of simulated Exceptions not actually thrown by my
application. The purpose of these Exceptions are simply an exercise to show I am considering potential sources of error
and my ability to handle them appropriately e.g. database errors, conflicting requests (idempotency failures), etc.
Additionally, I added custom validation logic to requests to ensure proper sanitation of inbound requests. All errors
are handled with a global `ApiExceptionHandler` class, with appropriate HTTP Response Codes built in.

### Concurrency Considerations

Due to the scope of the exercise, there was no in-built transaction handling or atomic operations in data handling. I
have considered this by making my `performTransaction` method `synchronized`, allowing Spring to safely handle any
potential concurrency issues with writing transactions records to the same resource concurrently.

## Future Enhancements

For a production-like environment there are a number of potential improvements that could be included:

- Use of a persistent database:
    - Support of Atomic/transactional operations
    - Automated data backups
    - Supports RBAC to prevent user data "leakage"
    - Can include pagination for enhanced performance
- Support for multiple user accounts
- Authentication and authorisation mechanism integration for security purposes
- Audit logging of user operations performed
- Monitoring of key metrics with observability platform integration e.g. Grafana
- Log search functionality for live service support purposes (e.g. using an ELK stack)
- More comprehensive automated testing, integration, and deployment built into the repository itself
- OpenAPI documentation for easier inter-team integration
- Containerisation of application allowing for deployment into Kubernetes clusters as a microservice

## Trade-Off Summary

The primary design goal was to create a solution that is:
-Easy to understand

- Easy to run locally
- Easy to review
- Aligned with the stated requirements

As a result, simplicity was consistently prioritised over scalability and advanced architectural patterns.
