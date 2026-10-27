package com.smartsched.service.algorithm;

import com.smartsched.dto.ScheduleResultDto;
import com.smartsched.dto.TimetableGenerationRequest;
import com.smartsched.entity.*;
import com.smartsched.validation.ScheduleValidator;
import com.smartsched.validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Hill Climbing Scheduling Optimization Algorithm.
 *
 * Implements discrete local search (steepest-descent / coordinate ascent) on
 * the timetable configuration space with random restarts.
 *
 * - Objective Function: Cost = (Hard Violations * 1000.0) + Soft Penalty.
 * - Neighbor Generation: Modifies a placement (slot + room) for conflicted or penalty-causing variables.
 * - Acceptance Criterion: Strictly accepts moves that reduce the overall penalty cost.
 * - Random Restarts: Escapes local plateaus and local optima by reseeding candidate schedules.
 * - Verification: Final schedule is independently verified through ScheduleValidator.
 */
@Service
public class HillClimbingSchedulingAlgorithm implements SchedulingAlgorithm {

    private final ScheduleValidator validator;
    private final Random random = new Random(42);

    public HillClimbingSchedulingAlgorithm(ScheduleValidator validator) {
        this.validator = validator;
    }

    @Override
    public String getAlgorithmName() {
        return "Hill Climbing";
    }

    /**
     * Internal state representation for fast evaluation during search.
     */
    private static class State {
        final SchedulingPlacement[] placements;
        int hardViolations;
        double softPenalty;
        double cost; // Lower is better: (hardViolations * 1000.0) + softPenalty

        State(SchedulingPlacement[] placements) {
            this.placements = placements;
        }
    }

