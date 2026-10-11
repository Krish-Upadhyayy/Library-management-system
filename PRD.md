# Library Management System — PRD

## 1. Product Overview

The Library Management System is a Java-based web application designed to manage library books, members, book issuing and returns, borrowing history, and overdue fines. It provides separate dashboards and functionality for administrators and members.

## 2. Problem Statement

Managing a library manually can make it difficult to track available books, member records, borrowing activity, return dates, and overdue fines. This application centralizes these operations in one system and maintains inventory records as books are issued and returned.

## 3. Product Objectives

- Manage books and library member records.
- Provide secure login and role-based access.
- Track book issuing and returns.
- Maintain accurate book availability.
- Display borrowing history for administrators and members.
- Calculate overdue fines.
- Provide a consistent and professional user interface.

## 4. Target Users

### Administrator
- Access the administrator dashboard.
- Add, view, edit, and delete book records.
- Manage member records.
- Issue and process the return of books.
- View overall library borrowing history.

### Member
- Access the member dashboard.
- Browse the book catalogue.
- View currently borrowed books.
- Review personal borrowing history.
- View fine details.

## 5. Functional Requirements

### FR-01: Authentication
Users can log in through a login form. The application maintains the user's session and supports logout.

### FR-02: Role-Based Access
Administrators and members have separate dashboards and permissions. Restricted operations must be protected on the server side.

### FR-03: Book Management
Administrators can manage book records, including title, author, ISBN, category, and copy count.

### FR-04: Member Management
Administrators can manage member information, including user ID, membership ID, phone number, and address.

### FR-05: Book Catalogue
Users can browse book information. Administrators receive management actions, while members have read-only access.

### FR-06: Book Issuing
Administrators can issue books to members. The system records borrowing details and decreases the available-copy count.

### FR-07: Book Returns
Administrators can process book returns using the relevant issue record. The system increases the available-copy count after a return.

### FR-08: Borrowing History
Administrators can view overall borrowing records. Members can view their own borrowing history.

### FR-09: Current Borrowed Books
Members can view their currently active borrowing records.

### FR-10: Fine Calculation
The current project rule calculates fines at ₹5 per overdue day.

### FR-11: Borrowing Limit
The current project rule allows a member to borrow a maximum of five active books.

### FR-12: Due Date
The current project rule sets the due date 14 days after the issue date.

### FR-13: User Feedback
Pages should provide understandable empty states and error messages when records are unavailable or an operation fails.

## 6. Main Application Pages

| Page | Purpose |
|---|---|
| `index.html` | Application landing page |
| `login.html` | User login |
| `admin-dashboard.html` | Administrator dashboard |
| `member-dashboard.html` | Member dashboard |
| `add-book.html` | Add a book |
| `add-member.html` | Add a member |
| `issue-book.html` | Issue a book |
| `return-book.html` | Process a return |
| `/books` | Book catalogue |
| `/members` | Member management |
| `/borrowing-history` | Overall borrowing history |
| `/my-borrowed-books` | Current member borrowings |
| `/my-borrowing-history` | Personal borrowing history |
| `/my-fines` | Member fine details |

## 7. Technology Stack

- **Programming language:** Java
- **Java version:** Java 17
- **Build tool:** Apache Maven
- **Backend:** Java Servlets
- **ORM / database connectivity:** Hibernate and JDBC
- **Database:** MySQL
- **Application server:** Apache Tomcat 10.1.x
- **Frontend:** HTML and CSS
- **Shared styling:** `style.css`
- **IDE:** IntelliJ IDEA
- **Database tool:** MySQL Workbench

## 8. UI/UX Requirements

The interface follows a modern, professional visual direction.

- Navy-blue sidebar navigation.
- White content cards and surfaces.
- Blue accent colours.
- Consistent buttons, forms, and tables.
- Clear page headings and navigation.
- Responsive layout for different screen sizes.
- Consistent styling across the main application pages.

## 9. Business Rules

1. A member may have up to five active borrowed books.
2. The due date is 14 days after the issue date.
3. The fine rate is ₹5 per overdue day.
4. Issuing a book decreases available inventory.
5. Returning a book increases available inventory.
6. Administrative functions must be protected by server-side authorization checks.

## 10. Non-Functional Requirements

- **Usability:** Clear navigation, readable text, and helpful feedback.
- **Maintainability:** Maven-based project structure and shared CSS.
- **Security:** Protect credentials and restrict access to authorized users.
- **Data integrity:** Keep borrowing records and inventory counts consistent.
- **Compatibility:** Support the configured Java, Tomcat, and MySQL environment.
- **Responsive design:** Keep pages usable on desktop and smaller screens.

## 11. Acceptance Criteria

The project is considered ready for delivery when:

- Users can log in and access the appropriate dashboard.
- Administrator and member permissions are enforced.
- Book and member management workflows work correctly.
- Issuing and returning books updates inventory correctly.
- Borrowing history is available to the appropriate users.
- Fine details follow the configured calculation rule.
- The main pages use consistent styling.
- The project builds and runs in its configured environment.

## 12. Out of Scope

The following features are not confirmed as part of the current implementation:

- Online fine payments.
- Email or SMS notifications.
- Barcode scanning.
- Multi-branch library management.
- Advanced analytics and reporting.
- Cloud hosting and automated backups.

## 13. Deployment and GitHub Delivery

The immediate delivery goal is to publish the completed project to GitHub.

Before publishing:
- Exclude generated build output such as `target/`.
- Exclude unnecessary IDE metadata.
- Check that database passwords and other secrets are not committed.
- Review the files before staging and committing.
- Push to the intended GitHub repository without overwriting existing work.

The project directory contains `pom.xml`, `src`, and `target`. At the last check, Git reported that the project directory was not yet a Git repository.

## 14. Document Scope Note

This PRD records the requirements and project details discussed so far. Exact implementation details should be checked against the source code before this document is treated as a formal verification report.
