package com.example.data

import com.example.model.*

object SportsOpsSeedData {

    val users = listOf(
        CurrentUser(
            id = "TM-01",
            name = "Krish Sharma",
            email = "krish.sharma@university.edu",
            role = UserRole.CORE,
            vertical = "Event Operations",
            avatarColor = 0xFF003F5C
        ),
        CurrentUser(
            id = "TM-02",
            name = "Jahanvi Patel",
            email = "jahanvi.patel@university.edu",
            role = UserRole.DEPUTY_CORE,
            vertical = "Technology & Systems",
            reportsTo = "Krish Sharma",
            avatarColor = 0xFF2F4B7C
        ),
        CurrentUser(
            id = "TM-03",
            name = "Saksham Verma",
            email = "saksham.verma@university.edu",
            role = UserRole.SUPER_COORDINATOR,
            vertical = "Stakeholder Coordination",
            reportsTo = "Krish Sharma",
            avatarColor = 0xFF665191
        ),
        CurrentUser(
            id = "TM-04",
            name = "Bhanav Kapoor",
            email = "bhanav.kapoor@university.edu",
            role = UserRole.COORDINATOR,
            vertical = "Event Operations",
            reportsTo = "Saksham Verma",
            avatarColor = 0xFFA05195
        ),
        CurrentUser(
            id = "TM-05",
            name = "Saavan Nair",
            email = "saavan.nair@university.edu",
            role = UserRole.VOLUNTEER,
            vertical = "Technology & Systems",
            reportsTo = "Jahanvi Patel",
            avatarColor = 0xFFD45087
        ),
        CurrentUser(
            id = "TM-06",
            name = "Ananya Iyer",
            email = "ananya.iyer@university.edu",
            role = UserRole.COORDINATOR,
            vertical = "Stakeholder Coordination",
            reportsTo = "Saksham Verma",
            avatarColor = 0xFFF95D6A
        ),
        CurrentUser(
            id = "TM-07",
            name = "Rohan Sengupta",
            email = "rohan.sengupta@university.edu",
            role = UserRole.VOLUNTEER,
            vertical = "Event Operations",
            reportsTo = "Bhanav Kapoor",
            avatarColor = 0xFFFF7C43
        ),
        CurrentUser(
            id = "TM-08",
            name = "Meera Joshi",
            email = "meera.joshi@university.edu",
            role = UserRole.VOLUNTEER,
            vertical = "Technology & Systems",
            reportsTo = "Jahanvi Patel",
            avatarColor = 0xFFFFA600
        )
    )

    val teamMembers = listOf(
        TeamMember("TM-01", "Krish Sharma", "krish.sharma@university.edu", "+1 (555) 019-2831", UserRole.CORE, "Event Operations", null, null, true, "2025-08-01", 0xFF003F5C),
        TeamMember("TM-02", "Jahanvi Patel", "jahanvi.patel@university.edu", "+1 (555) 019-4822", UserRole.DEPUTY_CORE, "Technology & Systems", "TM-01", "Krish Sharma", true, "2025-09-01", 0xFF2F4B7C),
        TeamMember("TM-03", "Saksham Verma", "saksham.verma@university.edu", "+1 (555) 019-9384", UserRole.SUPER_COORDINATOR, "Stakeholder Coordination", "TM-01", "Krish Sharma", true, "2025-10-15", 0xFF665191),
        TeamMember("TM-04", "Bhanav Kapoor", "bhanav.kapoor@university.edu", "+1 (555) 019-8472", UserRole.COORDINATOR, "Event Operations", "TM-03", "Saksham Verma", true, "2026-01-10", 0xFFA05195),
        TeamMember("TM-05", "Saavan Nair", "saavan.nair@university.edu", "+1 (555) 019-1123", UserRole.VOLUNTEER, "Technology & Systems", "TM-02", "Jahanvi Patel", true, "2026-02-01", 0xFFD45087),
        TeamMember("TM-06", "Ananya Iyer", "ananya.iyer@university.edu", "+1 (555) 019-5561", UserRole.COORDINATOR, "Stakeholder Coordination", "TM-03", "Saksham Verma", true, "2026-02-15", 0xFFF95D6A),
        TeamMember("TM-07", "Rohan Sengupta", "rohan.sengupta@university.edu", "+1 (555) 019-7729", UserRole.VOLUNTEER, "Event Operations", "TM-04", "Bhanav Kapoor", true, "2026-03-01", 0xFFFF7C43),
        TeamMember("TM-08", "Meera Joshi", "meera.joshi@university.edu", "+1 (555) 019-3348", UserRole.VOLUNTEER, "Technology & Systems", "TM-02", "Jahanvi Patel", true, "2026-03-10", 0xFFFFA600)
    )