    @Override
    public ScheduleResultDto generateSchedule(
            TimetableGenerationRequest request,
            List<StudentBatch> batches,
            List<Subject> subjects,
            List<Faculty> faculties,
            List<Classroom> classrooms,
            List<TimeSlot> timeSlots) {

        long startTime = System.currentTimeMillis();
        int maxIterations = (request != null && request.getMaxIterations() > 50) ? request.getMaxIterations() : 2000;
        int randomRestarts = (request != null && request.getRandomRestarts() > 0) ? request.getRandomRestarts() : 5;
        String scheduleName = (request != null && request.getName() != null) ? request.getName() : "Academic Timetable";

        List<SchedulingVariable> variables = SchedulingProblemHelper.buildVariables(batches);
        ScheduleResultDto result = new ScheduleResultDto();
        result.setAlgorithmName(getAlgorithmName());
        result.setName(scheduleName);

        if (variables.isEmpty()) {
            result.setStatus("FAILED");
            result.setDiagnosticNotes("No subjects or student batches available to schedule.");
            result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            return result;
        }

        // 1. Precompute feasible domains for all scheduling variables
        List<List<SchedulingPlacement>> domains = new ArrayList<>(variables.size());
        for (SchedulingVariable var : variables) {
            List<SchedulingPlacement> dom = SchedulingProblemHelper.computeFeasibleDomain(var, classrooms, timeSlots);
            if (dom.isEmpty()) {
                result.setStatus("INFEASIBLE");
                result.setDiagnosticNotes("Feasible domain empty for " + var.getSubject().getCode() +
                        " (" + var.getSubject().getName() + "). No compatible classroom or slot matches requirements.");
                result.setHardViolations(1);
                result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
                return result;
            }
            domains.add(dom);
        }

        int varCount = variables.size();
        State globalBest = null;
        long exploredStates = 0;
        int totalIterations = 0;
        List<Double> fitnessHistory = new ArrayList<>();

        int itersPerRestart = Math.max(50, maxIterations / randomRestarts);

        // 2. Multi-start Hill Climbing loop
        for (int restart = 0; restart < randomRestarts; restart++) {
            // Generate initial candidate timetable for this restart
            State current = generateInitialState(variables, domains);
            evaluateState(current, variables);
            exploredStates++;

            if (globalBest == null || current.cost < globalBest.cost) {
                globalBest = copyState(current);
            }

            int stagnationCount = 0;
            int maxStagnation = 80; // Steps without improvement before triggering random restart

            for (int iter = 0; iter < itersPerRestart; iter++) {
                totalIterations++;

                // Pick variable to tweak (prioritize variables in clash or with soft penalties)
                int varIdx = pickVariableToTweak(current, variables);
                List<SchedulingPlacement> dom = domains.get(varIdx);

                SchedulingPlacement originalPlacement = current.placements[varIdx];
                SchedulingPlacement bestNeighborPlacement = null;
                double bestNeighborCost = current.cost;

                // Sample candidate neighbor placements from feasible domain
                int sampleSize = Math.min(dom.size(), 16);
                for (int t = 0; t < sampleSize; t++) {
                    SchedulingPlacement candidate = dom.get(random.nextInt(dom.size()));
                    if (candidate.equals(originalPlacement)) continue;

                    current.placements[varIdx] = candidate;
                    evaluateState(current, variables);
                    exploredStates++;

                    if (current.cost < bestNeighborCost) {
                        bestNeighborCost = current.cost;
                        bestNeighborPlacement = candidate;
                    }
                }

                // Acceptance Criterion: strictly move to improving neighbor
                if (bestNeighborPlacement != null && bestNeighborCost < current.cost) {
                    current.placements[varIdx] = bestNeighborPlacement;
                    current.cost = bestNeighborCost;
                    stagnationCount = 0;

                    if (current.cost < globalBest.cost) {
                        globalBest = copyState(current);
                    }
                } else {
                    // Revert to original placement
                    current.placements[varIdx] = originalPlacement;
                    evaluateState(current, variables);
                    stagnationCount++;
                }

                // Record fitness progression for chart as percentage (0.0% to 100.0%)
                double currentFit = 100.0 / (1.0 + 0.005 * globalBest.cost);
                if (globalBest.hardViolations > 0) {
                    currentFit = Math.min(35.0, currentFit);
                }
                fitnessHistory.add(Math.round(currentFit * 100.0) / 100.0);

                // If perfectly clash-free with optimal soft constraints, or stagnated, restart
                if (globalBest.hardViolations == 0 && globalBest.softPenalty == 0) {
                    break;
                }
                if (stagnationCount >= maxStagnation) {
                    break; // Local optimum / plateau reached -> restart
                }
            }

            // Early exit if global best is fully clash-free and sufficiently optimized
            if (globalBest.hardViolations == 0 && globalBest.softPenalty < 10.0) {
                break;
            }
        }

        long execTime = System.currentTimeMillis() - startTime;
        result.setExecutionTimeMs(execTime);
        result.setExploredStates(exploredStates);
        result.setIterationsOrGenerations(totalIterations);
        result.setFitnessHistory(fitnessHistory);

        // 3. Independent validation of global best solution
        Map<SchedulingVariable, SchedulingPlacement> assignmentMap = new HashMap<>();
        for (int v = 0; v < varCount; v++) {
            assignmentMap.put(variables.get(v), globalBest.placements[v]);
        }
        List<ScheduleAssignment> assignments = SchedulingProblemHelper.toEntityAssignments(assignmentMap, null);
        ValidationResult validation = validator.validate(assignments, batches, varCount);

        result.setAssignments(SchedulingProblemHelper.toDtoAssignments(assignments));
        result.setHardViolations(validation.getHardViolationCount());
        result.setSoftPenalty(validation.getSoftPenalty());
        result.setFitnessScore(validation.getFitnessScore());
        result.setViolations(validation.getViolations());

        if (validation.isValid()) {
            result.setStatus("VALID");
            result.setDiagnosticNotes("Hill Climbing reached a valid clash-free schedule in " +
                    totalIterations + " iterations (" + execTime + " ms).");
        } else {
            result.setStatus("INVALID");
            result.setDiagnosticNotes("Hill Climbing reached a local optimum with " +
                    validation.getHardViolationCount() + " clash(es). Try increasing iterations or random restarts.");
        }

        return result;
    }

    /**
     * Generates an initial candidate state by random sampling from feasible domains.
     */
    private State generateInitialState(List<SchedulingVariable> variables, List<List<SchedulingPlacement>> domains) {
        int n = variables.size();
        SchedulingPlacement[] arr = new SchedulingPlacement[n];
        for (int i = 0; i < n; i++) {
            List<SchedulingPlacement> dom = domains.get(i);
            arr[i] = dom.get(random.nextInt(dom.size()));
        }
        return new State(arr);
    }

