# Merchant Management System (MMS)

A web-based Merchant Management System developed using **Java, Spring Boot, Thymeleaf, and MySQL** to manage merchant information, payment transactions, settlements, and reports efficiently.

The application provides a centralized platform for monitoring merchant activities and managing payment-related operations through an interactive dashboard.

## Live Website

**Live Demo:** https://mms-merchant-management-system-2.onrender.com/

**GitHub Repository:** https://github.com/Shriya247/MMS-Merchant-Management-System

## Project Overview

The Merchant Management System (MMS) is a Java-based web application designed to simplify merchant-related operations.

It enables administrators to maintain merchant records, monitor payment transactions, manage settlements, and access reports through a user-friendly interface.

## Features

### Dashboard
- Overview of merchant, payment, and settlement records
- Summary statistics and recent transactions
- Easy navigation between modules

### Merchant Management
- Add and manage merchant records
- View merchant information and business categories
- Monitor active and inactive merchant statuses

### Payment Management
- Track merchant payment transactions
- View payment amounts, methods, and statuses
- Filter and inspect payment records

### Settlement Management
- Maintain settlement records
- Track settlement amounts, dates, and statuses
- Associate settlements with merchants and payments

### Reports
- View merchant and payment summaries
- Monitor settlement information
- Access transaction reports and analytics

### Security
- Administrator authentication using Spring Security
- Protected administrative operations
- Environment-variable-based credentials
- CSRF protection

## Technologies Used

| Category | Technology |
|---|---|
| Programming Language | Java |
| Framework | Spring Boot |
| Web Pages | HTML, CSS, JavaScript |
| Template Engine | Thymeleaf |
| Database | MySQL |
| Security | Spring Security |
| Build Tool | Maven |
| Containerization | Docker |
| Deployment | Render |
| Cloud Database | Aiven MySQL |
| Version Control | Git and GitHub |

## Database Structure

The application uses three relational tables:

**1. Merchants**

Stores merchant ID, name, business type, status, and creation date.

**2. Payments**

Stores payment ID, merchant ID, amount, payment method, payment status, and transaction date.

**3. Settlements**

Stores settlement ID, payment ID, merchant ID, settlement amount, date, and settlement status.

### Database Relationships

- One merchant can have multiple payments.
- Each payment belongs to a merchant.
- Each settlement is linked to a payment and a merchant.
- A payment can have at most one settlement.

## Application Modules

1. Dashboard
2. Merchant Management
3. Payment Management
4. Settlement Management
5. Reports
6. Administrator Login

## How to Run Locally

### Prerequisites

- Java JDK compatible with the project
- Maven
- MySQL Server
- Visual Studio Code or another Java IDE

### Clone the Repository

```bash
git clone https://github.com/Shriya247/MMS-Merchant-Management-System.git
cd MMS-Merchant-Management-System
```

### Database Configuration

Create a MySQL database named `mms` and initialize the required tables.

Configure the following environment variables:

```text
DB_URL=jdbc:mysql://localhost:3306/mms
DB_USERNAME=root
DB_PASSWORD=your_database_password

MMS_ADMIN_USERNAME=your_admin_username
MMS_ADMIN_PASSWORD=your_admin_password
```

Replace the placeholders with your own credentials. Never commit passwords to GitHub.

### Run the Application

```bash
mvn spring-boot:run
```

Open the application at:

http://localhost:8080

## Deployment

The application is deployed on **Render** using Docker, with its MySQL database hosted on **Aiven**.

Database connections and administrator credentials are configured using environment variables.

**Live Application:** https://mms-merchant-management-system-2.onrender.com/

## Future Enhancements

- Additional user roles and permissions
- Advanced reporting and analytics
- PDF and Excel report exports
- Email notifications
- Automated testing and monitoring

## Learning Outcomes

This project demonstrates practical knowledge of:

- Java application development
- Spring Boot and MVC architecture
- Database connectivity using MySQL
- Relational database design
- Authentication and application security
- Cloud deployment and configuration

## Author

**Shriya Karthikeyan**

GitHub: https://github.com/Shriya247

---

Developed as an academic Java web application project.