    val events = listOf(
        SportsEvent(
            id = "E01",
            name = "Inter-College Football Championship",
            society = "Football Club & Athletics Council",
            eventHead = "Devansh Malhotra",
            eventHeadContact = "+1 (555) 234-5678",
            sportsPoc = "Bhanav Kapoor",
            coordinator = "Rohan Sengupta",
            currentStage = EventStage.MOCK_TRIALS,
            readinessPercent = 78,
            coreApproval = CoreApprovalStatus.APPROVED,
            remarks = "Referees confirmed; turf maintenance scheduled for Aug 22.",
            description = "Premier 11v11 men's & women's university football championship spanning 16 teams across 4 zones.",
            startDate = "2026-08-25",
            endDate = "2026-08-30",
            venue = "Main Athletic Stadium",
            expectedParticipants = 320
        ),
        SportsEvent(
            id = "E02",
            name = "Annual Track & Field Athletics Meet",
            society = "Track & Field Society",
            eventHead = "Pooja Reddy",
            eventHeadContact = "+1 (555) 345-6789",
            sportsPoc = "Saksham Verma",
            coordinator = "Ananya Iyer",
            currentStage = EventStage.REGISTRATION,
            readinessPercent = 62,
            coreApproval = CoreApprovalStatus.PENDING,
            remarks = "Awaiting timing sensors calibration from vendor.",
            description = "100m, 400m, 4x100m relay, long jump, high jump, shotput and javelin competitions.",
            startDate = "2026-09-05",
            endDate = "2026-09-07",
            venue = "Olympic Track Ground",
            expectedParticipants = 450
        ),
        SportsEvent(
            id = "E03",
            name = "Badminton Open Championship",
            society = "Racquet Sports Club",
            eventHead = "Aditya Kulkarni",
            eventHeadContact = "+1 (555) 456-7890",
            sportsPoc = "Bhanav Kapoor",
            coordinator = "Rohan Sengupta",
            currentStage = EventStage.READY_FOR_EXECUTION,
            readinessPercent = 95,
            coreApproval = CoreApprovalStatus.APPROVED,
            remarks = "Courts booked and shuttlecock inventory cleared.",
            description = "Singles & doubles badminton tournament with BWF certified scoring rules.",
            startDate = "2026-08-20",
            endDate = "2026-08-22",
            venue = "Indoor Sports Complex Hall A",
            expectedParticipants = 128
        ),
        SportsEvent(
            id = "E04",
            name = "Table Tennis Invitational",
            society = "Table Tennis Union",
            eventHead = "Harshita Sen",
            eventHeadContact = "+1 (555) 567-8901",
            sportsPoc = "Saavan Nair",
            coordinator = "Meera Joshi",
            currentStage = EventStage.TECHNICAL_SETUP,
            readinessPercent = 42,
            coreApproval = CoreApprovalStatus.REWORK_REQUESTED,
            remarks = "Rulebook rejected by Core due to missing anti-doping and walkover clauses. Scoreboard offline.",
            description = "Fast-paced tournament on 8 synthetic Stag tables with automated digital scoring.",
            startDate = "2026-08-28",
            endDate = "2026-08-29",
            venue = "Indoor Sports Complex Hall B",
            expectedParticipants = 96
        ),
        SportsEvent(
            id = "E05",
            name = "Basketball 3x3 Street League",
            society = "Basketball Association",
            eventHead = "Varun Chawla",
            eventHeadContact = "+1 (555) 678-9012",
            sportsPoc = "Saksham Verma",
            coordinator = "Ananya Iyer",
            currentStage = EventStage.BRANDING,
            readinessPercent = 54,
            coreApproval = CoreApprovalStatus.PENDING,
            remarks = "Poster creative submitted, awaiting social media release.",
            description = "Half-court fast break FIBA 3x3 sanctioned competition.",
            startDate = "2026-09-12",
            endDate = "2026-09-14",
            venue = "Outdoor Floodlit Courts",
            expectedParticipants = 160
        ),
        SportsEvent(
            id = "E06",
            name = "University Chess Grand Prix",
            society = "Strategic Mind Games Society",
            eventHead = "Kavya Menon",
            eventHeadContact = "+1 (555) 789-0123",
            sportsPoc = "Jahanvi Patel",
            coordinator = "Saavan Nair",
            currentStage = EventStage.LIVE,
            readinessPercent = 100,
            coreApproval = CoreApprovalStatus.APPROVED,
            remarks = "Round 3 in progress. Livestream functioning smoothly.",
            description = "FIDE rapid 15+10 time control tournament with DGT live digital boards.",
            startDate = "2026-08-15",
            endDate = "2026-08-17",
            venue = "Senate Conference Hall",
            expectedParticipants = 64
        ),
        SportsEvent(
            id = "E07",
            name = "Aquatics Swimming Gala",
            society = "Swimming & Waterpolo Club",
            eventHead = "Nikhil Deshmukh",
            eventHeadContact = "+1 (555) 890-1234",
            sportsPoc = "Bhanav Kapoor",
            coordinator = "Rohan Sengupta",
            currentStage = EventStage.DOCUMENTATION,
            readinessPercent = 30,
            coreApproval = CoreApprovalStatus.PENDING,
            remarks = "Lifeguard safety certification submitted for verification.",
            description = "50m, 100m freestyle, backstroke, breaststroke, butterfly and medley relay.",
            startDate = "2026-09-20",
            endDate = "2026-09-22",
            venue = "University Olympic Pool",
            expectedParticipants = 180
        ),
        SportsEvent(
            id = "E08",
            name = "Volleyball Smash Fest",
            society = "Volleyball League",
            eventHead = "Tarun Mathur",
            eventHeadContact = "+1 (555) 901-2345",
            sportsPoc = "Saksham Verma",
            coordinator = "Ananya Iyer",
            currentStage = EventStage.PROPOSAL,
            readinessPercent = 15,
            coreApproval = CoreApprovalStatus.PENDING,
            remarks = "Proposal submitted for 10-criteria rubric review.",
            description = "Inter-departmental volleyball championship for students and faculty.",
            startDate = "2026-10-02",
            endDate = "2026-10-04",
            venue = "Outdoor Volleyball Arena",
            expectedParticipants = 200
        )
    )

