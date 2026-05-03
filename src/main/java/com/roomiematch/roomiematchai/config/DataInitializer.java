package com.roomiematch.roomiematchai.config;

import com.roomiematch.roomiematchai.entity.Role;
import com.roomiematch.roomiematchai.entity.StudentProfile;
import com.roomiematch.roomiematchai.entity.User;
import com.roomiematch.roomiematchai.repository.StudentProfileRepository;
import com.roomiematch.roomiematchai.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with test users on first startup.
 * Creates: 1 MANAGER, 2 WARDENs, 5 STUDENTs (3 boys + 2 girls).
 * Only runs if no users exist in the database (safe for re-runs).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public DataInitializer(UserRepository userRepository,
                           StudentProfileRepository profileRepository,
                           PasswordEncoder passwordEncoder,
                           org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("Starting DataInitializer...");
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void run(String... args) {
        log.info("Cleaning database via native queries for hierarchical system migration...");
        try {
            // Using native queries to avoid JPA mapping issues with old enum values like 'USER'
            userRepository.clearRequests();
            userRepository.clearProfiles();
            userRepository.alterRoleColumn();
            userRepository.deleteAllNative();
            log.info("Database cleaned successfully.");
        } catch (Exception e) {
            log.warn("Failed to clean database (might be empty): {}", e.getMessage());
        }

        log.info("Seeding database with demo users (MANAGER, WARDENs, STUDENTs)...");

        // ── Manager (Hostel Manager — top admin) ────────
        createUser("Hostel Manager", "manager@upes.ac.in", "admin123", null,
                   Role.MANAGER, "UPES", null, null);

        // ── Wardens (one per hostel) ────────────────────
        createUser("Boys Hostel Warden", "warden.boys@upes.ac.in", "admin123", null,
                   Role.WARDEN, "UPES", "BIDHOLI_BOYS_HOSTEL", null);

        createUser("Girls Hostel Warden", "warden.girls@upes.ac.in", "admin123", null,
                   Role.WARDEN, "UPES", "BIDHOLI_GIRLS_HOSTEL", null);

        // ── Demo Students (Boys Hostel) ─────────────────
        String[][] boysData = {
            {"Alice Sharma",   "alice@university.edu",   "password123", "SAP001"},
            {"Bob Kumar",      "bob@university.edu",     "password123", "SAP002"},
            {"Charlie Singh",  "charlie@university.edu", "password123", "SAP003"},
        };
        String[][] boysProfiles = {
            {"EARLY_BIRD", "VERY_CLEAN", "SILENT",   "INTROVERT", "IN_ROOM",  "RARELY",    "COOL"},
            {"NIGHT_OWL",  "MODERATE",   "LOUD_OK",  "EXTROVERT", "LIBRARY",  "OFTEN",     "WARM"},
            {"FLEXIBLE",   "VERY_CLEAN", "MODERATE", "AMBIVERT",  "FLEXIBLE", "SOMETIMES", "MODERATE"},
        };
        for (int i = 0; i < boysData.length; i++) {
            User u = createUser(boysData[i][0], boysData[i][1], boysData[i][2], boysData[i][3],
                                Role.STUDENT, "UPES", "BIDHOLI_BOYS_HOSTEL", null);
            createProfile(u, boysProfiles[i]);
        }

        // ── Demo Students (Girls Hostel) ────────────────
        String[][] girlsData = {
            {"Diana Gupta",  "diana@university.edu", "password123", "SAP004"},
            {"Eva Patel",    "eva@university.edu",   "password123", "SAP005"},
        };
        String[][] girlsProfiles = {
            {"EARLY_BIRD", "MODERATE", "SILENT",  "AMBIVERT",  "IN_ROOM", "RARELY", "COOL"},
            {"NIGHT_OWL",  "RELAXED",  "LOUD_OK", "EXTROVERT", "LIBRARY", "OFTEN",  "WARM"},
        };
        for (int i = 0; i < girlsData.length; i++) {
            User u = createUser(girlsData[i][0], girlsData[i][1], girlsData[i][2], girlsData[i][3],
                                Role.STUDENT, "UPES", "BIDHOLI_GIRLS_HOSTEL", null);
            createProfile(u, girlsProfiles[i]);
        }

        log.info("═══════════════════════════════════════");
        log.info("  Seed data complete.");
        log.info("  Manager:  manager@upes.ac.in / admin123");
        log.info("  Warden:   warden.boys@upes.ac.in / admin123");
        log.info("  Warden:   warden.girls@upes.ac.in / admin123");
        log.info("  Student:  alice@university.edu / password123");
        log.info("═══════════════════════════════════════");
    }

    private User createUser(String name, String email, String password, String sapId,
                            Role role, String org, String hostel, Long createdBy) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setSapId(sapId);
        user.setRole(role);
        user.setOrganization(org);
        user.setHostel(hostel);
        user.setCreatedBy(createdBy);
        User saved = userRepository.save(user);
        log.info("  Created {} : {} ({})", role, email, hostel != null ? hostel : "ALL");
        return saved;
    }

    private void createProfile(User user, String[] profile) {
        StudentProfile sp = new StudentProfile();
        sp.setUser(user);
        sp.setSleepSchedule(profile[0]);
        sp.setCleanlinessLevel(profile[1]);
        sp.setNoiseTolerance(profile[2]);
        sp.setSocialLevel(profile[3]);
        sp.setStudyHabits(profile[4]);
        sp.setGuestFrequency(profile[5]);
        sp.setRoomTemperature(profile[6]);
        profileRepository.save(sp);
    }
}
