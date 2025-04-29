# Secure Banking System - Java Socket Programming

![Java](https://img.shields.io/badge/Java-21-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange)
![Socket Programming](https://img.shields.io/badge/Network-Socket_Programming-green)

A secure client-server banking system implementing core banking operations with database integration and encrypted communication.

🏦 **Banking Operations**
- User registration & login
- Account management
- Deposit/Withdraw funds
- Balance inquiry
- Transaction history
- Multi-client support

💾 **Database Integration**
- MySQL relational database
- Persistent data storage
- Transaction records
- Account relationship management

## Prerequisites

- Java JDK 21+
- XAMPP (for MySQL)
- MySQL Connector/J 8.0+
- jBCrypt library

## Installation

1. **Database Setup**
   - Start XAMPP MySQL server
   - Create database using `schema.sql`:
   ```bash
   mysql -u root -p < schema.sql


## Output

1. **Register new user**
   - REGISTER alice_123 password_here
   - SUCCESS: Registration successful
   ```bash
   REGISTER alice_123 password_here
   
2. **LOGIN new user**
   - LOGIN alice_123 password_here
   - SUCCESS: Login successful
   ```bash
   LOGIN alice_123 password_here