    fun generateRequirementsForEvent(eventId: String): List<EventReadinessRequirement> {
        val reqs = mutableListOf<EventReadinessRequirement>()
        val defaultMatrix = mapOf(
            1 to listOf(
                "Rulebook Submitted" to "Complete rules, dispute resolution & tie-breakers document.",
                "Rulebook Approved" to "Formal review and sign-off by Sports Department Core.",
                "Event Tagline" to "Catchy 1-line slogan for promotion & jersey printing.",
                "Event Description" to "Detailed description of competition format & eligibility.",
                "Round-wise Description" to "Clear fixture tree, match durations, overtime rules.",
                "FAQs Submitted" to "Top 10 participant questions answered."
            ),
            2 to listOf(
                "Poster Requirements Submitted" to "Design brief given to Media & Branding team.",
                "Posters Approved" to "Sign-off on vertical & horizontal promotional banners.",
                "Promotional Content Ready" to "Social copy, WhatsApp broadcast text, reel scripts."
            ),
            3 to listOf(
                "Registration Fields Shared" to "Google Form / Portal schema validated.",
                "Registration Live" to "Public portal open with payment/roster verification."
            ),
            4 to listOf(
                "Google Meet Requirement Shared" to "Captains briefing call schedule.",
                "Meet Link Created" to "Official video conference link generated and shared.",
                "Livestream Requirement Shared" to "Camera angles & OBS streaming setup requested.",
                "Livestream Ready" to "YouTube / Twitch test stream verified.",
                "Scoreboard Ready" to "Physical LED or digital overlay scoring system ready."
            ),
            5 to listOf(
                "Mock Trial 1 Completed" to "Dry run of check-in, whistle checks & timing equipment.",
                "Mock Trial 2 Completed" to "Full staff drill under match conditions.",
                "Event Approved for Execution" to "Final Core sign-off granting green light."
            ),
            6 to listOf(
                "Event Executed Successfully" to "All rounds played according to schedule.",
                "Participation Data Submitted" to "Final attendance sheets verified and submitted.",
                "Winner Data Submitted" to "Podium list & scorecards signed by head referee."
            ),
            7 to listOf(
                "Post Event Report Submitted" to "Detailed budget, incident report and photo link.",
                "Event Archived" to "Trophies awarded, equipment restored to storage."
            )
        )

        val phaseTitles = mapOf(
            1 to "Documentation",
            2 to "Branding",
            3 to "Registration",
            4 to "Technical Setup",
            5 to "Mock Trials",
            6 to "Execution",
            7 to "Closure"
        )

        var counter = 1
        defaultMatrix.forEach { (phaseNum, items) ->
            items.forEach { (title, note) ->
                val id = "$eventId-P$phaseNum-${String.format("%02d", counter++)}"

                // Vary completion based on event stage for realistic realism
                val isCompleted = when (eventId) {
                    "E06" -> true // Live chess -> 100%
                    "E03" -> phaseNum <= 5 // Badminton -> 95%
                    "E01" -> phaseNum <= 4 || (phaseNum == 5 && title.contains("Trial 1")) // Football -> ~78%
                    "E02" -> phaseNum <= 2 || (phaseNum == 3 && title.contains("Fields")) // Track -> ~62%
                    "E05" -> phaseNum <= 2 // Basketball -> 54%
                    "E04" -> if (title == "Rulebook Approved") false else (phaseNum == 1 && title.contains("Submitted")) // Table Tennis -> At risk
                    "E07" -> phaseNum == 1 && (title.contains("Tagline") || title.contains("Description"))
                    else -> false
                }

                val pocState = if (isCompleted) RequirementResponsibilityState.COMPLETED else if (eventId == "E04" && title == "Rulebook Approved") RequirementResponsibilityState.REWORK else RequirementResponsibilityState.PENDING
                val coordState = if (isCompleted) RequirementResponsibilityState.COMPLETED else RequirementResponsibilityState.PENDING
                val coreState = if (isCompleted) RequirementResponsibilityState.COMPLETED else if (eventId == "E04" && title == "Rulebook Approved") RequirementResponsibilityState.REJECTED else RequirementResponsibilityState.PENDING

                reqs.add(
                    EventReadinessRequirement(
                        id = id,
                        eventId = eventId,
                        phaseNumber = phaseNum,
                        phaseTitle = phaseTitles[phaseNum] ?: "Phase $phaseNum",
                        title = title,
                        pocStatus = pocState,
                        coordinatorStatus = coordState,
                        coreStatus = coreState,
                        deadline = "2026-08-20",
                        notes = note,
                        evidenceUrl = if (isCompleted) "https://drive.google.com/sports/$eventId/$id" else null,
                        lastUpdated = "2026-08-15 16:30"
                    )
                )
            }
        }
        return reqs
    }

    val readinessRequirements: List<EventReadinessRequirement> by lazy {
        events.flatMap { generateRequirementsForEvent(it.id) }
    }

