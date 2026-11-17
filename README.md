# 🏥 Hospital Management System (HMS)

A full-stack, web-based **Hospital Management System** designed to streamline hospital operations by integrating patient management, doctor scheduling, appointments, electronic medical records, laboratory services, pharmacy inventory, billing, staff administration, and reporting into one centralized platform.

The system aims to reduce manual administrative work, improve operational efficiency, and provide secure, organized access to hospital information.

## 🚀 Technology Stack

### Frontend
- React.js
- JavaScript
- HTML5 & CSS3
- Vite
- REST API Integration

### Backend
- Java
- Spring Boot
- Spring Data JPA / Hibernate
- Spring Security
- RESTful APIs
- Maven

### Database
- MySQL
- Aiven Cloud MySQL

### Deployment & DevOps
- Vercel – Frontend Hosting
- Render – Backend Hosting
- Aiven – Cloud Database
- Docker – Backend Containerization
- Git & GitHub – Version Control
- GitHub Actions – Workflow Automation

---

## ✨ Key Features

### 🔐 1. User Management
- User authentication and logout
- Role-based access control
- Password management
- User account administration
- Audit logging of system activities

### 👥 2. Patient Management
- Register new patients
- Update patient information
- Search and view patient records
- Manage patient contact and emergency details
- View patient medical history
- Manage patient-related documents

### 👨‍⚕️ 3. Doctor Management
- Register and manage doctors
- Assign doctors to departments
- Manage doctor availability and schedules
- Maintain doctor specialization and licence details

### 📅 4. Appointment Management
- Schedule patient appointments
- Select doctors based on availability
- Manage appointment dates and time slots
- Track appointment statuses
- Cancel and reschedule appointments
- Calendar-based appointment visualization

### 🏥 5. Admission Management
- Register inpatient admissions
- Assign wards and beds
- Track admission and discharge dates
- Manage admission types
- Monitor admission statuses

### 📋 6. Electronic Medical Records
- Create and manage patient medical records
- Record diagnoses and treatments
- Maintain prescription information
- Track inpatient and outpatient visits
- Store clinical reports and treatment history

### 🧪 7. Laboratory Management
- Register laboratory test requests
- Track sample collection
- Manage test processing statuses
- Record laboratory results
- Maintain reference ranges and reports

### 💊 8. Pharmacy Management
- Register medicines and pharmacy products
- Manage medicine inventory
- Monitor stock quantities and reorder levels
- Track medicine expiry dates
- Record medicine dispensing
- Record supplier stock receipts
- Perform inventory adjustments
- Maintain stock movement history
- Track pharmacy sales

### 💳 9. Billing and Payment Management
- Generate patient bills
- Calculate consultation charges
- Include laboratory, pharmacy, and admission charges
- Track outstanding payments
- Record cash, card, bank transfer, and insurance payments
- Maintain payment references and transaction history

### 👨‍💼 10. Staff Management
- Register hospital employees
- Assign employees to departments
- Manage employee attendance
- Record check-in and check-out times
- Manage employee leave requests
- Track leave approval statuses

### 📊 11. Reports and Analytics
- Patient reports
- Appointment reports
- Revenue and billing reports
- Pharmacy inventory reports
- Laboratory reports
- Staff and attendance reports

### 🛡️ 12. Audit Trail
- Record important system activities
- Track user actions
- Maintain activity timestamps
- Support administrative monitoring and accountability

---

## 📸 System Screenshots

Explore the main interfaces and functionalities of the Hospital Management System.

> **Note:** The images below are linked to the `screenshots/` directory. Add screenshots of your actual HMS using the filenames provided.

### 🏠 Dashboard

The main dashboard provides an overview of hospital operations, including important statistics, summaries, and quick access to system modules.

<img width="1906" height="867" alt="Screenshot (919)" src="https://github.com/user-attachments/assets/283a1ae9-759c-442f-94b5-7ee46dd15a06" />


### 👥 Patient Management

Register patients, manage personal information, search patient records, and access medical history.

<img width="1920" height="868" alt="Screenshot (920)" src="https://github.com/user-attachments/assets/9069373a-14ac-4307-a038-9c02c95c4fc6" />
<img width="1920" height="865" alt="Screenshot (930)" src="https://github.com/user-attachments/assets/289565bd-fcab-49d4-aa4d-6fc296ae525a" />

