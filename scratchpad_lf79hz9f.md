# Verification Plan - Hierarchical Admin System

- [x] Login as Manager (`manager@upes.ac.in`)
- [x] Verify redirection to `manager.html`
- [x] Capture Manager Dashboard screenshot
- [x] Logout
- [x] Login as Warden (`warden.boys@upes.ac.in`)
- [x] Verify Warden Dashboard (likely `admin.html`)
- [x] Capture Warden Dashboard screenshot
- [x] Report success/failure

## Findings
- Manager login successful.
- Redirection to `http://localhost:56144/manager` (Manager Dashboard) confirmed.
- Manager dashboard shows system-wide stats and upload options.
- Warden login successful.
- Redirection to `http://localhost:56144/admin` (Warden Dashboard) confirmed.
- Warden dashboard correctly filters students by hostel (e.g., Bidholi Boys Hostel).
- Both screenshots captured successfully.
