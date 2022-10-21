# Babun v0.3

Babun is a base code template for microservice in server site development section. 
As SH of server site development would say

> When i read a code, it should looks like it was written by 1 person n not by many people.
> Writing code already hard, reading it and maintaining it would be much harder. Always strive 
> for maintainability.

## Objective

- Standard way of writing code
- Example for those who want to write a maintainable code
- Improve developer productivity
- modular design and code portability 

## Principle

* all user story should be able to be implemented as layering design
    * controller 
    * service
    * data ( repo, finder)
* strive for loose coupling between layer
    * service is a MUST to use interface
* developer will use java config as what Spring recommended
    * bean declaration happen inside *Configuration class
    * component auto scanning will not work and purposely DISABLED
    * bean declaration NEED to have identifier 
* package will be based on domain problem
    * subpackage can be
        * model
        * dto 
        * another sub domain 
    * service, repository will be always belong to package(domain)/sub package(sub domain)
* common package contain things being shared across domain package
    * it is meant to be review by the team in the end of sprint for action item
* unit test at controller and service layer
* developer will test manually the persistence layer
* before PR,
    * all unit test must be green
    * spring boot application able to run
    
## Spring profile

developer and deployment will be using different profile

| profile | stage | description |
| ------- | ----- | ----------- |
| local | development | developer local testing with external sub system (db, jms, etc) setup |
| test | development, ci cd | developer local testing without hitting external sub system ( db, jms, etc) |
| dev | deployment | deployment to development environment | 
| sit | deployment | deployment to SIT environment |
| uat | deployment | deployment to UAT environment | 

**Developer will not concern them self with deployment** 

The profile above need to be invoke as argument during
- application start
- unit test

## definition of done per user story (DOD)

- all unit test must be green
- spring boot application able to run
- review was done by peer
- status reflected in Jira



  

 