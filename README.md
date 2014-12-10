### logger-service   [![Build Status](https://travis-ci.org/AtlasOfLivingAustralia/logger-service.svg?branch=master)](https://travis-ci.org/AtlasOfLivingAustralia/logger-service)

### Installation

The logger service is built using Ansible. Scripts reside in the ala-install repository: ansible/logger-standalone.yml and ansible/roles/logger-service.

### Database setup

SQL scripts to create the database schema, stored procedures and initial reference are in stored with the ansible build script in the ala-install repository under ansible/logger-service/files/db.