    /**
     * Selects a variable to modify. Prioritizes variables currently participating
     * in conflicts to guide search towards feasible regions quickly.
     */
    private int pickVariableToTweak(State state, List<SchedulingVariable> variables) {
        int n = variables.size();
        // 1. Conflict-directed selection
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (SchedulingProblemHelper.hasConflict(variables.get(i), state.placements[i], variables.get(j), state.placements[j])) {
                    return random.nextBoolean() ? i : j;
                }
            }
        }
        // 2. If no hard conflicts, pick random variable to optimize soft constraints
        return random.nextInt(n);
    }

    /**
     * Evaluates candidate state against all hard constraints and soft constraints.
     * Cost = (Hard Violations * 1000.0) + Soft Penalties.
     */
    private void evaluateState(State state, List<SchedulingVariable> variables) {
        int hard = 0;
        double soft = 0.0;
        int n = variables.size();

        // Data structures for soft constraint tracking
        // Batch -> Day -> List of Periods
        Map<Long, Map<String, List<Integer>>> batchDayPeriods = new HashMap<>();
        // Faculty -> Day -> List of Periods
        Map<Long, Map<String, List<Integer>>> facultyDayPeriods = new HashMap<>();
        // Batch -> Day -> List of (Period, RoomId)
        Map<Long, Map<String, List<int[]>>> batchRoomSchedule = new HashMap<>();

        for (int i = 0; i < n; i++) {
            SchedulingVariable v1 = variables.get(i);
            SchedulingPlacement p1 = state.placements[i];

            // Hard constraints: pairwise clashes (room, batch, faculty)
            for (int j = i + 1; j < n; j++) {
                SchedulingVariable v2 = variables.get(j);
                SchedulingPlacement p2 = state.placements[j];

                if (SchedulingProblemHelper.hasConflict(v1, p1, v2, p2)) {
                    hard++;
                }
            }

            // Soft constraint: Faculty slot preference
            Faculty f = v1.getFaculty();
            int startPeriod = p1.getSlot().getPeriodNumber();
            String day = p1.getSlot().getDayOfWeek().toUpperCase();

            if (f != null && f.getPreferredSlots() != null && !f.getPreferredSlots().isEmpty()) {
                if (!f.getPreferredSlots().contains(String.valueOf(startPeriod))) {
                    soft += 5.0;
                }
            }

            // Populate structures for soft evaluation
            int duration = v1.getDuration() > 0 ? v1.getDuration() : 1;
            for (int d = 0; d < duration; d++) {
                int period = startPeriod + d;

                batchDayPeriods.computeIfAbsent(v1.getBatch().getId(), k -> new HashMap<>())
                        .computeIfAbsent(day, k -> new ArrayList<>()).add(period);

                if (f != null) {
                    facultyDayPeriods.computeIfAbsent(f.getId(), k -> new HashMap<>())
                            .computeIfAbsent(day, k -> new ArrayList<>()).add(period);
                }

                batchRoomSchedule.computeIfAbsent(v1.getBatch().getId(), k -> new HashMap<>())
                        .computeIfAbsent(day, k -> new ArrayList<>()).add(new int[]{period, p1.getRoom().getId().intValue()});
            }
        }

        // Soft Constraint 1: Student timetable idle gaps
        for (Map<String, List<Integer>> dayMap : batchDayPeriods.values()) {
            for (List<Integer> periods : dayMap.values()) {
                Collections.sort(periods);
                for (int i = 0; i < periods.size() - 1; i++) {
                    int gap = periods.get(i + 1) - periods.get(i) - 1;
                    if (gap > 0) {
                        soft += gap * 10.0;
                    }
                }
            }
        }

        // Soft Constraint 2: Excessive consecutive faculty teaching (>3 periods)
        for (Map<String, List<Integer>> dayMap : facultyDayPeriods.values()) {
            for (List<Integer> periods : dayMap.values()) {
                Collections.sort(periods);
                int consecutive = 1;
                for (int i = 0; i < periods.size() - 1; i++) {
                    if (periods.get(i + 1) == periods.get(i) + 1) {
                        consecutive++;
                        if (consecutive > 3) {
                            soft += 15.0;
                        }
                    } else {
                        consecutive = 1;
                    }
                }
            }
        }

        // Soft Constraint 3: Faculty daily overload (>4 periods in a single day)
        for (Map<String, List<Integer>> dayMap : facultyDayPeriods.values()) {
            for (List<Integer> periods : dayMap.values()) {
                int dailyLoad = periods.size();
                if (dailyLoad > 4) {
                    soft += (dailyLoad - 4) * 12.0;
                }
            }
        }

        // Soft Constraint 4: Unnecessary room changes for batch on same day
        for (Map<String, List<int[]>> dayMap : batchRoomSchedule.values()) {
            for (List<int[]> sessions : dayMap.values()) {
                sessions.sort(Comparator.comparingInt(a -> a[0]));
                for (int i = 0; i < sessions.size() - 1; i++) {
                    if (sessions.get(i + 1)[0] == sessions.get(i)[0] + 1) {
                        if (sessions.get(i + 1)[1] != sessions.get(i)[1]) {
                            soft += 4.0;
                        }
                    }
                }
            }
        }

        state.hardViolations = hard;
        state.softPenalty = soft;
        state.cost = (hard * 1000.0) + soft;
    }

    private State copyState(State src) {
        SchedulingPlacement[] copy = Arrays.copyOf(src.placements, src.placements.length);
        State s = new State(copy);
        s.hardViolations = src.hardViolations;
        s.softPenalty = src.softPenalty;
        s.cost = src.cost;
        return s;
    }
}
