INSERT INTO departments (department_name)
VALUES ('Computer Science'),
       ('Electronics'),
       ('Mechanical'),
       ('Chemical'),
       ('Civil'),
       ('Biotechnology'),
       ('AI & Data Science');

INSERT INTO skills (skill_name)
VALUES ('Java'),
       ('Python'),
       ('DSA'),
       ('SQL'),
       ('Aptitude'),
       ('Communication');

INSERT INTO companies (company_name)
VALUES ('Infosys'),
       ('TCS'),
       ('Wipro'),
       ('Accenture'),
       ('Amazon');

INSERT INTO eligibility_criteria (company_id, min_cgpa, min_dsa, min_aptitude, weight_cgpa, weight_dsa, weight_aptitude,
                                  weight_mock, weight_skill)
VALUES (1, 6.50, 50.00, 50.00, 0.25, 0.25, 0.20, 0.20, 0.10),
       (2, 6.00, 45.00, 45.00, 0.30, 0.20, 0.20, 0.20, 0.10),
       (3, 6.20, 50.00, 40.00, 0.20, 0.25, 0.20, 0.25, 0.10),
       (4, 7.00, 55.00, 50.00, 0.25, 0.30, 0.20, 0.15, 0.10),
       (5, 7.50, 65.00, 60.00, 0.20, 0.35, 0.20, 0.15, 0.10);

INSERT INTO students (name, department_id, cgpa, dsa_score, aptitude_score, mock_test_score, final_score, student_rank,
                      placement_status, level)
VALUES ('Aarav Kumar', 1, 8.50, 72.00, 68.00, 70.00, NULL, NULL, 'PENDING', 'Advanced'),
       ('Diya Sharma', 1, 7.90, 65.00, 71.00, 69.00, NULL, NULL, 'PENDING', 'Intermediate'),
       ('Karthik Raj', 1, 6.80, 55.00, 58.00, 54.00, NULL, NULL, 'PENDING', 'Beginner'),
       ('Meera Iyer', 1, 9.10, 82.00, 76.00, 80.00, NULL, NULL, 'PENDING', 'Advanced'),
       ('Vikram Singh', 1, 7.20, 60.00, 62.00, 61.00, NULL, NULL, 'PENDING', 'Intermediate'),
       ('Ananya Gupta', 1, 8.00, 70.00, 66.00, 68.00, NULL, NULL, 'PENDING', 'Intermediate'),
       ('Rohan Patel', 1, 6.40, 48.00, 52.00, 50.00, NULL, NULL, 'PENDING', 'Beginner'),
       ('Sneha Nair', 1, 7.60, 63.00, 67.00, 64.00, NULL, NULL, 'PENDING', 'Intermediate'),
       ('Arjun Rao', 1, 8.20, 75.00, 73.00, 72.00, NULL, NULL, 'PENDING', 'Advanced'),
       ('Priya Das', 1, 7.10, 58.00, 60.00, 57.00, NULL, NULL, 'PENDING', 'Intermediate'),
       ('Harish Menon', 2, 6.90, 52.00, 55.00, 53.00, NULL, NULL, 'PENDING', 'Beginner'),
       ('Nila Verma', 2, 7.40, 61.00, 64.00, 62.00, NULL, NULL, 'PENDING', 'Intermediate'),
       ('Sanjay Rao', 2, 8.10, 74.00, 69.00, 71.00, NULL, NULL, 'PENDING', 'Advanced'),
       ('Keerthi P', 2, 6.70, 49.00, 51.00, 48.00, NULL, NULL, 'PENDING', 'Beginner'),
       ('Mohan Das', 2, 7.80, 66.00, 70.00, 65.00, NULL, NULL, 'PENDING', 'Intermediate'),
       ('Swathi L', 2, 8.40, 78.00, 74.00, 76.00, NULL, NULL, 'PENDING', 'Advanced'),
       ('Yash T', 2, 6.30, 46.00, 49.00, 45.00, NULL, NULL, 'PENDING', 'Beginner'),
       ('Lavanya K', 2, 7.00, 57.00, 59.00, 56.00, NULL, NULL, 'PENDING', 'Intermediate'),
       ('Rahul N', 2, 8.00, 71.00, 72.00, 70.00, NULL, NULL, 'PENDING', 'Advanced'),
       ('Divya S', 2, 7.20, 60.00, 63.00, 61.00, NULL, NULL, 'PENDING', 'Intermediate'),
       ('Aakash G', 3, 6.50, 50.00, 54.00, 52.00, NULL, NULL, 'PENDING', 'Beginner'),
       ('Isha M', 3, 7.30, 59.00, 61.00, 58.00, NULL, NULL, 'PENDING', 'Intermediate'),
       ('Naveen K', 3, 8.20, 73.00, 68.00, 70.00, NULL, NULL, 'PENDING', 'Advanced'),
       ('Bhavana R', 3, 6.80, 53.00, 56.00, 54.00, NULL, NULL, 'PENDING', 'Beginner'),
       ('Prakash V', 3, 7.60, 64.00, 66.00, 63.00, NULL, NULL, 'PENDING', 'Intermediate'),
       ('Jaya C', 3, 8.50, 79.00, 75.00, 77.00, NULL, NULL, 'PENDING', 'Advanced'),
       ('Suresh H', 3, 6.20, 45.00, 47.00, 44.00, NULL, NULL, 'PENDING', 'Beginner'),
       ('Kavya B', 3, 7.10, 58.00, 60.00, 57.00, NULL, NULL, 'PENDING', 'Intermediate'),
       ('Manoj R', 3, 8.00, 70.00, 69.00, 68.00, NULL, NULL, 'PENDING', 'Advanced'),
       ('Pooja T', 3, 7.40, 62.00, 65.00, 60.00, NULL, NULL, 'PENDING', 'Intermediate');

