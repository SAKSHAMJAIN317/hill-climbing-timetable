SAKSHAM JAIN -- 2427030737
PRIYANSHI SHARMA -- 2427030746



# SmartSched AI — University Timetable & Exam Scheduler (Hill Climbing Engine)

SmartSched AI is an autonomous, full-stack, enterprise-grade academic scheduling platform designed to solve high-dimensional university timetabling and examination planning problems. The optimization engine is powered exclusively by a high-performance **Hill Climbing Local Search Algorithm** with steepest-descent penalty minimization and multi-start random restarts.

---

## 1. System Architecture

SmartSched AI employs a clean, layered Spring Boot architecture:

```
[ Frontend: HTML5 / Vanilla JS / Bootstrap 5 / Chart.js ]
                         │
                         ▼ (RESTful JSON APIs)
[ Controllers: ScheduleController, ExamController, FacultyController, ... ]
                         │
                         ▼
[ Services & Business Layer: SchedulingService, ExamSchedulingService, DemoDataService ]
                         │
       ┌─────────────────┴─────────────────┐
       ▼                                   ▼
[ Hill Climbing Optimization Engine ]  [ Independent ScheduleValidator ]
  ├─ Candidate State Initialization      ├─ Hard Constraints (Double booking, Labs, Capacity)
  ├─ Steepest Descent Local Search       └─ Soft Constraints (Idle gaps, Overload, Room fatigue)
  └─ Multi-Start Random Restarts
                         │
                         ▼
[ Repositories & Spring Data JPA ]
                         │
                         ▼
[ Database: MySQL 8 / H2 In-Memory Dev Mode ]
```

---

## 2. Technology Stack

- **Backend:** Java 21+ / Spring Boot 3.3.4
  - Spring Web (REST Controllers)
  - Spring Data JPA (Relational persistence)
  - Hibernate Validator (Bean Validation)
- **Database:**
  - **MySQL 8** (Primary production database via `application-mysql.properties`)
  - **H2** (In-memory development profile for instant zero-setup execution and automated tests)
- **Frontend:**
  - HTML5 & CSS3 (Custom design system + Print-friendly mode)
  - Vanilla JavaScript ES6+ (Modular API service, dynamic DOM rendering)
  - Bootstrap 5.3.3 & FontAwesome 6.5.1
  - Chart.js 4.4.2 (Hill Climbing Fitness Convergence trajectory curve)
- **Testing:**
  - JUnit 5, Spring Boot Test, MockMvc (19 passing unit & integration tests)

---

## 3. Hill Climbing Optimization Engine

### A. Mathematical Formulation
1. **Variables ($V$):** Each weekly lecture or practical lab session across all student batches:
   $$v_i = (\text{Subject}_i, \text{Batch}_i, \text{Faculty}_i, \text{Duration}_i, \text{RoomType}_i)$$
2. **Feasible Domain ($Dom(v_i)$):** Precomputed valid `(TimeSlot, Classroom)` tuples honoring room capacity ($\ge \text{students}$), room type (Lecture vs Lab), non-break working periods, and faculty availability.
3. **State Representation ($S$):** A candidate assignment vector $S = [P_1, P_2, \dots, P_N]$ where $P_i \in Dom(v_i)$.

### B. Objective & Fitness Function
The algorithm performs **steepest-descent minimization** on an objective penalty cost:
$$\text{Cost}(S) = (\text{HardViolations}(S) \times 1000.0) + \text{SoftPenalty}(S)$$
$$\text{Quality Score (\%)} = \frac{100.0}{1.0 + 0.005 \times \text{Cost}(S)}$$

If any hard constraint is violated, fitness is capped below 35.0%. A clash-free valid schedule yields 0 hard violations and high fitness ($\ge 85\%$).

### C. Neighbor Generation & Steepest Descent
1. **Conflict-Directed Selection:** When hard clashes exist, the algorithm picks variables involved in faculty, batch, or room double-bookings.
2. **Neighborhood Evaluation:** It evaluates candidate alternative placements in the variable's feasible domain.
3. **Acceptance Criterion:** A neighbor state $S'$ is accepted if and only if $\text{Cost}(S') < \text{Cost}(S)$.

### D. Multi-Start Random Restarts (Escaping Local Optima)
- If the search reaches a local plateau or local minimum where no neighbor improves the cost after `maxStagnation` steps (e.g. 80 steps), it triggers a **Random Restart**.
- A new candidate timetable is reseeded while tracking the `globalBest` timetable across all restarts.
- This overcomes the classic local optimum pitfall of simple hill climbing.

### E. Independent Constraint Validation
At termination, the timetable is independently evaluated by `ScheduleValidator`:
- **Hard Constraints (0 violations required for `VALID` status):**
  1. No Faculty Double-Booking
  2. No Student Batch Double-Booking
  3. No Room Double-Booking
  4. Room Capacity Requirements
  5. Laboratory Compatibility (Practical classes in labs)
  6. Faculty Available Days & Slots
  7. Non-break working hours
  8. Full session coverage
- **Soft Constraints (Minimized by local search):**
  1. Student Timetable Idle Gaps (10 pts/gap)
  2. Excessive Consecutive Faculty Teaching ($>3$ periods, 15 pts)
  3. Faculty Daily Overload ($>4$ periods/day, 12 pts)
  4. Consecutive Room Changes for Students (4 pts)
  5. Faculty Non-Preferred Slots (5 pts)