    val tasks = listOf(
        TaskItem(
            id = "TASK-0001",
            title = "Finalize Rulebook & Match Regulations for Football Championship",
            description = "Integrate red card penalty rules, tie-breaker penalties, match ball specs and protest arbitration flow.",
            vertical = "Event Operations",
            teamMemberId = "TM-04",
            teamMemberName = "Bhanav Kapoor",
            taskType = "Documentation",
            priority = Priority.CRITICAL,
            assignedById = "TM-01",
            assignedByName = "Krish Sharma",
            dateAssigned = "2026-08-10",
            deadline = "2026-08-14", // OVERDUE
            status = TaskStatus.IN_PROGRESS,
            progressPercent = 85,
            blocker = "Awaiting signed confirmation from Medical Team on mandatory EMT presence.",
            remarks = "Medical team POC requested follow-up meeting.",
            eventId = "E01",
            eventName = "Inter-College Football Championship",
            dependencies = emptyList(),
            evidenceList = listOf(
                EvidenceAttachment("EV-01", "Football Rulebook Draft v2.1", "https://docs.google.com/document/d/football-rulebook", "Document", "Bhanav Kapoor", "2026-08-14 11:20")
            ),
            activityHistory = listOf(
                TaskActivity("ACT-01", "Krish Sharma", "Created task and assigned to Bhanav Kapoor", "2026-08-10 09:30"),
                TaskActivity("ACT-02", "Bhanav Kapoor", "Updated progress from 0% to 50%", "2026-08-12 14:15"),
                TaskActivity("ACT-03", "Bhanav Kapoor", "Flagged blocker: Medical EMT confirmation pending", "2026-08-14 18:00")
            ),
            lastUpdated = "2026-08-14 18:00"
        ),
        TaskItem(
            id = "TASK-0002",
            title = "Deploy Automated Registration Portal for Athletics Meet",
            description = "Build student verification form with student ID lookup, event slot capping and waiver checkbox.",
            vertical = "Technology & Systems",
            teamMemberId = "TM-05",
            teamMemberName = "Saavan Nair",
            taskType = "System Setup",
            priority = Priority.HIGH,
            assignedById = "TM-02",
            assignedByName = "Jahanvi Patel",
            dateAssigned = "2026-08-11",
            deadline = "2026-08-17", // AT RISK (due tomorrow)
            status = TaskStatus.IN_PROGRESS,
            progressPercent = 65,
            blocker = null,
            remarks = "Database connection tested; frontend styling in progress.",
            eventId = "E02",
            eventName = "Annual Track & Field Athletics Meet",
            dependencies = emptyList(),
            evidenceList = listOf(
                EvidenceAttachment("EV-02", "Athletics Portal Staging URL", "https://athletics.sportsops.university.edu/staging", "Link", "Saavan Nair", "2026-08-15 15:40")
            ),
            activityHistory = listOf(
                TaskActivity("ACT-04", "Jahanvi Patel", "Created task and assigned to Saavan Nair", "2026-08-11 10:00"),
                TaskActivity("ACT-05", "Saavan Nair", "Updated progress to 65%", "2026-08-15 15:40")
            ),
            lastUpdated = "2026-08-15 15:40"
        ),
        TaskItem(
            id = "TASK-0003",
            title = "Inspect & Calibrate Timing Sensors for 100m Track",
            description = "Laser tripwire timing gates must be calibrated with photo-finish camera system.",
            vertical = "Technology & Systems",
            teamMemberId = "TM-08",
            teamMemberName = "Meera Joshi",
            taskType = "Technical Setup",
            priority = Priority.CRITICAL,
            assignedById = "TM-02",
            assignedByName = "Jahanvi Patel",
            dateAssigned = "2026-08-12",
            deadline = "2026-08-15", // OVERDUE
            status = TaskStatus.BLOCKED,
            progressPercent = 40,
            blocker = "Vendor backup battery pack has low voltage; replacement requested under ISS-001.",
            remarks = "Escalated to Deputy Core for emergency replacement.",
            eventId = "E02",
            eventName = "Annual Track & Field Athletics Meet",
            dependencies = listOf("TASK-0002"),
            evidenceList = listOf(
                EvidenceAttachment("EV-03", "Diagnostic Report - Gate 4 Fault", "https://drive.google.com/file/d/timing-gate-diagnostics.pdf", "Document", "Meera Joshi", "2026-08-15 17:00")
            ),
            activityHistory = listOf(
                TaskActivity("ACT-06", "Meera Joshi", "Flagged task as BLOCKED", "2026-08-15 17:00")
            ),
            lastUpdated = "2026-08-15 17:00"
        ),
        TaskItem(
            id = "TASK-0004",
            title = "Approve Branding Banners & Social Content for Badminton Open",
            description = "Review 1080x1920 story creatives, printable 10x4ft vinyl court banners and sponsor logos.",
            vertical = "Stakeholder Coordination",
            teamMemberId = "TM-06",
            teamMemberName = "Ananya Iyer",
            taskType = "Branding & Media",
            priority = Priority.MEDIUM,
            assignedById = "TM-03",
            assignedByName = "Saksham Verma",
            dateAssigned = "2026-08-08",
            deadline = "2026-08-18",
            status = TaskStatus.COMPLETED,
            progressPercent = 100,
            completedOn = "2026-08-16",
            blocker = null,
            remarks = "All 4 banners approved by Dean of Student Affairs.",
            eventId = "E03",
            eventName = "Badminton Open Championship",
            dependencies = emptyList(),
            evidenceList = listOf(
                EvidenceAttachment("EV-04", "Badminton Banners Final High-Res", "https://drive.google.com/drive/folders/badminton-art", "Drive", "Ananya Iyer", "2026-08-16 09:00")
            ),
            activityHistory = listOf(
                TaskActivity("ACT-07", "Ananya Iyer", "Marked task as COMPLETED (100%)", "2026-08-16 09:00")
            ),
            lastUpdated = "2026-08-16 09:00"
        ),
        TaskItem(
            id = "TASK-0005",
            title = "Resolve Table Tennis Rulebook Deficiencies & Re-submit",
            description = "Add explicit walkover penalty, certified referee credentials, and dress code regulations.",
            vertical = "Event Operations",
            teamMemberId = "TM-07",
            teamMemberName = "Rohan Sengupta",
            taskType = "Documentation",
            priority = Priority.CRITICAL,
            assignedById = "TM-01",
            assignedByName = "Krish Sharma",
            dateAssigned = "2026-08-14",
            deadline = "2026-08-17", // AT RISK
            status = TaskStatus.IN_PROGRESS,
            progressPercent = 50,
            blocker = null,
            remarks = "Drafting revised clauses with Table Tennis Society President.",
            eventId = "E04",
            eventName = "Table Tennis Invitational",
            dependencies = emptyList(),
            evidenceList = emptyList(),
            activityHistory = listOf(
                TaskActivity("ACT-08", "Krish Sharma", "Requested rework on rulebook", "2026-08-14 16:00")
            ),
            lastUpdated = "2026-08-15 11:30"
        ),
        TaskItem(
            id = "TASK-0006",
            title = "Configure Live Digital Chess Boards (DGT) for Grand Prix",
            description = "Verify serial connectivity for 16 DGT electronic boards to tournament transmission server.",
            vertical = "Technology & Systems",
            teamMemberId = "TM-05",
            teamMemberName = "Saavan Nair",
            taskType = "Technical Setup",
            priority = Priority.HIGH,
            assignedById = "TM-02",
            assignedByName = "Jahanvi Patel",
            dateAssigned = "2026-08-12",
            deadline = "2026-08-15",
            status = TaskStatus.COMPLETED,
            progressPercent = 100,
            completedOn = "2026-08-15",
            blocker = null,
            remarks = "All boards operational with 1-second latency.",
            eventId = "E06",
            eventName = "University Chess Grand Prix",
            dependencies = emptyList(),
            evidenceList = listOf(
                EvidenceAttachment("EV-05", "DGT Live Stream Dashboard", "https://chess.sportsops.university.edu/live", "Link", "Saavan Nair", "2026-08-15 14:00")
            ),
            activityHistory = listOf(
                TaskActivity("ACT-09", "Saavan Nair", "Completed hardware sync", "2026-08-15 14:00")
            ),
            lastUpdated = "2026-08-15 14:00"
        ),
        TaskItem(
            id = "TASK-0007",
            title = "Coordinate External Referees & Match Officials for Basketball 3x3",
            description = "Contract 4 state-certified FIBA referees, arrange hospitality, match fee disbursements and briefing.",
            vertical = "Stakeholder Coordination",
            teamMemberId = "TM-06",
            teamMemberName = "Ananya Iyer",
            taskType = "Stakeholder Outreach",
            priority = Priority.HIGH,
            assignedById = "TM-03",
            assignedByName = "Saksham Verma",
            dateAssigned = "2026-08-13",
            deadline = "2026-08-21",
            status = TaskStatus.IN_PROGRESS,
            progressPercent = 30,
            blocker = null,
            remarks = "Initial inquiry sent to State Basketball Federation.",
            eventId = "E05",
            eventName = "Basketball 3x3 Street League",
            dependencies = emptyList(),
            evidenceList = emptyList(),
            activityHistory = listOf(
                TaskActivity("ACT-10", "Saksham Verma", "Assigned task to Ananya Iyer", "2026-08-13 11:00")
            ),
            lastUpdated = "2026-08-14 10:00"
        ),
        TaskItem(
            id = "TASK-0008",
            title = "Conduct Pre-Tournament Safety & Lifeguard Audit for Aquatics Gala",
            description = "Inspect poolside AED defibrillators, water clarity, emergency rescue tubes and first-aid kits.",
            vertical = "Event Operations",
            teamMemberId = "TM-04",
            teamMemberName = "Bhanav Kapoor",
            taskType = "Safety Audit",
            priority = Priority.CRITICAL,
            assignedById = "TM-01",
            assignedByName = "Krish Sharma",
            dateAssigned = "2026-08-15",
            deadline = "2026-08-24",
            status = TaskStatus.NOT_STARTED,
            progressPercent = 0,
            blocker = null,
            remarks = "Waiting on Pool Maintenance Directorate access keys.",
            eventId = "E07",
            eventName = "Aquatics Swimming Gala",
            dependencies = emptyList(),
            evidenceList = emptyList(),
            activityHistory = listOf(
                TaskActivity("ACT-11", "Krish Sharma", "Created safety audit task", "2026-08-15 09:00")
            ),
            lastUpdated = "2026-08-15 09:00"
        )
    )