INSERT INTO student_skills (student_id, skill_id, skill_score)
VALUES (1, 1, 78.00),
       (1, 3, 72.00),
       (2, 1, 70.00),
       (2, 4, 65.00),
       (3, 2, 60.00),
       (3, 5, 58.00),
       (4, 1, 85.00),
       (4, 3, 80.00),
       (5, 2, 62.00),
       (5, 6, 68.00),
       (6, 1, 74.00),
       (6, 4, 69.00),
       (7, 2, 50.00),
       (8, 6, 72.00),
       (9, 3, 77.00),
       (10, 5, 59.00);

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_option, difficulty_level, topic,
                       company_id)
VALUES ('If a train travels 60 km in 1 hour, how far will it travel in 30 minutes?', '15 km', '30 km', '45 km', '60 km',
        'B', 'Easy', 'APTITUDE', NULL),
       ('A can complete a work in 12 days and B in 18 days. How many days together?', '7.2', '7.5', '8', '9', 'A',
        'Medium', 'APTITUDE', NULL),
       ('The average of five numbers is 24. If one number is 30, average of remaining?', '20', '22.5', '23', '24', 'B',
        'Easy', 'APTITUDE', NULL),
       ('A shopkeeper marks 20% above cost and offers 10% discount. Profit?', '8%', '10%', '12%', '15%', 'A', 'Medium',
        'APTITUDE', NULL),
       ('If 3x + 2 = 14, find x.', '2', '3', '4', '5', 'B', 'Easy', 'APTITUDE', NULL),
       ('A car covers 240 km in 4 hours. Speed?', '50', '55', '60', '65', 'C', 'Easy', 'APTITUDE', NULL),
       ('Probability of getting a head in a coin toss?', '1/4', '1/3', '1/2', '2/3', 'C', 'Easy', 'APTITUDE', NULL),
       ('Simple interest on 5000 at 5% for 2 years?', '400', '500', '550', '600', 'B', 'Easy', 'APTITUDE', NULL),
       ('LCM of 6 and 8?', '12', '24', '36', '48', 'B', 'Easy', 'APTITUDE', NULL),
       ('If 25% of a number is 50, the number is?', '150', '175', '200', '250', 'C', 'Easy', 'APTITUDE', NULL),
       ('Find the next number: 2, 6, 12, 20, ?', '28', '30', '32', '36', 'B', 'Medium', 'APTITUDE', NULL),
       ('Ratio of 3:5 is equal to?', '6:10', '9:12', '12:20', '15:25', 'A', 'Easy', 'APTITUDE', NULL),
       ('If 8 workers finish in 15 days, 12 workers finish in?', '8', '9', '10', '12', 'B', 'Medium', 'APTITUDE', NULL),
       ('Time to fill a tank by two pipes together is 6 hours. If one takes 10 hours, other takes?', '12', '15', '20',
        '30', 'C', 'Hard', 'APTITUDE', NULL),
       ('Find the missing term: 5, 10, 20, 40, ?', '60', '70', '80', '90', 'C', 'Easy', 'APTITUDE', NULL),
       ('Which data structure uses LIFO?', 'Queue', 'Stack', 'Heap', 'Tree', 'B', 'Easy', 'DSA', 1),
       ('Time complexity of binary search?', 'O(n)', 'O(log n)', 'O(n log n)', 'O(1)', 'B', 'Easy', 'DSA', 2),
       ('Which traversal is BFS?', 'Preorder', 'Inorder', 'Level order', 'Postorder', 'C', 'Medium', 'DSA', 3),
       ('What is the output size of an adjacency matrix for n nodes?', 'n', 'n^2', '2n', 'n log n', 'B', 'Easy', 'DSA',
        4),
       ('Which structure is used for recursion?', 'Queue', 'Stack', 'Array', 'Graph', 'B', 'Easy', 'DSA', 5),
       ('Best way to handle a conflict in team?', 'Avoid discussion', 'Escalate immediately', 'Discuss calmly', 'Ignore issue', 'C', 'Medium', 'SOFT_SKILLS', NULL),
       ('In HR interview, when asked weakness you should?', 'Say no weakness', 'Share real area with improvement plan', 'Blame team', 'Skip answer', 'B', 'Easy', 'SOFT_SKILLS', NULL),
       ('Effective communication starts with?', 'Speaking loudly', 'Active listening', 'Using jargon', 'Interrupting', 'B', 'Easy', 'SOFT_SKILLS', NULL),
       ('If a customer is upset, first response?', 'Defend yourself', 'Listen and acknowledge', 'End chat', 'Transfer call', 'B', 'Medium', 'SOFT_SKILLS', NULL),
       ('STAR in behavioral interview stands for?', 'Situation Task Action Result', 'Simple Task Action Response', 'Situation Topic Answer Review', 'State Think Act React', 'A', 'Hard', 'SOFT_SKILLS', NULL),
       ('Good email etiquette includes?', 'No subject', 'All caps', 'Clear subject and concise body', 'Slang-heavy tone', 'C', 'Easy', 'SOFT_SKILLS', NULL),
       ('When giving feedback, use?', 'Personal criticism', 'Specific constructive points', 'Public shaming', 'Vague comments', 'B', 'Medium', 'SOFT_SKILLS', NULL),
       ('To improve public speaking, you should?', 'Avoid practice', 'Practice and seek feedback', 'Read from slides always', 'Speak very fast', 'B', 'Easy', 'SOFT_SKILLS', NULL),
       ('Professional response to unknown answer in interview?', 'Guess confidently', 'Admit and explain approach', 'Stay silent', 'Change topic', 'B', 'Medium', 'SOFT_SKILLS', NULL),
       ('Body language during interview should be?', 'Closed posture', 'Avoid eye contact', 'Confident and open', 'Look at phone', 'C', 'Easy', 'SOFT_SKILLS', NULL);

