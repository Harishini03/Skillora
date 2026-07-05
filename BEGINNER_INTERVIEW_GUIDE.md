# Beginner Interview Guide (Project Quick Notes)

This file helps you explain the backend logic in simple words during interviews.

## 1) Login and Signup Flow (`AuthService`)

- `login()`:
  - Finds user by username or email.
  - Checks password using `PasswordEncoder`.
  - Validates role if role is sent from UI.
  - Updates last login time.
  - Returns JWT token in `AuthResponse`.

- `signup()`:
  - Validates password rules.
  - Checks unique username and email.
  - Creates user in `User` table.
  - Creates profile based on role:
    - `STUDENT` -> saves `Student` and skills
    - other roles -> saves `StaffProfile`
  - Returns JWT token in `AuthResponse`.

- `googleLogin()`:
  - Verifies Google token if provided.
  - If user does not exist, creates account with Google provider.
  - Updates login time and returns JWT token.

Interview line:
`AuthService keeps all auth business logic in one place and controllers only call service methods.`

## 2) Test Flow (`TestService`)

- `startSession()`:
  - Creates a test session with start time, duration, and status `ACTIVE`.

- `recordTestResult()`:
  - Validates session belongs to student and is still active.
  - Loads all answered questions.
  - Calculates score.
  - Converts score to percentage.
  - Updates student score based on test type.
  - Recalculates readiness score.
  - Saves attempt and each answer.

- `calculateReadiness()`:
  - Formula:
    - Aptitude: 35%
    - Coding: 45%
    - Soft skills: 20%

Interview line:
`I separated validation, score calculation, and saving logic into helper methods to keep the code readable and maintainable.`

## 3) Design Pattern Used

- Controller -> Service -> Repository
- Controller handles request/response.
- Service handles business rules.
- Repository handles database queries.

This separation is important to explain in interviews.

## 4) Common Interview Questions You Can Answer

- Why use `@Transactional`?
  - To keep related DB operations in one safe unit of work.

- Why use `PasswordEncoder`?
  - To store secure password hash, not plain text password.

- Why JWT?
  - Stateless authentication; each request carries token.

- Where is validation done?
  - Mostly in service layer before saving data.