---

## 4. Simple Viva Q&A Guide (For College Evaluation)

### Q1: What is Hill Climbing and how does it work here?
> **Answer:** Hill Climbing is a local search optimization algorithm. In our system, the state is a complete candidate timetable. The algorithm measures how many clashes exist using an objective cost function. In each iteration, it explores neighbor schedules by shifting a conflicted lecture to another valid time slot or room, strictly accepting changes that reduce clashes and gaps until a clash-free valid timetable is reached.

### Q2: What happens if Hill Climbing gets stuck in a local optimum?
> **Answer:** Pure hill climbing can get stuck on a plateau or local minimum where 1 or 2 clashes remain. We overcome this using **Multi-Start Random Restarts**: after a threshold of consecutive moves without improvement (stagnation), the algorithm reseeds a fresh initial schedule while remembering the best solution found so far.

### Q3: Why is Hill Climbing better suited than brute-force search?
> **Answer:** For a typical college semester with 40 sessions and 30 time-slot/room combinations, the brute-force search space is $30^{40} \approx 10^{59}$ combinations, which is impossible to compute. Hill Climbing navigates directly towards conflict-free regions in milliseconds ($<100\text{ ms}$).

### Q4: How do you guarantee that an invalid schedule is never approved?
> **Answer:** The system uses an independent `ScheduleValidator`. If any hard constraint remains violated, the system honestly marks the timetable as `INVALID` or `INFEASIBLE` with diagnostic violation notes. It is **never** falsely marked as valid.

---

## 5. Setup & Running the Application

### Option 1: Instant Run with In-Memory Database (Zero-Config)
The application defaults to the `dev` profile with an embedded H2 database pre-seeded with a complete university dataset.

```bash
# 1. Navigate to the project directory
cd "/Users/sakshamjain/Desktop/aisc Priyanshi ji"

# 2. Run automated test suite (19 tests)
mvn test

# 3. Start Spring Boot Application
mvn spring-boot:run
```

Access the web interface at:  
**`http://localhost:8080`**

---

### Option 2: Running with MySQL 8

1. **Create MySQL Database & User:**
   ```bash
   mysql -u root -p < database-setup.sql
   ```

2. **Configure Environment Variables:**
   ```bash
   export SPRING_PROFILES_ACTIVE=mysql
   export DB_HOST=localhost
   export DB_PORT=3306
   export DB_NAME=smartsched_db
   export DB_USERNAME=root
   export DB_PASSWORD=your_mysql_password
   ```

3. **Run Application:**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

---

## 6. REST API Reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/dashboard/summary` | Live university statistics, recent solver runs, status |
| `POST` | `/api/schedules/generate` | Generate timetable using Hill Climbing optimizer |
| `GET` | `/api/schedules/{id}` | Retrieve complete schedule with all assignments and metrics |
| `PUT` | `/api/schedules/{id}/assignments/{assignmentId}` | Manually move session (enforces hard constraint validation) |
| `GET` | `/api/schedules/{id}/export.csv` | Export weekly timetable to CSV |
| `POST` | `/api/exams/generate` | Generate conflict-free examination schedule |
| `GET` | `/api/exams/schedules` | Retrieve examination timetable |
| `GET` | `/api/exams/export.csv` | Export examination timetable to CSV |
| `POST` | `/api/demo/load?scenario=NORMAL\|CONSTRAINED\|IMPOSSIBLE` | Switch between university test scenarios |

---

## 7. 5-Minute Live Viva Demonstration Script

1. **Open the Dashboard (`http://localhost:8080`):**
   - Highlight the 11 Faculty, 14 Subjects, 4 Batches, and 7 Rooms/Labs preloaded in the system.
2. **Generate Timetable via Hill Climbing (`AI Generator` Tab):**
   - Click **Generate Timetable (Hill Climbing)**.
   - Show that within $<200$ ms, Hill Climbing explores candidate configurations, eliminates clashes, and produces a **100% VALID** schedule with 0 hard conflicts.
   - Point out the **Fitness Optimization Curve** chart showing how fitness climbed to $>90\%$ across iterations.
3. **Inspect the Weekly Timetable Grid (`Timetable Grid` Tab):**
   - Filter by Batch `CSE-5A` or `Dr. Rajesh Sharma`.
   - Point out color-coded Theory (Lec) and Practical (Lab) sessions, lunch break separation, and room allocation.
   - Click on any session card to demonstrate **Manual Editing with Hard Constraint Validation**: Attempting to move a session into an occupied room or unavailable slot is safely caught and rejected by the server validator.
4. **Demonstrate Honest Failure Reporting (Viva Highlight):**
   - In the top-right Demo dropdown, click **Load Impossible Dataset (Honest Failure)**.
   - Click **Generate Timetable**.
   - Explain to evaluators how the AI honestly reports `INFEASIBLE` with diagnostic reasons rather than presenting a fake valid timetable.
5. **Examination Scheduler & CSV Export (`Exam Scheduler` Tab):**
   - Click **Generate Exam Schedule** to demonstrate multi-day exam slotting, room capacity checks, and faculty invigilator assignment.
   - Click **Export CSV** and **Print Timetable** to showcase production-ready institutional reporting.