INSERT INTO users (username, email, name, password_hash, role, auth_provider, active, last_login_at)
VALUES ('student1', 'student1@skillora.com', 'Aarav Kumar',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiE7R6POjISiFDUFxIr05oig3NbS1u.', 'STUDENT', 'LOCAL', TRUE, CURRENT_TIMESTAMP),
       ('recruiter1', 'recruiter1@skillora.com', 'Riya Recruiter',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiE7R6POjISiFDUFxIr05oig3NbS1u.', 'RECRUITER', 'LOCAL', TRUE, CURRENT_TIMESTAMP),
       ('staffadmin', 'staffadmin@skillora.com', 'Skillora Admin',
        '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiE7R6POjISiFDUFxIr05oig3NbS1u.', 'STAFF', 'LOCAL', TRUE, CURRENT_TIMESTAMP);

UPDATE students
SET user_id = (SELECT user_id FROM users WHERE username = 'student1')
WHERE student_id = 1;

INSERT INTO staff_profiles (user_id, department_id)
VALUES ((SELECT user_id FROM users WHERE username = 'recruiter1'), 1),
       ((SELECT user_id FROM users WHERE username = 'staffadmin'), 1);

INSERT INTO job_postings (title, description, location, compensation, min_cgpa, required_skills, job_type, active,
                          created_at, recruiter_user_id, department_id, company_id)
VALUES ('Software Engineer Trainee',
        'Entry level role focused on backend development, problem solving, and production support.',
        'Chennai', '5.5 LPA', 6.50, 'Java, SQL, DSA', 'FULL_TIME', TRUE, CURRENT_TIMESTAMP,
        (SELECT user_id FROM users WHERE username = 'recruiter1'), 1, 1),
       ('Data Analyst Intern',
        'Internship opportunity for dashboarding, SQL analysis, and business reporting.',
        'Bengaluru', '25k / month', 6.20, 'Python, SQL, Aptitude', 'INTERNSHIP', TRUE, CURRENT_TIMESTAMP,
        (SELECT user_id FROM users WHERE username = 'recruiter1'), 1, 4);

INSERT INTO job_applications (job_posting_id, student_id, status, applied_at, last_updated_at, recruiter_notes)
VALUES (1, 1, 'APPLIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Strong aptitude performance'),
       (2, 2, 'SHORTLISTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Proceed to interview round');

INSERT INTO interview_schedules (job_application_id, interviewer_user_id, scheduled_at, duration_minutes, mode,
                                 meeting_link, status, created_at)
VALUES (2, (SELECT user_id FROM users WHERE username = 'staffadmin'),
        DATEADD('HOUR', 24, CURRENT_TIMESTAMP), 45, 'ONLINE', 'https://meet.skillora.com/interview/2', 'SCHEDULED',
        CURRENT_TIMESTAMP);

INSERT INTO portal_notifications (user_id, notification_type, message, is_read, created_at)
VALUES ((SELECT user_id FROM users WHERE username = 'student1'), 'WELCOME',
        'Welcome to Skillora. Complete your profile and start applying to jobs.', FALSE, CURRENT_TIMESTAMP),
       ((SELECT user_id FROM users WHERE username = 'recruiter1'), 'WELCOME',
        'Welcome recruiter. Post your first job and build a shortlist.', FALSE, CURRENT_TIMESTAMP),
       ((SELECT user_id FROM users WHERE username = 'staffadmin'), 'WELCOME',
        'Welcome to Skillora. Monitor student progress and manage interviews.', FALSE, CURRENT_TIMESTAMP);