    val issues = listOf(
        IssueItem(
            id = "ISS-001",
            dateRaised = "2026-08-14",
            vertical = "Technology & Systems",
            eventId = "E02",
            eventName = "Annual Track & Field Athletics Meet",
            problem = "Photocell Timing Gate 4 sensor battery dead; cannot measure 100m sprint lane 4 split times.",
            raisedById = "TM-08",
            raisedByName = "Meera Joshi",
            assignedToId = "TM-02",
            assignedToName = "Jahanvi Patel",
            severity = IssueSeverity.CRITICAL,
            status = IssueStatus.UNDER_REVIEW,
            actionRequired = "Procure 12V 7Ah replacement battery or rent backup laser sensor from State Athletics Club.",
            deadline = "2026-08-16", // DUE TODAY
            escalatedToId = "TM-01",
            escalatedToName = "Krish Sharma",
            resolution = null,
            resolutionDate = null,
            evidenceList = listOf(
                EvidenceAttachment("ISS-EV-01", "Gate Battery Voltmeter Photo", "https://storage.sportsops.university.edu/issues/gate4_fault.jpg", "Image", "Meera Joshi", "2026-08-14 17:30")
            ),
            remarks = "Vendor contacted; 24h delivery estimated. Core approval needed for expedited $120 fee.",
            escalationHistory = listOf(
                EscalationHistoryEntry("Meera Joshi", "Jahanvi Patel", "2026-08-14 17:35", "Hardware sensor non-responsive during calibration test."),
                EscalationHistoryEntry("Jahanvi Patel", "Krish Sharma", "2026-08-15 10:00", "Critical budget approval required for expedited replacement.")
            ),
            lastUpdated = "2026-08-15 10:00"
        ),
        IssueItem(
            id = "ISS-002",
            dateRaised = "2026-08-13",
            vertical = "Event Operations",
            eventId = "E04",
            eventName = "Table Tennis Invitational",
            problem = "Table Tennis Rulebook failed Core standards due to vague tie-breaking and dispute arbitration clauses.",
            raisedById = "TM-01",
            raisedByName = "Krish Sharma",
            assignedToId = "TM-07",
            assignedToName = "Rohan Sengupta",
            severity = IssueSeverity.HIGH,
            status = IssueStatus.ACTION_TAKEN,
            actionRequired = "Revise Section 4 and Section 7 in compliance with ITTF handbook and re-submit to Core inbox.",
            deadline = "2026-08-17",
            escalatedToId = "TM-04",
            escalatedToName = "Bhanav Kapoor",
            resolution = "Drafting revised clauses with faculty advisor.",
            resolutionDate = null,
            evidenceList = emptyList(),
            remarks = "Rohan and Bhanav working on revision.",
            escalationHistory = listOf(
                EscalationHistoryEntry("Krish Sharma", "Rohan Sengupta", "2026-08-13 15:00", "Core review returned with Rework status.")
            ),
            lastUpdated = "2026-08-15 12:00"
        ),
        IssueItem(
            id = "ISS-003",
            dateRaised = "2026-08-12",
            vertical = "Stakeholder Coordination",
            eventId = "E01",
            eventName = "Inter-College Football Championship",
            problem = "Campus Health Center requires formal written indemnification before allocating ambulance on match days.",
            raisedById = "TM-04",
            raisedByName = "Bhanav Kapoor",
            assignedToId = "TM-03",
            assignedToName = "Saksham Verma",
            severity = IssueSeverity.HIGH,
            status = IssueStatus.RESOLVED,
            actionRequired = "Obtain Dean of Student Welfare counter-signature on Medical Support Memorandum.",
            deadline = "2026-08-15",
            escalatedToId = "TM-01",
            escalatedToName = "Krish Sharma",
            resolution = "Signed MoU received from Dean Office; ambulance allocated for Aug 25-30.",
            resolutionDate = "2026-08-15",
            evidenceList = listOf(
                EvidenceAttachment("ISS-EV-02", "Signed Medical Support MoU", "https://docs.google.com/pdf/medical_mou_signed.pdf", "Document", "Saksham Verma", "2026-08-15 16:20")
            ),
            remarks = "Resolved and filed with Security & Operations.",
            escalationHistory = listOf(
                EscalationHistoryEntry("Bhanav Kapoor", "Saksham Verma", "2026-08-12 11:30", "Medical Center required executive escalation.")
            ),
            lastUpdated = "2026-08-15 16:20"
        ),
        IssueItem(
            id = "ISS-004",
            dateRaised = "2026-08-15",
            vertical = "Technology & Systems",
            eventId = "E05",
            eventName = "Basketball 3x3 Street League",
            problem = "Outdoor floodlight breaker panel tripping under full 20kW illumination load.",
            raisedById = "TM-05",
            raisedByName = "Saavan Nair",
            assignedToId = "TM-02",
            assignedToName = "Jahanvi Patel",
            severity = IssueSeverity.MEDIUM,
            status = IssueStatus.OPEN,
            actionRequired = "Campus Electrical Engineering team requested to replace 63A MCB with industrial breaker.",
            deadline = "2026-08-20",
            escalatedToId = null,
            escalatedToName = null,
            resolution = null,
            resolutionDate = null,
            evidenceList = emptyList(),
            remarks = "Ticket #EE-9842 filed with university facilities.",
            escalationHistory = emptyList(),
            lastUpdated = "2026-08-15 18:45"
        )
    )

