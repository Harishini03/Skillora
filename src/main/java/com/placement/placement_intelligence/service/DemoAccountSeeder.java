package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.model.AuthProvider;
import com.placement.placement_intelligence.model.CodingProblem;
import com.placement.placement_intelligence.model.Department;
import com.placement.placement_intelligence.model.PlacementStatus;
import com.placement.placement_intelligence.model.ProblemTestCase;
import com.placement.placement_intelligence.model.Role;
import com.placement.placement_intelligence.model.StaffProfile;
import com.placement.placement_intelligence.model.Student;
import com.placement.placement_intelligence.model.User;
import com.placement.placement_intelligence.repository.CodingProblemRepository;
import com.placement.placement_intelligence.repository.DepartmentRepository;
import com.placement.placement_intelligence.repository.ProblemTestCaseRepository;
import com.placement.placement_intelligence.repository.StaffProfileRepository;
import com.placement.placement_intelligence.repository.StudentRepository;
import com.placement.placement_intelligence.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Profile("!test")
public class DemoAccountSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "Skillora@123";

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodingProblemRepository codingProblemRepository;
    private final ProblemTestCaseRepository problemTestCaseRepository;

    public DemoAccountSeeder(UserRepository userRepository,
                             StudentRepository studentRepository,
                             DepartmentRepository departmentRepository,
                             StaffProfileRepository staffProfileRepository,
                             PasswordEncoder passwordEncoder,
                             CodingProblemRepository codingProblemRepository,
                             ProblemTestCaseRepository problemTestCaseRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.codingProblemRepository = codingProblemRepository;
        this.problemTestCaseRepository = problemTestCaseRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Department department = resolveDepartment();

        User studentUser = ensureUser("student.demo", "student.demo@skillora.com", "Student Demo", Role.STUDENT);
        ensureStudentProfile(studentUser, department);

        User recruiterUser = ensureUser("recruiter.demo", "recruiter.demo@skillora.com", "Recruiter Demo", Role.RECRUITER);
        ensureStaffProfile(recruiterUser, department);

        User staffUser = ensureUser("staff.demo", "staff.demo@skillora.com", "Staff Demo", Role.STAFF);
        ensureStaffProfile(staffUser, department);

        seedCodingProblems();
    }

    // ============= CODING PROBLEMS SEEDER =============

    private void seedCodingProblems() {
        if (codingProblemRepository.count() > 0) {
            return; // Already seeded
        }

        User creator = userRepository.findByUsername("staffadmin")
                .or(() -> userRepository.findByEmail("staffadmin@skillora.com"))
                .orElse(null);

        if (creator == null) {
            // Fall back to any STAFF user
            creator = userRepository.findByRoleOrderByNameAsc(Role.STAFF)
                    .stream().findFirst().orElse(null);
        }
        if (creator == null) {
            return; // No suitable creator found — skip seeding
        }

        // 1. Two Sum
        CodingProblem p1 = saveProblem(creator, "Two Sum", "EASY", "Arrays",
                "Given an array `nums` and an integer `target`, return the indices of the two numbers that add up to `target`.\n" +
                "Assume exactly one solution exists. You may not use the same element twice.\n\n" +
                "Input format:\n  Line 1: N (size of array)\n  Line 2: N space-separated integers\n  Line 3: target\n\n" +
                "Output: two space-separated 0-based indices.");
        addTestCase(p1, "4\n2 7 11 15\n9", "0 1", true, 1);
        addTestCase(p1, "3\n3 2 4\n6", "1 2", true, 2);

        // 2. Reverse a String
        CodingProblem p2 = saveProblem(creator, "Reverse a String", "EASY", "Strings",
                "Given a string, print its reverse.\n\nInput: a single string\nOutput: the reversed string.");
        addTestCase(p2, "hello", "olleh", true, 1);
        addTestCase(p2, "abcde", "edcba", true, 2);

        // 3. Palindrome Check
        CodingProblem p3 = saveProblem(creator, "Palindrome Check", "EASY", "Strings",
                "Given a string, determine if it is a palindrome.\n" +
                "Print \"YES\" if it is a palindrome, \"NO\" otherwise.\n\n" +
                "Input: a single string\nOutput: YES or NO");
        addTestCase(p3, "racecar", "YES", true, 1);
        addTestCase(p3, "hello", "NO", true, 2);

        // 4. Fibonacci Nth Term
        CodingProblem p4 = saveProblem(creator, "Fibonacci Nth Term", "EASY", "Dynamic Programming",
                "Print the Nth Fibonacci number (0-indexed: F(0)=0, F(1)=1, F(2)=1, ...).\n\n" +
                "Input: a single integer N\nOutput: the Nth Fibonacci number");
        addTestCase(p4, "10", "55", true, 1);
        addTestCase(p4, "5", "5", true, 2);

        // 5. Find Maximum in Array
        CodingProblem p5 = saveProblem(creator, "Find Maximum in Array", "EASY", "Arrays",
                "Given N numbers, print the maximum value.\n\n" +
                "Input format:\n  Line 1: N (number of elements)\n  Line 2: N space-separated integers\n\n" +
                "Output: the maximum element");
        addTestCase(p5, "5\n3 1 4 1 5", "5", true, 1);
        addTestCase(p5, "4\n10 20 30 25", "30", true, 2);

        // 6. Binary Search
        CodingProblem p6 = saveProblem(creator, "Binary Search", "EASY", "Searching",
                "Given a sorted array and a target value, return the 0-based index of the target. " +
                "If the target is not found, return -1.\n\n" +
                "Input format:\n  Line 1: N (size of array)\n  Line 2: N sorted space-separated integers\n  Line 3: target\n\n" +
                "Output: index of target or -1");
        addTestCase(p6, "6\n1 3 5 7 9 11\n7", "3", true, 1);
        addTestCase(p6, "4\n1 2 3 4\n5", "-1", true, 2);

        // 7. Valid Parentheses
        CodingProblem p7 = saveProblem(creator, "Valid Parentheses", "MEDIUM", "Stacks",
                "Given a string containing only the characters '(', ')', '{', '}', '[' and ']', " +
                "determine if the input string is valid.\n\n" +
                "A string is valid if:\n" +
                "  1. Open brackets are closed by the same type of brackets.\n" +
                "  2. Open brackets are closed in the correct order.\n\n" +
                "Print \"YES\" if valid, \"NO\" otherwise.");
        addTestCase(p7, "()[]{}",  "YES", true, 1);
        addTestCase(p7, "([)]",    "NO",  true, 2);

        // 8. Merge Two Sorted Arrays
        CodingProblem p8 = saveProblem(creator, "Merge Two Sorted Arrays", "MEDIUM", "Arrays",
                "Given two sorted arrays, merge them into a single sorted array and print it space-separated.\n\n" +
                "Input format:\n  Line 1: M N (sizes of the two arrays)\n  Line 2: M space-separated integers (first array)\n" +
                "  Line 3: N space-separated integers (second array)\n\n" +
                "Output: merged sorted array, space-separated");
        addTestCase(p8, "3 3\n1 3 5\n2 4 6", "1 2 3 4 5 6", true, 1);
        addTestCase(p8, "2 2\n1 4\n2 3",     "1 2 3 4",     true, 2);

        // 9. Linked List Cycle Detection
        CodingProblem p9 = saveProblem(creator, "Linked List Cycle Detection", "MEDIUM", "Linked Lists",
                "Which algorithm detects a cycle in a linked list in O(1) extra space?\n\n" +
                "This is a conceptual problem. Floyd's Cycle Detection Algorithm (also called the Tortoise and Hare algorithm) " +
                "uses two pointers moving at different speeds to detect a cycle in O(n) time and O(1) space.\n\n" +
                "For the coding challenge: given a sequence of next-pointers (0-indexed node numbers, -1 = null), " +
                "determine if a cycle exists. Print the algorithm name if a cycle is found.\n\n" +
                "Input: 1 (placeholder)\nOutput: Floyd's Tortoise and Hare");
        addTestCase(p9, "1", "Floyd's Tortoise and Hare", true, 1);
        addTestCase(p9, "1", "Floyd's Tortoise and Hare", true, 2);

        // 10. Maximum Subarray (Kadane's Algorithm)
        CodingProblem p10 = saveProblem(creator, "Maximum Subarray (Kadane's Algorithm)", "MEDIUM", "Dynamic Programming",
                "Find the contiguous subarray within an array (containing at least one number) that has the largest sum.\n\n" +
                "Input format:\n  Line 1: N (size of array)\n  Line 2: N space-separated integers\n\n" +
                "Output: the largest sum");
        addTestCase(p10, "8\n-2 1 -3 4 -1 2 1 -5", "6",  true, 1);
        addTestCase(p10, "5\n1 2 3 4 5",             "15", true, 2);

        // 11. Count Inversions
        CodingProblem p11 = saveProblem(creator, "Count Inversions", "MEDIUM", "Sorting",
                "Count the number of inversions in an array. A pair (i, j) is an inversion if i < j but arr[i] > arr[j].\n\n" +
                "Input format:\n  Line 1: N (size of array)\n  Line 2: N space-separated integers\n\n" +
                "Output: number of inversions");
        addTestCase(p11, "5\n2 4 1 3 5", "3", true, 1);
        addTestCase(p11, "3\n3 2 1",     "3", true, 2);

        // 12. Longest Common Subsequence
        CodingProblem p12 = saveProblem(creator, "Longest Common Subsequence", "HARD", "Dynamic Programming",
                "Find the length of the Longest Common Subsequence (LCS) of two strings.\n\n" +
                "A subsequence is a sequence that appears in the same relative order, but not necessarily contiguous.\n\n" +
                "Input format:\n  Line 1: first string\n  Line 2: second string\n\n" +
                "Output: length of the LCS");
        addTestCase(p12, "ABCBDAB\nBDCABA", "4", true, 1);
        addTestCase(p12, "ABC\nAC",         "2", true, 2);

        // 13. 0/1 Knapsack
        CodingProblem p13 = saveProblem(creator, "0/1 Knapsack", "HARD", "Dynamic Programming",
                "Given a knapsack of capacity W and N items each with a weight and a value, " +
                "find the maximum total value that fits in the knapsack. Each item can be taken at most once.\n\n" +
                "Input format:\n  Line 1: W N (capacity and number of items)\n" +
                "  Line 2: N space-separated weights\n  Line 3: N space-separated values\n\n" +
                "Output: maximum value achievable");
        addTestCase(p13, "6 3\n2 3 4\n3 4 5", "7", true, 1);
        addTestCase(p13, "4 3\n1 2 3\n1 4 5", "9", true, 2);

        // 14. Detect Cycle in Directed Graph
        CodingProblem p14 = saveProblem(creator, "Detect Cycle in Directed Graph", "HARD", "Graphs",
                "Given a directed graph with N nodes (0-indexed) and E directed edges, detect if a cycle exists.\n" +
                "Print \"YES\" if a cycle exists, \"NO\" otherwise.\n\n" +
                "Input format:\n  Line 1: N E (number of nodes and edges)\n  Lines 2..(E+1): each line has two integers u v representing a directed edge from u to v\n\n" +
                "Output: YES or NO");
        addTestCase(p14, "4 4\n0 1\n1 2\n2 3\n3 1", "YES", true, 1);
        addTestCase(p14, "3 2\n0 1\n1 2",            "NO",  true, 2);

        // 15. Word Break Problem
        CodingProblem p15 = saveProblem(creator, "Word Break Problem", "HARD", "Dynamic Programming",
                "Given a string s and a dictionary of words, determine if s can be segmented into a space-separated " +
                "sequence of one or more dictionary words.\n\n" +
                "Print \"YES\" if segmentation is possible, \"NO\" otherwise.\n\n" +
                "Input format:\n  Line 1: the string s\n  Line 2: K (number of dictionary words)\n" +
                "  Line 3: K space-separated dictionary words\n\n" +
                "Output: YES or NO");
        addTestCase(p15, "leetcode\n2\nleet code",             "YES", true, 1);
        addTestCase(p15, "catsandog\n3\ncats cat and sand dog", "NO",  true, 2);
    }

    private CodingProblem saveProblem(User creator, String title, String difficultyLevel,
                                      String topicTags, String description) {
        CodingProblem problem = new CodingProblem();
        problem.setTitle(title);
        problem.setDifficultyLevel(difficultyLevel);
        problem.setTopicTags(topicTags);
        problem.setDescription(description);
        problem.setTimeLimitSeconds(5);
        problem.setMemoryLimitMb(256);
        problem.setCreatedBy(creator);
        return codingProblemRepository.save(problem);
    }

    private void addTestCase(CodingProblem problem, String inputData, String expectedOutput,
                             boolean isSample, int orderIndex) {
        ProblemTestCase testCase = new ProblemTestCase();
        testCase.setProblem(problem);
        testCase.setInputData(inputData);
        testCase.setExpectedOutput(expectedOutput);
        testCase.setIsSample(isSample);
        testCase.setOrderIndex(orderIndex);
        problemTestCaseRepository.save(testCase);
    }

    // ============= DEMO ACCOUNTS =============

    private Department resolveDepartment() {
        Optional<Department> byName = departmentRepository.findByNameIgnoreCase("Computer Science");
        if (byName.isPresent()) {
            return byName.get();
        }
        return departmentRepository.save(new Department("Computer Science"));
    }

    private User ensureUser(String username, String email, String name, Role role) {
        User existing = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(email))
                .orElse(null);
        if (existing != null) {
            if (!passwordEncoder.matches(DEMO_PASSWORD, existing.getPasswordHash())) {
                existing.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
            }
            existing.setRole(role);
            existing.setName(name);
            existing.setEmail(email);
            existing.setAuthProvider(AuthProvider.LOCAL);
            existing.setActive(true);
            return userRepository.save(existing);
        }

        User created = new User();
        created.setUsername(username);
        created.setEmail(email);
        created.setName(name);
        created.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
        created.setRole(role);
        created.setAuthProvider(AuthProvider.LOCAL);
        created.setActive(true);
        return userRepository.save(created);
    }

    private void ensureStudentProfile(User user, Department department) {
        Student existing = studentRepository.findByUser_Id(user.getId());
        if (existing != null) {
            if (existing.getDepartment() == null) {
                existing.setDepartment(department);
            }
            if (existing.getName() == null || existing.getName().isBlank()) {
                existing.setName(user.getName());
            }
            if (existing.getCgpa() == null) {
                existing.setCgpa(8.0);
            }
            if (existing.getReadinessScore() == null) {
                existing.setReadinessScore(55.0);
            }
            if (existing.getPlacementStatus() == null) {
                existing.setPlacementStatus(PlacementStatus.PENDING);
            }
            studentRepository.save(existing);
            return;
        }

        Student student = new Student();
        student.setUser(user);
        student.setName(user.getName());
        student.setDepartment(department);
        student.setCgpa(8.0);
        student.setLevel("Intermediate");
        student.setInterests("DSA, Backend, Aptitude");
        student.setAptitudeScore(58.0);
        student.setDsaScore(61.0);
        student.setSoftSkillScore(64.0);
        student.setReadinessScore(61.0);
        student.setPlacementStatus(PlacementStatus.PENDING);
        studentRepository.save(student);
    }

    private void ensureStaffProfile(User user, Department department) {
        if (staffProfileRepository.findByUser_Id(user.getId()).isPresent()) {
            return;
        }
        StaffProfile profile = new StaffProfile();
        profile.setUser(user);
        profile.setDepartment(department);
        staffProfileRepository.save(profile);
    }
}
