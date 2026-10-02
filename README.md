# 🚗 RoadGuardian

### Smart Road Safety & Vehicle Assistance Platform

RoadGuardian is a desktop-based smart road safety and vehicle assistance application developed using **Java, JavaFX, and Firebase Firestore**.

The platform brings vehicle assistance, emergency support, mechanic services, navigation, vehicle management, and service management together into a single desktop application.

---

## 📌 Overview

RoadGuardian is designed to simplify the process of getting assistance during vehicle-related problems and emergency situations.

The system provides separate interfaces for:

- 👤 Users
- 🔧 Mechanics
- 🛡️ Administrators

Each module provides role-specific functionality while sharing centralized data through **Firebase Firestore**.

---

## ✨ Key Features

### 👤 User Module

- User Registration & Login
- User Dashboard
- Vehicle Management
- Vehicle Information
- AI-assisted Vehicle Diagnosis
- Service Requests
- Mechanic Assistance
- Live Tracking
- Navigation
- Emergency SOS
- Women Safety Assistance
- Tow Truck Assistance
- Service History
- Notifications
- Reviews & Ratings
- Complaints
- Profile & Settings

---

### 🔧 Mechanic Module

- Mechanic Login
- Mechanic Dashboard
- Service Request Management
- Active Jobs
- Job History
- Customer Information
- Navigation
- SOS Request Handling
- Job Status Management

---

### 🛡️ Admin Module

- Admin Dashboard
- Customer Management
- Mechanic Management
- Vehicle Management
- Service Management
- Service Request Management
- SOS Request Management
- Complaint Management
- Notification Management
- Review Management
- Report Generation
- System Settings

---

## 🤖 AI-Assisted Diagnosis

RoadGuardian includes an AI-assisted vehicle diagnosis feature that helps users understand possible vehicle issues based on the information they provide.

The feature is integrated into the user module to provide quick assistance before requesting professional mechanical support.

---

## 🚨 Emergency & Safety

RoadGuardian provides emergency-oriented features including:

- Emergency SOS
- SOS Request Management
- Mechanic Assistance
- Women Safety Support
- Tow Truck Assistance
- Emergency Notifications

These features help users initiate and manage assistance requests through the application.

---

## 🗺️ Navigation & Live Tracking

The application includes navigation and live tracking functionality to support interactions between users and mechanics.

The system supports:

- Location-based assistance
- Mechanic navigation
- User-mechanic coordination
- Service assistance workflows
- Live location-related functionality

---

## 🔥 Firebase & Real-Time Data

Firebase Firestore is used as the application's cloud database and data synchronization layer.

The system manages data related to:

- Users
- Mechanics
- Vehicles
- Services
- Service Requests
- SOS Requests
- Notifications
- Reviews
- Complaints
- Reports

Firebase integration allows different application modules to work with centralized cloud data.

---

## 🏗️ Project Architecture

RoadGuardian follows a modular layered architecture that separates application logic, controllers, data access, models, services, and user interface components.

```text
RoadGuardian
│
├── app
│   ├── AppNavigator
│   ├── RoadGuardianApp
│   ├── SessionManager
│   └── WindowManager
│
├── controller
│   ├── admin
│   ├── mechanic
│   └── user
│
├── dao
│   ├── admin
│   ├── auth
│   ├── mechanic
│   └── user
│
├── exception
│
├── firebase
│
├── model
│
├── service
│
├── ui
│   ├── admin
│   ├── landing
│   ├── mechanic
│   ├── user
│   └── theme
│
└── util
```

---

## 🔄 Application Workflow

```text
                         RoadGuardian
                              │
              ┌───────────────┼───────────────┐
              │               │               │
            User           Mechanic          Admin
              │               │               │
       ┌──────┼──────┐   ┌────┼────┐    ┌─────┼─────┐
       │      │      │   │    │     │    │     │     │
    Vehicle  SOS    AI  Jobs  Nav   SOS Users Services Reports
       │      │      │   │    │     │    │     │     │
       └──────┴──────┴───┴────┴─────┴────┴─────┴─────┘
                              │
                              ▼
                       Firebase Firestore
```

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| ☕ Java | Core application development |
| 🎨 JavaFX | Desktop user interface |
| 🔥 Firebase Firestore | Cloud database and data synchronization |
| 🔐 Firebase Admin SDK | Firebase integration |
| 📦 Maven | Dependency and project management |
| 🤖 Gemini API | AI-assisted diagnosis |
| 🐙 Git | Version control |
| 🌐 GitHub | Source code hosting |

---

## ▶️ How to Run

### Prerequisites

Make sure the following are installed:

- Java JDK 17 or later
- Maven
- Firebase configuration
- Internet connection

### Clone the Repository

```bash
git clone https://github.com/ganeshtupe-18/RoadGuardian.git
```

### Navigate to the Project

```bash
cd RoadGuardian
```

### Build the Project

```bash
mvn clean install
```

### Run the Application

```bash
mvn javafx:run
```

---

## 🔐 Firebase Configuration

Firebase credentials are intentionally not included in this public repository.

Before running the application, configure the Firebase credentials locally according to the project's Firebase configuration.

> ⚠️ **Important:** Never commit private Firebase service-account credentials, passwords, private keys, or other sensitive credentials to a public repository.

---

## 📁 Project Structure

```text
RoadGuardian/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── project/
│       │       ├── app/
│       │       ├── controller/
│       │       │   ├── admin/
│       │       │   ├── mechanic/
│       │       │   └── user/
│       │       ├── dao/
│       │       │   ├── admin/
│       │       │   ├── auth/
│       │       │   ├── mechanic/
│       │       │   └── user/
│       │       ├── exception/
│       │       ├── firebase/
│       │       ├── model/
│       │       ├── service/
│       │       ├── ui/
│       │       │   ├── admin/
│       │       │   ├── landing/
│       │       │   ├── mechanic/
│       │       │   ├── user/
│       │       │   └── theme/
│       │       └── util/
│       │
│       └── resources/
│
├── pom.xml
├── .gitignore
├── FIREBASE_CUSTOMER_FIX.md
└── README.md
```

---

## 🚀 Future Scope

Possible future improvements include:

- Advanced AI-assisted vehicle diagnosis
- Predictive vehicle maintenance
- Improved real-time location tracking
- Enhanced navigation features
- Connected vehicle integration
- Additional emergency service integrations
- Mobile companion application
- Advanced analytics and reporting

---

## 📌 Project Status

**Active Development**

RoadGuardian is continuously being improved with new functionality, UI enhancements, navigation features, and system integrations.

---

## 👨‍💻 Developer

### Ganesh Tupe

**B.E. Computer Engineering**

Interested in:

- ☕ Java
- 🧩 Data Structures & Algorithms
- 💻 Software Development
- 🧠 Problem Solving
- 🤖 Artificial Intelligence

---

## 🔗 Repository

**GitHub:**  
https://github.com/ganeshtupe-18/RoadGuardian

---

## ⭐ Project

If you find RoadGuardian interesting, feel free to explore the source code and project structure.

⭐ Star the repository if you find the project useful or interesting.