    val calendarItems = listOf(
        CalendarItem(
            id = "CAL-001",
            date = "2026-08-16",
            time = "10:00 AM",
            activity = "Sports Ops Command Center Morning Briefing",
            category = "Meeting",
            eventOrArea = "All Department Verticals",
            audience = "Core, Coordinators, Super Coordinators",
            status = "Scheduled",
            priority = Priority.CRITICAL,
            deadlineType = DeadlineType.HARD_DEADLINE,
            meetingUrl = "https://meet.google.com/sports-ops-brief",
            remarks = "Review Football mock trial readiness and Table Tennis rework."
        ),
        CalendarItem(
            id = "CAL-002",
            date = "2026-08-16",
            time = "04:30 PM",
            activity = "Chess Grand Prix Round 4 Live Broadcast",
            category = "Match",
            eventOrArea = "E06 - Chess Grand Prix",
            audience = "Public & Participants",
            status = "Live",
            priority = Priority.HIGH,
            deadlineType = DeadlineType.INFORMATIONAL,
            meetingUrl = "https://youtube.com/live/universitychess2026",
            resourceUrl = "https://chess.sportsops.university.edu/pairings",
            remarks = "Top board GM match streamed with commentary."
        ),
        CalendarItem(
            id = "CAL-003",
            date = "2026-08-17",
            time = "11:59 PM",
            activity = "Athletics Meet Registration Portal Hard Close",
            category = "Deadlines",
            eventOrArea = "E02 - Track & Field Athletics Meet",
            audience = "Participants & Tech Team",
            status = "Scheduled",
            priority = Priority.CRITICAL,
            deadlineType = DeadlineType.HARD_DEADLINE,
            resourceUrl = "https://athletics.sportsops.university.edu",
            remarks = "No late registrations accepted after timer expires."
        ),
        CalendarItem(
            id = "CAL-004",
            date = "2026-08-18",
            time = "03:00 PM",
            activity = "Badminton Championship Captains Technical Meeting",
            category = "Meeting",
            eventOrArea = "E03 - Badminton Open Championship",
            audience = "All Team Captains & Referees",
            status = "Scheduled",
            priority = Priority.HIGH,
            deadlineType = DeadlineType.SOFT_DEADLINE,
            meetingUrl = "https://meet.google.com/badminton-captains-brief",
            remarks = "Draw seedings release and jersey color conflict checks."
        ),
        CalendarItem(
            id = "CAL-005",
            date = "2026-08-20",
            time = "09:00 AM",
            activity = "Opening Ceremony: Badminton Open Championship",
            category = "Milestone",
            eventOrArea = "E03 - Badminton Open Championship",
            audience = "All University",
            status = "Scheduled",
            priority = Priority.HIGH,
            deadlineType = DeadlineType.HARD_DEADLINE,
            remarks = "Chief Guest: Vice Chancellor & Sports Director."
        ),
        CalendarItem(
            id = "CAL-006",
            date = "2026-08-22",
            time = "02:00 PM",
            activity = "Football Championship Turf Mock Trial & Whistle Drill",
            category = "Setup",
            eventOrArea = "E01 - Football Championship",
            audience = "Ground Staff, Referees & Operations",
            status = "Scheduled",
            priority = Priority.CRITICAL,
            deadlineType = DeadlineType.HARD_DEADLINE,
            remarks = "Dry run with electronic substitute boards & siren."
        )
    )

