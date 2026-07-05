package com.placement.placement_intelligence.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Seeds reference data on first production boot (departments, skills, companies,
 * eligibility criteria, and sample questions).
 *
 * Uses INSERT IGNORE so it is safe to run on every restart — duplicate rows are
 * silently skipped. Only active in the "prod" profile.
 */
@Component
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class ProductionDataSeeder implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("ProductionDataSeeder: seeding reference data...");

        seedDepartments();
        seedSkills();
        seedCompanies();
        seedEligibilityCriteria();
        seedQuestions();

        log.info("ProductionDataSeeder: done.");
    }

    // ── Departments ──────────────────────────────────────────────────────────

    private void seedDepartments() {
        String sql = "INSERT IGNORE INTO departments (department_name) VALUES (?)";
        String[] departments = {
            "Computer Science", "Electronics", "Mechanical",
            "Chemical", "Civil", "Biotechnology", "AI & Data Science"
        };
        for (String dept : departments) {
            jdbc.update(sql, dept);
        }
    }

    // ── Skills ───────────────────────────────────────────────────────────────

    private void seedSkills() {
        String sql = "INSERT IGNORE INTO skills (skill_name) VALUES (?)";
        String[] skills = {"Java", "Python", "DSA", "SQL", "Aptitude", "Communication"};
        for (String skill : skills) {
            jdbc.update(sql, skill);
        }
    }

    // ── Companies ────────────────────────────────────────────────────────────

    private void seedCompanies() {
        String sql = "INSERT IGNORE INTO companies (company_name) VALUES (?)";
        String[] companies = {"Infosys", "TCS", "Wipro", "Accenture", "Amazon"};
        for (String company : companies) {
            jdbc.update(sql, company);
        }
    }

    // ── Eligibility Criteria ─────────────────────────────────────────────────

    private void seedEligibilityCriteria() {
        // Only insert if no criteria exist yet
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM eligibility_criteria", Integer.class);
        if (count != null && count > 0) return;

        String sql = """
            INSERT INTO eligibility_criteria
              (company_id, min_cgpa, min_dsa, min_aptitude,
               weight_cgpa, weight_dsa, weight_aptitude, weight_mock, weight_skill)
            SELECT c.company_id, ?, ?, ?, ?, ?, ?, ?, ?
            FROM companies c WHERE c.company_name = ?
            """;

        Object[][] criteria = {
            {6.50, 50.00, 50.00, 0.25, 0.25, 0.20, 0.20, 0.10, "Infosys"},
            {6.00, 45.00, 45.00, 0.30, 0.20, 0.20, 0.20, 0.10, "TCS"},
            {6.20, 50.00, 40.00, 0.20, 0.25, 0.20, 0.25, 0.10, "Wipro"},
            {7.00, 55.00, 50.00, 0.25, 0.30, 0.20, 0.15, 0.10, "Accenture"},
            {7.50, 65.00, 60.00, 0.20, 0.35, 0.20, 0.15, 0.10, "Amazon"},
        };
        for (Object[] row : criteria) {
            jdbc.update(sql, row);
        }
    }

    // ── Sample Questions ─────────────────────────────────────────────────────

    private void seedQuestions() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM questions", Integer.class);
        if (count != null && count > 0) return;

        String sql = """
            INSERT INTO questions
              (question_text, option_a, option_b, option_c, option_d,
               correct_option, difficulty_level, topic, company_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Object[][] questions = {
            // Aptitude
            {"If a train travels 60 km in 1 hour, how far will it travel in 30 minutes?",
                "15 km", "30 km", "45 km", "60 km", "B", "Easy", "APTITUDE", null},
            {"A can complete a work in 12 days and B in 18 days. How many days together?",
                "7.2", "7.5", "8", "9", "A", "Medium", "APTITUDE", null},
            {"The average of five numbers is 24. If one number is 30, average of remaining?",
                "20", "22.5", "23", "24", "B", "Easy", "APTITUDE", null},
            {"A shopkeeper marks 20% above cost and offers 10% discount. Profit?",
                "8%", "10%", "12%", "15%", "A", "Medium", "APTITUDE", null},
            {"If 3x + 2 = 14, find x.", "2", "3", "4", "5", "B", "Easy", "APTITUDE", null},
            {"A car covers 240 km in 4 hours. Speed?", "50", "55", "60", "65", "C", "Easy", "APTITUDE", null},
            {"Probability of getting a head in a coin toss?",
                "1/4", "1/3", "1/2", "2/3", "C", "Easy", "APTITUDE", null},
            {"Simple interest on 5000 at 5% for 2 years?", "400", "500", "550", "600", "B", "Easy", "APTITUDE", null},
            {"LCM of 6 and 8?", "12", "24", "36", "48", "B", "Easy", "APTITUDE", null},
            {"If 25% of a number is 50, the number is?", "150", "175", "200", "250", "C", "Easy", "APTITUDE", null},
            {"Find the next number: 2, 6, 12, 20, ?", "28", "30", "32", "36", "B", "Medium", "APTITUDE", null},
            {"Ratio of 3:5 is equal to?", "6:10", "9:12", "12:20", "15:25", "A", "Easy", "APTITUDE", null},
            {"If 8 workers finish in 15 days, 12 workers finish in?", "8", "9", "10", "12", "B", "Medium", "APTITUDE", null},
            {"Time to fill a tank by two pipes together is 6 hours. If one takes 10 hours, other takes?",
                "12", "15", "20", "30", "C", "Hard", "APTITUDE", null},
            {"Find the missing term: 5, 10, 20, 40, ?", "60", "70", "80", "90", "C", "Easy", "APTITUDE", null},
            // DSA
            {"Which data structure uses LIFO?", "Queue", "Stack", "Heap", "Tree", "B", "Easy", "DSA", null},
            {"Time complexity of binary search?", "O(n)", "O(log n)", "O(n log n)", "O(1)", "B", "Easy", "DSA", null},
            {"Which traversal is BFS?", "Preorder", "Inorder", "Level order", "Postorder", "C", "Medium", "DSA", null},
            {"Output size of adjacency matrix for n nodes?", "n", "n^2", "2n", "n log n", "B", "Easy", "DSA", null},
            {"Which structure is used for recursion?", "Queue", "Stack", "Array", "Graph", "B", "Easy", "DSA", null},
            // Soft skills
            {"Best way to handle a conflict in team?",
                "Avoid discussion", "Escalate immediately", "Discuss calmly", "Ignore issue", "C", "Medium", "SOFT_SKILLS", null},
            {"In HR interview, when asked weakness you should?",
                "Say no weakness", "Share real area with improvement plan", "Blame team", "Skip answer", "B", "Easy", "SOFT_SKILLS", null},
            {"Effective communication starts with?",
                "Speaking loudly", "Active listening", "Using jargon", "Interrupting", "B", "Easy", "SOFT_SKILLS", null},
            {"If a customer is upset, first response?",
                "Defend yourself", "Listen and acknowledge", "End chat", "Transfer call", "B", "Medium", "SOFT_SKILLS", null},
            {"STAR in behavioral interview stands for?",
                "Situation Task Action Result", "Simple Task Action Response",
                "Situation Topic Answer Review", "State Think Act React", "A", "Hard", "SOFT_SKILLS", null},
            {"Good email etiquette includes?",
                "No subject", "All caps", "Clear subject and concise body", "Slang-heavy tone", "C", "Easy", "SOFT_SKILLS", null},
            {"When giving feedback, use?",
                "Personal criticism", "Specific constructive points", "Public shaming", "Vague comments", "B", "Medium", "SOFT_SKILLS", null},
            {"To improve public speaking, you should?",
                "Avoid practice", "Practice and seek feedback", "Read from slides always", "Speak very fast", "B", "Easy", "SOFT_SKILLS", null},
            {"Professional response to unknown answer in interview?",
                "Guess confidently", "Admit and explain approach", "Stay silent", "Change topic", "B", "Medium", "SOFT_SKILLS", null},
            {"Body language during interview should be?",
                "Closed posture", "Avoid eye contact", "Confident and open", "Look at phone", "C", "Easy", "SOFT_SKILLS", null},
        };

        for (Object[] q : questions) {
            jdbc.update(sql, q);
        }
    }
}
