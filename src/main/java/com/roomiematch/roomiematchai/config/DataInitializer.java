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
 * Seeds the database with test users and profiles on first startup.
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

        log.info("Seeding database with demo users and profiles...");

        // ── Demo Users ──────────────────────────────────
        String[][] users = {
            {"alice@university.edu",   "password123"},
            {"bob@university.edu",     "password123"},
            {"charlie@university.edu", "password123"},
            {"diana@university.edu",   "password123"},
            {"ethan@university.edu",   "password123"},
        };

        // ── Demo Profiles (sleep, cleanliness, noise, social, study, guests, temp) ──
        String[][] profiles = {
            {"EARLY_BIRD", "VERY_CLEAN", "SILENT",     "INTROVERT", "IN_ROOM",  "RARELY",    "COOL"},
            {"NIGHT_OWL",  "MODERATE",   "LOUD_OK",    "EXTROVERT", "LIBRARY",  "OFTEN",     "WARM"},
            {"FLEXIBLE",   "VERY_CLEAN", "MODERATE",   "AMBIVERT",  "FLEXIBLE", "SOMETIMES", "MODERATE"},
            {"EARLY_BIRD", "MODERATE",   "SILENT",     "AMBIVERT",  "IN_ROOM",  "RARELY",    "COOL"},
            {"NIGHT_OWL",  "RELAXED",    "LOUD_OK",    "EXTROVERT", "LIBRARY",  "OFTEN",     "WARM"},
        };

        for (int i = 0; i < users.length; i++) {
            User user = new User();
            user.setEmail(users[i][0]);
            user.setPassword(passwordEncoder.encode(users[i][1]));
            user.setRole(Role.USER);
            User saved = userRepository.save(user);

            StudentProfile profile = new StudentProfile();
            profile.setUser(saved);
            profile.setSleepSchedule(profiles[i][0]);
            profile.setCleanlinessLevel(profiles[i][1]);
            profile.setNoiseTolerance(profiles[i][2]);
            profile.setSocialLevel(profiles[i][3]);
            profile.setStudyHabits(profiles[i][4]);
            profile.setGuestFrequency(profiles[i][5]);
            profile.setRoomTemperature(profiles[i][6]);
            profileRepository.save(profile);

            log.info("  Created user: {} with profile", users[i][0]);
        }

        log.info("Seed data complete — {} users created. Login with any email + password123", users.length);
    }
}
