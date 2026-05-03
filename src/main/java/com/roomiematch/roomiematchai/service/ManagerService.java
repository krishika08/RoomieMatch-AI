package com.roomiematch.roomiematchai.service;

import com.roomiematch.roomiematchai.dto.AdminStudentResponseDTO;
import com.roomiematch.roomiematchai.dto.RoommateRequestResponseDTO;
import com.roomiematch.roomiematchai.dto.UploadResultDTO;
import com.roomiematch.roomiematchai.dto.WardenAssignDTO;
import com.roomiematch.roomiematchai.entity.*;
import com.roomiematch.roomiematchai.exception.DuplicateEmailException;
import com.roomiematch.roomiematchai.exception.ResourceNotFoundException;
import com.roomiematch.roomiematchai.repository.RoommateRequestRepository;
import com.roomiematch.roomiematchai.repository.StudentProfileRepository;
import com.roomiematch.roomiematchai.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for MANAGER-level operations:
 *   - Bulk student upload (CSV / JSON)
 *   - Warden assignment
 *   - Full visibility over all hostels
 */
@Service
public class ManagerService {

    private static final Logger log = LoggerFactory.getLogger(ManagerService.class);

    private final UserRepository userRepository;
    private final RoommateRequestRepository requestRepository;
    private final StudentProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthContextService authContext;

    public ManagerService(UserRepository userRepository,
                          RoommateRequestRepository requestRepository,
                          StudentProfileRepository profileRepository,
                          PasswordEncoder passwordEncoder,
                          AuthContextService authContext) {
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authContext = authContext;
    }

    // ──────────────────────────────────────────────
    //  1. Upload student dataset (CSV or JSON)
    // ──────────────────────────────────────────────
    @Transactional
    public UploadResultDTO uploadStudents(MultipartFile file) {
        User manager = authContext.getLoggedInUser();
        log.info("Manager {} uploading student dataset: {}", manager.getEmail(), file.getOriginalFilename());

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        List<Map<String, String>> records;

        if (filename.endsWith(".csv") || (contentType != null && contentType.contains("csv"))) {
            records = parseCsv(file);
        } else if (filename.endsWith(".json") || (contentType != null && contentType.contains("json"))) {
            records = parseJson(file);
        } else {
            throw new IllegalStateException("Unsupported file format. Upload a .csv or .json file.");
        }

        int created = 0, skipped = 0, failed = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            Map<String, String> row = records.get(i);
            try {
                String email = getField(row, "universityEmail", "email");
                String password = getField(row, "password");
                String hostel = getField(row, "hostelType", "hostel");
                String name = getFieldOptional(row, "name");
                String sapId = getFieldOptional(row, "sapId", "sap_id");

                if (email == null || email.isBlank()) {
                    errors.add("Row " + (i + 1) + ": Missing email");
                    failed++;
                    continue;
                }
                if (password == null || password.isBlank()) {
                    errors.add("Row " + (i + 1) + ": Missing password");
                    failed++;
                    continue;
                }

                // Normalize hostel
                if (hostel != null) {
                    hostel = normalizeHostel(hostel);
                }

                // Skip if email already exists
                if (userRepository.findByEmail(email).isPresent()) {
                    skipped++;
                    continue;
                }

                // Skip if sapId already exists
                if (sapId != null && !sapId.isBlank() && userRepository.findBySapId(sapId).isPresent()) {
                    errors.add("Row " + (i + 1) + ": Duplicate SAP ID: " + sapId);
                    skipped++;
                    continue;
                }

                User student = new User();
                student.setName(name);
                student.setEmail(email.trim());
                student.setPassword(passwordEncoder.encode(password));
                student.setSapId(sapId != null && !sapId.isBlank() ? sapId.trim() : null);
                student.setRole(Role.STUDENT);
                student.setOrganization(manager.getOrganization() != null ? manager.getOrganization() : "UPES");
                student.setHostel(hostel);
                student.setCreatedBy(manager.getId());
                userRepository.save(student);
                created++;

            } catch (Exception e) {
                errors.add("Row " + (i + 1) + ": " + e.getMessage());
                failed++;
            }
        }

        log.info("Upload complete: {} created, {} skipped, {} failed out of {} records",
                created, skipped, failed, records.size());