    val proposalRubricCriteria = listOf(
        ReviewCriterion("CRIT-01", "Feasibility & Logistics", "Practicality of venue, schedule and equipment availability.", 10, 1.2),
        ReviewCriterion("CRIT-02", "Budget & Financial Prudence", "Detailed itemized budget estimate and sponsor potential.", 10, 1.0),
        ReviewCriterion("CRIT-03", "Safety & Risk Management", "Medical contingency, EMT readiness and injury protocols.", 10, 1.3),
        ReviewCriterion("CRIT-04", "Participant Engagement", "Anticipated turnout, student appeal and inclusivity.", 10, 1.0),
        ReviewCriterion("CRIT-05", "Technical & Referee Standards", "Certification of match officials and scoring integrity.", 10, 1.0),
        ReviewCriterion("CRIT-06", "Branding & Media Reach", "Quality of promotional campaign, live streaming and visual identity.", 10, 0.8),
        ReviewCriterion("CRIT-07", "Timeline & Milestone Realism", "Adequacy of buffer days for mock trials and setup.", 10, 1.0),
        ReviewCriterion("CRIT-08", "Equipment & Facility Impact", "Preservation of university turf, courts and hardware.", 10, 0.9),
        ReviewCriterion("CRIT-09", "Volunteer & Staffing Plan", "Ratio of trained volunteers to match fixtures.", 10, 0.9),
        ReviewCriterion("CRIT-10", "Legacy & Competitive Value", "Contribution to university athletics prestige and ranking.", 10, 0.9)
    )

    val proposalReviews = listOf(
        ProposalReview(
            id = "REV-01",
            eventId = "E08",
            eventTitle = "Volleyball Smash Fest",
            reviewerId = "TM-01",
            reviewerName = "Krish Sharma",
            criteriaScores = listOf(
                CriterionScore("CRIT-01", "Feasibility & Logistics", 9, 10, "Outdoor court availability confirmed."),
                CriterionScore("CRIT-02", "Budget & Financial Prudence", 8, 10, "Reasonable referee fee structure."),
                CriterionScore("CRIT-03", "Safety & Risk Management", 8, 10, "Need non-slip court tape."),
                CriterionScore("CRIT-04", "Participant Engagement", 9, 10, "High student demand for inter-hostel league."),
                CriterionScore("CRIT-05", "Technical & Referee Standards", 9, 10, "State association referees pledged."),
                CriterionScore("CRIT-06", "Branding & Media Reach", 7, 10, "Branding plan needs more reels."),
                CriterionScore("CRIT-07", "Timeline & Milestone Realism", 8, 10, "Two-week prep window is tight but viable."),
                CriterionScore("CRIT-08", "Equipment & Facility Impact", 9, 10, "Net posts secured."),
                CriterionScore("CRIT-09", "Volunteer & Staffing Plan", 8, 10, "15 court marshals assigned."),
                CriterionScore("CRIT-10", "Legacy & Competitive Value", 9, 10, "Excellent revival of annual cup.")
            ),
            totalScore = 84,
            maxPossibleScore = 100,
            recommendation = ProposalRecommendation.RECOMMEND,
            strengths = "High student excitement, low facility cost, proven referee pipeline.",
            concerns = "Weather contingency if rain hits outdoor court.",
            suggestions = "Reserve indoor backup court slots.",
            isSubmitted = true,
            submittedAt = "2026-08-14 16:30"
        ),
        ProposalReview(
            id = "REV-02",
            eventId = "E08",
            eventTitle = "Volleyball Smash Fest",
            reviewerId = "TM-02",
            reviewerName = "Jahanvi Patel",
            criteriaScores = listOf(
                CriterionScore("CRIT-01", "Feasibility & Logistics", 8, 10, "Good logistics."),
                CriterionScore("CRIT-02", "Budget & Financial Prudence", 8, 10, "Within allocated sports budget."),
                CriterionScore("CRIT-03", "Safety & Risk Management", 7, 10, "Requires ice pack coolers on courtside."),
                CriterionScore("CRIT-04", "Participant Engagement", 9, 10, "16 teams ready."),
                CriterionScore("CRIT-05", "Technical & Referee Standards", 8, 10, "FIVB rules."),
                CriterionScore("CRIT-06", "Branding & Media Reach", 8, 10, "Live scoring overlay required."),
                CriterionScore("CRIT-07", "Timeline & Milestone Realism", 8, 10, "On schedule."),
                CriterionScore("CRIT-08", "Equipment & Facility Impact", 9, 10, "Good."),
                CriterionScore("CRIT-09", "Volunteer & Staffing Plan", 8, 10, "Staff count sufficient."),
                CriterionScore("CRIT-10", "Legacy & Competitive Value", 8, 10, "Strong competition.")
            ),
            totalScore = 81,
            maxPossibleScore = 100,
            recommendation = ProposalRecommendation.RECOMMEND,
            strengths = "Well-balanced cost to participation ratio.",
            concerns = "Digital live scoreboard software must be tested.",
            suggestions = "Test scoreboard on OBS during mock trials.",
            isSubmitted = true,
            submittedAt = "2026-08-15 11:00"
        )
    )

