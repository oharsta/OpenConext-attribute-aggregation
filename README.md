# OpenConext-attribute-aggregation

[![Build Status](https://travis-ci.org/OpenConext/OpenConext-attribute-aggregation.svg)](https://travis-ci.org/OpenConext/OpenConext-attribute-aggregation)
[![codecov.io](https://codecov.io/github/OpenConext/OpenConext-attribute-aggregation/coverage.svg)](https://codecov.io/github/OpenConext/OpenConext-attribute-aggregation)

OpenConext Attribute Aggregation

## Getting started

### System Requirements

- Java 8
- Maven 3
- MySQL 5.5
- node.js

### Create database

Connect to your local mysql database: `mysql -uroot`

Execute the following:

```sql
CREATE DATABASE aaserver DEFAULT CHARACTER SET latin1;
grant all on aaserver.* to 'root'@'localhost';
```

## Building and running

### The aa-server

This project uses Spring Boot and Maven. To run locally, type:

`cd aa-server`

`mvn spring-boot:run -Drun.jvmArguments="-Dspring.profiles.active=dev"`

When developing, it's convenient to just execute the applications main-method, which is in [Application](aa-server/src/main/java/aa/Application.java). Don't forget
to set the active profile to dev.

### The aa-gui

The client is build with react.js and to get initially started:

`cd aa-gui`

`brew install npm;`

To run locally:

`npm start`

Browse to the [application homepage](http://localhost:8000/).

When new npm dependencies are added:

`npm install`

## Miscellaneous

### Testing

There are integration tests that test against a running Spring boot AA server.

When testing the aggregations in the GUI Playground you have to provide input attributes for retrieving values from the authorities.

For VOOT use `urn:collab:person:example.com:admin`.

For SAB use `urn:collab:person:surfnet.nl:henny`.

For ORCID use `jstiglitz@harvard-example.edu` or `oharsta@surfguest.nl`.

Note that you can register your own ORCID at orcid.test.surfconext.nl

### cUrl

The unsecured ServiceProviderConfig endpoint:

`curl -ik https://aa.test.surfconext.nl/aa/api/v2/ServiceProviderConfig -H "Content-Type: application/json"`

For the SCIM endpoint for the SP where we need client credentials we can obtain an access_token from the authz server:

`curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -u https@//aa.test.surfconext.nl:secret -d "grant_type=client_credentials&scope=attribute-aggregation" https://authz.test.surfconext.nl/oauth/token`

Use the access token to call the ResourceType and Schema endpoint (do not forget to replace ${access_token} with the atual obtained access_token

`curl -ik -H "Authorization: Bearer ${access_token}" -H "Content-Type: application/json" https://aa.test.surfconext.nl/aa/api/v2/ResourceType`
`curl -ik -H "Authorization: Bearer ${access_token}" -H "Content-Type: application/json" https://aa.test.surfconext.nl/aa/api/v2/Schema`

To mimic the behaviour of attribute aggregation for an internal client - e.g. EngineBlock - we need to post form data:

`curl -X POST -H "Content-Type: application/json" --data-binary @./aa-server/src/test/resources/json/eb/request.json -u eb:secret https://aa.test.surfconext.nl/aa/api/attribute/aggregate`

### New Attribute Authority

New Attribute Authorities first must be added and configured in `attributeAuthoritiesProductionTemplate.yml`. Then add the new authority implementation to `AttributeAggregatorConfiguration#attributeAggregatorById`.

To actually use the new authority in the test/acc/prod environment it also needs to be configured in OpenConext-deploy [attributeAuthorities.yml.j2](https://github.com/OpenConext/OpenConext-deploy/blob/master/roles/aa/templates/attributeAuthorities.yml.j2).

Do not to forget to write Integration tests. For an example see [OrcidAttributeAggregatorTest](aa-server/src/test/java/aa/aggregators/orcid/OrcidAttributeAggregatorTest.java)

### Service Registry

The aa-server needs to access the metadata of Service providers from the Service Registry. In production modus the content is read (and periodically refreshed) from:
  
* https://tools.surfconext.nl/export/saml20-sp-remote.json

In any other modus the content is read from the file system:

* [saml20-sp-remote.json](aa-server/src/main/resources/service-registry/saml20-sp-remote.json)

To sync the data of the file system with the actual production data of `https://tools.surfconext.nl` run the [refreshEntityMetadata](aa-server/scripts/refreshEntityMetadata.sh) script.

### Configuration and deployment

On its classpath, the application has an [application.properties](aa-server/src/main/resources/application.properties) file that
contains configuration defaults that are convenient when developing.

When the application actually gets deployed to a meaningful platform, it is pre-provisioned with ansible and the application.properties depends on
environment specific properties in the group_vars. See the project OpenConext-deploy and the role aa for more information.

For details, see the [Spring Boot manual](http://docs.spring.io/spring-boot/docs/1.2.1.RELEASE/reference/htmlsingle/).
