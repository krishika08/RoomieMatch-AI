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
 * Seeds the database with test users, admin accounts, and profiles on first startup.
 * Only runs if no users exist in the database (safe for re-runs).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StudentProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           StudentProfileRepository profileRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already has users — skipping seed data.");
            return;
        }

        log.info("Seeding database with demo users, admins, and profiles...");

        // ── Admin Accounts ──────────────────────────────
        // Super Admin (manages all hostels in UPES)
        createAdmin("admin@upes.ac.in", "admin123", Role.ADMIN, "UPES", null);

        // Hostel Admins (one per hostel)
        createAdmin("admin.boys@upes.ac.in", "admin123", Role.HOSTEL_ADMIN, "UPES", "BIDHOLI_BOYS_HOSTEL");
        createAdmin("admin.girls@upes.ac.in", "admin123", Role.HOSTEL_ADMIN, "UPES", "BIDHOLI_GIRLS_HOSTEL");

        // ── Demo Users (Boys Hostel) ─────────────────────
        String[][] boysUsers = {
            {"alice@university.edu",   "password123"},
            {"bob@university.edu",     "password123"},
            {"charlie@university.edu", "password123"},
        };
        String[][] boysProfiles = {
            {"EARLY_BIRD", "VERY_CLEAN", "SILENT",     "INTROVERT", "IN_ROOM",  "RARELY",    "COOL"},
            {"NIGHT_OWL",  "MODERATE",   "LOUD_OK",    "EXTROVERT", "LIBRARY",  "OFTEN",     "WARM"},
            {"FLEXIBLE",   "VERY_CLEAN", "MODERATE",   "AMBIVERT",  "FLEXIBLE", "SOMETIMES", "MODERATE"},
        };
        for (int i = 0; i < boysUsers.length; i++) {
            createUserWithProfile(boysUsers[i][0], boysUsers[i][1], "UPES", "BIDHOLI_BOYS_HOSTEL", boysProfiles[i]);
        }

        // ── Demo Users (Girls Hostel) ────────────────────
        String[][] girlsUsers = {
            {"diana@university.edu",   "password123"},
            {"ethan@university.edu",   "password123"},
        };
        String[][] girlsProfiles = {
            {"EARLY_BIRD", "MODERATE",   "SILENT",     "AMBIVERT",  "IN_ROOM",  "RARELY",    "COOL"},
            {"NIGHT_OWL",  "RELAXED",    "LOUD_OK",    "EXTROVERT", "LIBRARY",  "OFTEN",     "WARM"},
        };
        for (int i = 0; i < girlsUsers.length; i++) {
            createUserWithProfile(girlsUsers[i][0], girlsUsers[i][1], "UPES", "BIDHOLI_GIRLS_HOSTEL", girlsProfiles[i]);
        }

        log.info("Seed data complete. Admin logins: admin@upes.ac.in / admin123");
    }

    private void createAdmin(String email, String password, Role role, String org, String hostel) {
        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(role);
        admin.setOrganization(org);
        admin.setHostel(hostel);
        userRepository.save(admin);
        log.info("  Created {} : {}", role, email);
    }

    private void createUserWithProfile(String email, String password, String org, String hostel, String[] profile) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.USER);
        user.setOrganization(org);
        user.setHostel(hostel);
        User saved = userRepository.save(user);

        StudentProfile sp = new StudentProfile();
        sp.setUser(saved);
        sp.setSleepSchedule(profile[0]);
        sp.setCleanlinessLevel(profile[1]);
        sp.setNoiseTolerance(profile[2]);
        sp.setSocialLevel(profile[3]);
        sp.setStudyHabits(profile[4]);
        sp.setGuestFrequency(profile[5]);
        sp.setRoomTemperature(profile[6]);
        profileRepository.save(sp);

        log.info("  Created user: {} ({}/{})", email, org, hostel);
    }
}