    val approvals = listOf(
        ApprovalItem(
            id = "APP-001",
            type = ApprovalType.EVENT_EXECUTION,
            title = "Final Execution Approval: Badminton Open (E03)",
            subtitle = "All 5 preparatory readiness phases cleared at 95%. Requesting Core Green Light.",
            targetId = "E03",
            requestedBy = "Bhanav Kapoor",
            requestedDate = "2026-08-15 18:00",
            status = CoreApprovalStatus.PENDING,
            remarks = "Ready for execution."
        ),
        ApprovalItem(
            id = "APP-002",
            type = ApprovalType.PROPOSAL_DECISION,
            title = "Proposal Selection: Volleyball Smash Fest (E08)",
            subtitle = "Average Rubric Score: 82.5/100 across 2 independent reviewer submissions.",
            targetId = "E08",
            requestedBy = "Saksham Verma",
            requestedDate = "2026-08-15 12:00",
            status = CoreApprovalStatus.PENDING,
            remarks = "Awaiting Core approval to advance to Stage 6 (Onboarding)."
        ),
        ApprovalItem(
            id = "APP-003",
            type = ApprovalType.ISSUE_ESCALATION,
            title = "Expedited Hardware Replacement Budget (ISS-001)",
            subtitle = "$120 emergency procurement of laser photocell sensor battery for E02.",
            targetId = "ISS-001",
            requestedBy = "Jahanvi Patel",
            requestedDate = "2026-08-15 10:30",
            status = CoreApprovalStatus.PENDING,
            remarks = "Vendor ready to dispatch on Core authorization."
        )
    )

    val notifications = listOf(
        NotificationItem(
            id = "NOTIF-01",
            title = "Critical Task Overdue",
            message = "TASK-0001 (Finalize Rulebook for Football) crossed its Aug 14 deadline.",
            priority = NotificationPriority.CRITICAL,
            timestamp = "10 mins ago",
            isRead = false,
            targetType = "TASK",
            targetId = "TASK-0001"
        ),
        NotificationItem(
            id = "NOTIF-02",
            title = "Hardware Blocker Escalated",
            message = "ISS-001 for Athletics Meet timing sensors escalated to Core Krish Sharma.",
            priority = NotificationPriority.CRITICAL,
            timestamp = "1 hour ago",
            isRead = false,
            targetType = "ISSUE",
            targetId = "ISS-001"
        ),
        NotificationItem(
            id = "NOTIF-03",
            title = "Approval Requested",
            message = "Badminton Open (E03) reached 95% readiness and requested Execution Authorization.",
            priority = NotificationPriority.HIGH,
            timestamp = "3 hours ago",
            isRead = false,
            targetType = "APPROVAL",
            targetId = "APP-001"
        ),
        NotificationItem(
            id = "NOTIF-04",
            title = "Task Completed",
            message = "Ananya Iyer completed TASK-0004: Badminton Banners & Media Creatives.",
            priority = NotificationPriority.NORMAL,
            timestamp = "Yesterday",
            isRead = true,
            targetType = "TASK",
            targetId = "TASK-0004"
        )
    )

    val auditLogs = listOf(
        AuditLogEntry("LOG-001", "Krish Sharma", UserRole.CORE, "2026-08-16 08:30", "Task", "TASK-0001", "Follow-up Sent", "Urgent reminder sent to Bhanav Kapoor for rulebook medical sign-off."),
        AuditLogEntry("LOG-002", "Ananya Iyer", UserRole.COORDINATOR, "2026-08-16 09:00", "Task", "TASK-0004", "Status Changed", "Status updated from In Progress to Completed (100%)."),
        AuditLogEntry("LOG-003", "Jahanvi Patel", UserRole.DEPUTY_CORE, "2026-08-15 10:00", "Issue", "ISS-001", "Escalated", "Escalated to Core Krish Sharma for emergency budget."),
        AuditLogEntry("LOG-004", "Saavan Nair", UserRole.VOLUNTEER, "2026-08-15 14:00", "Task", "TASK-0006", "Completed", "DGT chess board digital serial stream verified."),
        AuditLogEntry("LOG-005", "Krish Sharma", UserRole.CORE, "2026-08-14 16:00", "Event", "E04", "Core Rework", "Rulebook rejected with comments on missing anti-doping regulations.")
    )
}