### 📅 Appointment Management

Schedule appointments, select available doctors, manage time slots, and track appointment statuses.

<img width="1906" height="865" alt="Screenshot (923)" src="https://github.com/user-attachments/assets/547c392b-87d6-406c-b3a3-90a1e1727e7d" />

### 💳 Billing and Payments

Generate patient bills, calculate hospital charges, record payments, and monitor payment statuses.

<img width="1920" height="864" alt="Screenshot (925)" src="https://github.com/user-attachments/assets/a3efc2e3-a82d-4e06-8adf-19f5459ddccc" />

📊 Reports & Analytics

Provides a centralized view of hospital performance through interactive dashboards, statistical summaries, and visual reports.

<img width="1920" height="866" alt="Screenshot (926)" src="https://github.com/user-attachments/assets/283aacec-8794-4a82-857f-358e847924d5" />

---

## 🏗️ System Architecture

The application follows a layered architecture.

```text
              ┌─────────────────────┐
              │   React Frontend    │
              │       Vercel        │
              └──────────┬──────────┘
                         │
                    REST APIs
                         │
              ┌──────────▼──────────┐
              │ Spring Boot Backend │
              │       Render        │
              └──────────┬──────────┘
                         │
                    Spring Data JPA
                         │
              ┌──────────▼──────────┐
              │   MySQL Database    │
              │       Aiven         │
              └─────────────────────┘
```

---

## 📁 Project Structure

```text
Hospital-Management-System/
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml
│   └── Dockerfile
│
├── .github/
│   └── workflows/
│
└── README.md
```

*This structure illustrates the main project components.*

---

## ⚙️ Installation and Setup

### Prerequisites

Ensure the following tools are installed:

- Java JDK
- Node.js and npm
- MySQL Server
- Maven
- Git
- Visual Studio Code or IntelliJ IDEA

### 1. Clone the Repository

```bash
git clone https://github.com/ruwinya10/Hospital-Management-System.git
```

Navigate to the project directory:

```bash
cd Hospital-Management-System
```

### 2. Configure the Database

Create a MySQL database:

```sql
CREATE DATABASE hms;
```

Configure the backend database connection using environment variables.

### 3. Run the Backend

Navigate to the backend directory:

```bash
cd backend
```

Start the Spring Boot application:

```bash
mvn spring-boot:run
```

The backend normally runs at:

```text
http://localhost:8080
```

### 4. Run the Frontend

Open another terminal and navigate to the frontend directory:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

Open the application:

```text
http://localhost:5173
```

---

## ☁️ Cloud Deployment

The application uses separate cloud services for its frontend, backend, and database.

| Component | Technology | Hosting |
|-----------|------------|---------|
| Frontend | React + Vite | Vercel |
| Backend | Java + Spring Boot | Render |
| Database | MySQL | Aiven |
| Containerization | Docker | Render |
| Version Control | Git | GitHub |

### Frontend URL

https://hospital-management-system-bice-two.vercel.app/

---

## 🔒 Security

The system is designed with security considerations such as:

- User authentication
- Role-based authorization
- Password hashing
- Protected backend API endpoints
- Audit logging
- Environment-based secret configuration
- Secure database connectivity

Access to sensitive patient and medical information should be restricted to authorized personnel.

---

## 🎯 Project Objectives

- Digitize and centralize hospital operations.
- Reduce paperwork and manual data entry.
- Improve patient registration and appointment scheduling.
- Maintain organized electronic medical records.
- Enhance pharmacy inventory tracking.
- Simplify hospital billing and payment processing.
- Improve employee and administrative management.
- Support reporting and data-driven decision-making.
- Improve accountability through audit trails.

---

## 📄 Disclaimer

This project is developed for educational and demonstration purposes.

Any sample patient information, medical records, prescriptions, billing details, or other healthcare data used for testing should be fictional.

The application should undergo appropriate security, privacy, clinical workflow, and reliability assessments before use in a real healthcare environment.

---

⭐ If you find this project useful, consider giving the repository a star!