        return new UploadResultDTO(records.size(), created, skipped, failed, errors);
    }

    // ──────────────────────────────────────────────
    //  2. Assign/Create a Warden
    // ──────────────────────────────────────────────
    @Transactional
    public AdminStudentResponseDTO assignWarden(WardenAssignDTO dto) {
        User manager = authContext.getLoggedInUser();
        log.info("Manager {} assigning warden {} to hostel {}", manager.getEmail(), dto.getEmail(), dto.getHostel());

        String hostel = normalizeHostel(dto.getHostel());

        // Check if user already exists
        Optional<User> existing = userRepository.findByEmail(dto.getEmail());
        User warden;

        if (existing.isPresent()) {
            warden = existing.get();
            // Elevate to WARDEN if currently a STUDENT
            if (warden.getRole() == Role.STUDENT) {
                warden.setRole(Role.WARDEN);
                warden.setHostel(hostel);
                warden.setCreatedBy(manager.getId());
                if (dto.getName() != null) warden.setName(dto.getName());
                warden = userRepository.save(warden);
                log.info("Elevated existing user {} to WARDEN for hostel {}", dto.getEmail(), hostel);
            } else if (warden.getRole() == Role.WARDEN) {
                // Update hostel assignment
                warden.setHostel(hostel);
                if (dto.getName() != null) warden.setName(dto.getName());
                warden = userRepository.save(warden);
                log.info("Updated WARDEN {} hostel to {}", dto.getEmail(), hostel);
            } else {
                throw new IllegalStateException("Cannot reassign a MANAGER as warden.");
            }
        } else {
            // Create a new warden account
            warden = new User();
            warden.setName(dto.getName());
            warden.setEmail(dto.getEmail());
            warden.setPassword(passwordEncoder.encode(dto.getPassword()));
            warden.setRole(Role.WARDEN);
            warden.setOrganization(manager.getOrganization() != null ? manager.getOrganization() : "UPES");
            warden.setHostel(hostel);
            warden.setCreatedBy(manager.getId());
            warden = userRepository.save(warden);
            log.info("Created new WARDEN {} for hostel {}", dto.getEmail(), hostel);
        }

        return toStudentDTO(warden);
    }

    // ──────────────────────────────────────────────
    //  3. Get all students (manager sees everything)
    // ──────────────────────────────────────────────
    public List<AdminStudentResponseDTO> getStudents(String hostelFilter) {
        User manager = authContext.getLoggedInUser();
        String org = manager.getOrganization() != null ? manager.getOrganization() : "UPES";

        List<User> users;
        if (hostelFilter != null && !hostelFilter.isBlank()) {
            users = userRepository.findByOrganizationAndHostel(org, hostelFilter.toUpperCase());
        } else {
            users = userRepository.findByOrganization(org);
        }

        return users.stream()
                .filter(u -> u.getRole() == Role.STUDENT)
                .map(this::toStudentDTO)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    //  4. Get all wardens
    // ──────────────────────────────────────────────
    public List<AdminStudentResponseDTO> getWardens() {
        return userRepository.findByRole(Role.WARDEN).stream()
                .map(this::toStudentDTO)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    //  5. Get all requests
    // ──────────────────────────────────────────────
    public List<RoommateRequestResponseDTO> getRequests(String statusFilter) {
        List<RoommateRequest> requests;
        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                RequestStatus status = RequestStatus.valueOf(statusFilter.toUpperCase());
                requests = requestRepository.findByStatus(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid status. Use: PENDING, ACCEPTED, REJECTED");
            }
        } else {
            requests = requestRepository.findAll();
        }
        return requests.stream()
                .map(RoommateRequestResponseDTO::new)
                .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    //  6. Manual roommate assignment
    // ──────────────────────────────────────────────
    @Transactional
    public RoommateRequestResponseDTO assignRoommates(Long userId1, Long userId2) {
        User manager = authContext.getLoggedInUser();
        log.info("Manager {} assigning roommates: {} <-> {}", manager.getEmail(), userId1, userId2);

        if (userId1.equals(userId2)) {
            throw new IllegalStateException("Cannot assign a user as their own roommate.");
        }

        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId1));
        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId2));

        if (!user1.getHostel().equals(user2.getHostel())) {
            throw new IllegalStateException("Cannot assign roommates from different hostels.");
        }

        RoommateRequest request = new RoommateRequest();
        request.setSender(user1);
        request.setReceiver(user2);
        request.setStatus(RequestStatus.ACCEPTED);
        RoommateRequest saved = requestRepository.save(request);

        return new RoommateRequestResponseDTO(saved);
    }

    // ──────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────

    private AdminStudentResponseDTO toStudentDTO(User user) {
        boolean hasProfile = profileRepository.findByUserId(user.getId()).isPresent();
        return new AdminStudentResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getSapId(),
                user.getRole().name(),
                user.getOrganization(),
                user.getHostel(),
                hasProfile,
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );
    }

    /**
     * Normalizes various hostel name formats to the enum-style constant.
     * "Bidholi Boys" → "BIDHOLI_BOYS_HOSTEL"
     */
    private String normalizeHostel(String raw) {
        if (raw == null) return null;
        String upper = raw.trim().toUpperCase().replace(" ", "_");
        if (!upper.endsWith("_HOSTEL") && (upper.contains("BOYS") || upper.contains("GIRLS"))) {
            upper += "_HOSTEL";
        }
        if (!upper.startsWith("BIDHOLI") && (upper.contains("BOYS") || upper.contains("GIRLS"))) {
            upper = "BIDHOLI_" + upper;
        }
        return upper;
    }

    // ── CSV Parser ──
    private List<Map<String, String>> parseCsv(MultipartFile file) {
        List<Map<String, String>> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) throw new IllegalStateException("CSV file is empty.");
            String[] headers = headerLine.split(",");
            for (int i = 0; i < headers.length; i++) headers[i] = headers[i].trim();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] values = line.split(",", -1);
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i], values[i].trim());
                }
                records.add(row);
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse CSV file: " + e.getMessage());
        }
        return records;
    }

    // ── JSON Parser ──
    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parseJson(MultipartFile file) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(file.getInputStream(),
                    mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse JSON file: " + e.getMessage());
        }
    }

    // ── Field lookup helpers (support multiple column name aliases) ──
    private String getField(Map<String, String> row, String... keys) {
        for (String key : keys) {
            for (Map.Entry<String, String> e : row.entrySet()) {
                if (e.getKey().equalsIgnoreCase(key) && e.getValue() != null && !e.getValue().isBlank()) {
                    return e.getValue().trim();
                }
            }
        }
        return null;
    }

    private String getFieldOptional(Map<String, String> row, String... keys) {
        return getField(row, keys);
    }
}
