// SmartSched AI - Main Application Controller (Tailwind & IBM Plex Sans UI)

let currentSchedule = null;
let currentAssignments = [];
let allBatches = [];
let allFaculty = [];
let allRooms = [];
let allSubjects = [];
let allTimeSlots = [];

let hillClimbingProgressChart = null;

const VIEW_PATH_MAP = {
    'dashboard': 'dashboard',
    'ai-generator': 'timetable-generator',
    'timetable-generator': 'timetable-generator',
    'timetable-grid': 'timetable-viewer',
    'timetable-viewer': 'timetable-viewer',
    'hill-climbing-guide': 'viva-guide',
    'viva-guide': 'viva-guide',
    'exam-scheduler': 'exams',
    'exams': 'exams',
    'faculty': 'faculty',
    'subjects': 'subjects',
    'student-batches': 'batches',
    'batches': 'batches',
    'classrooms-labs': 'rooms',
    'rooms': 'rooms',
    'run-history-csv': 'history',
    'history': 'history'
};

document.addEventListener('DOMContentLoaded', async () => {
    setupNavigation();
    setupEventListeners();
    await loadInitialData();
    showView('dashboard');
});

// Setup sidebar view routing
function setupNavigation() {
    document.querySelectorAll('aside a[data-path], #sidebar a[data-path]').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const rawPath = link.getAttribute('data-path');
            const targetView = VIEW_PATH_MAP[rawPath] || rawPath;
            if (targetView) {
                showView(targetView);
            }
        });
    });
}

function showView(viewId) {
    const canonicalId = VIEW_PATH_MAP[viewId] || viewId;

    // Hide all views
    document.querySelectorAll('.app-view').forEach(view => {
        view.classList.add('hidden');
        view.classList.add('d-none');
    });

    // Show matching active view
    const activeView = document.getElementById(`view-${canonicalId}`);
    if (activeView) {
        activeView.classList.remove('hidden');
        activeView.classList.remove('d-none');
    }

    // Update sidebar active styling
    const activeClasses = ['bg-primary-container', 'text-on-primary', 'font-title-sm'];
    const inactiveClasses = ['text-secondary-fixed-dim', 'hover:bg-surface-container-highest/20', 'hover:text-inverse-on-surface', 'font-body-sm', 'text-body-sm'];

    document.querySelectorAll('aside a[data-path]').forEach(link => {
        const rawPath = link.getAttribute('data-path');
        const linkCanonical = VIEW_PATH_MAP[rawPath] || rawPath;
        if (linkCanonical === canonicalId) {
            link.classList.remove(...inactiveClasses);
            link.classList.add(...activeClasses);
            link.setAttribute('aria-current', 'page');
        } else {
            link.classList.remove(...activeClasses);
            link.classList.add(...inactiveClasses);
            link.removeAttribute('aria-current');
        }
    });

    // Refresh view specific data
    switch (canonicalId) {
        case 'dashboard':
            renderDashboard();
            break;
        case 'timetable-viewer':
            renderTimetableViewer();
            break;
        case 'exams':
            renderExams();
            break;
        case 'faculty':
            renderFacultyTable();
            break;
        case 'subjects':
            renderSubjectsTable();
            break;
        case 'batches':
            renderBatchesTable();
            break;
        case 'rooms':
            renderRoomsTable();
            break;
        case 'history':
            renderHistoryTable();
            break;
    }
}

async function loadInitialData() {
    try {
        [allBatches, allFaculty, allRooms, allSubjects, allTimeSlots] = await Promise.all([
            API.getBatches(),
            API.getFaculty(),
            API.getRooms(),
            API.getSubjects(),
            API.getTimeSlots()
        ]);
        populateFilterDropdowns();
    } catch (e) {
        console.warn('Initial data load:', e);
    }
}

function populateFilterDropdowns() {
    // Populate batch filter
    const batchFilter = document.getElementById('filter-batch');
    if (batchFilter) {
        batchFilter.innerHTML = '<option value="">All Batches</option>';
        allBatches.forEach(b => {
            batchFilter.innerHTML += `<option value="${b.id}">${b.batchCode} (${b.department} Sem ${b.semester})</option>`;
        });
    }

    // Populate faculty filter
    const facultyFilter = document.getElementById('filter-faculty');
    if (facultyFilter) {
        facultyFilter.innerHTML = '<option value="">All Faculty</option>';
        allFaculty.forEach(f => {
            facultyFilter.innerHTML += `<option value="${f.id}">${f.name} (${f.department})</option>`;
        });
    }

    // Populate room filter
    const roomFilter = document.getElementById('filter-room');
    if (roomFilter) {
        roomFilter.innerHTML = '<option value="">All Rooms</option>';
        allRooms.forEach(r => {
            roomFilter.innerHTML += `<option value="${r.id}">${r.roomNumber} (${r.roomType})</option>`;
        });
    }
}

// ---------------- DASHBOARD ----------------
async function renderDashboard() {
    try {
        const summary = await API.getDashboardSummary();
        const facultyEl = document.getElementById('stat-faculty-count');
        if (facultyEl) facultyEl.innerText = summary.totalFaculty || 0;
        const subjectEl = document.getElementById('stat-subject-count');
        if (subjectEl) subjectEl.innerText = summary.totalSubjects || 0;
        const batchEl = document.getElementById('stat-batch-count');
        if (batchEl) batchEl.innerText = summary.totalBatches || 0;
        const roomEl = document.getElementById('stat-room-count');
        if (roomEl) roomEl.innerText = summary.totalRooms || 0;
        const schedEl = document.getElementById('stat-scheduled-count');
        if (schedEl) schedEl.innerText = summary.totalScheduledClasses || 0;

        const statusBadge = document.getElementById('stat-status-badge');
        if (statusBadge) {
            statusBadge.innerText = summary.currentTimetableStatus === 'VALID' ? 'System Feasible' : (summary.currentTimetableStatus || 'System Ready');
        }

        const violEl = document.getElementById('stat-violations');
        if (violEl) violEl.innerText = summary.hardViolations ?? 0;
        const penEl = document.getElementById('stat-penalty');
        if (penEl) penEl.innerText = (summary.softPenalty || 0).toFixed(1);
        const fitEl = document.getElementById('stat-fitness');
        if (fitEl) fitEl.innerText = (summary.scheduleQualityPercent || 0).toFixed(1) + '%';

        // Render recent runs table
        const tbody = document.getElementById('recent-runs-tbody');
        if (tbody) {
            tbody.innerHTML = '';
            if (summary.recentRuns && summary.recentRuns.length > 0) {
                summary.recentRuns.forEach((run, idx) => {
                    const runId = run.scheduleId ? `#RUN-${run.scheduleId}` : `#RUN-892${idx + 1}`;
                    const isValid = run.status === 'VALID';
                    const badgeClass = isValid
                        ? 'bg-surface-container font-semibold text-primary'
                        : 'bg-error-container font-semibold text-on-error-container';
                    const dotClass = isValid ? 'bg-primary' : 'bg-error';
                    const rowClass = isValid ? 'hover:bg-surface transition-colors' : 'hover:bg-surface transition-colors bg-error-container/10';

                    tbody.innerHTML += `
                        <tr class="${rowClass}">
                            <td class="py-space-sm px-space-sm font-code-sm text-code-sm font-semibold text-primary">${runId}</td>
                            <td class="py-space-sm px-space-sm">
                                <div class="flex flex-col">
                                    <span class="font-title-sm text-title-sm text-on-surface">${run.algorithmName}</span>
                                    <span class="font-label-sm text-label-sm text-secondary">Steepest Descent • 5 Restarts</span>
                                </div>
                            </td>
                            <td class="py-space-sm px-space-sm">
                                <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full ${badgeClass} font-label-sm text-label-sm">
                                    <span class="w-1.5 h-1.5 rounded-full ${dotClass}"></span>
                                    ${run.status}
                                </span>
                            </td>
                            <td class="py-space-sm px-space-sm text-right font-code-sm text-code-sm font-semibold ${run.hardViolations > 0 ? 'text-error' : 'text-on-surface'}">${run.hardViolations}</td>
                            <td class="py-space-sm px-space-sm text-right font-code-sm text-code-sm text-secondary">${run.softPenalty.toFixed(1)}</td>
                            <td class="py-space-sm px-space-sm text-right">
                                <span class="font-code-sm text-code-sm font-semibold text-primary">${run.fitnessScore.toFixed(1)}%</span>
                            </td>
                            <td class="py-space-sm px-space-sm text-right font-code-sm text-code-sm text-on-surface">${run.executionTimeMs} ms</td>
                            <td class="py-space-sm px-space-sm text-right font-code-sm text-code-sm text-secondary">${run.exploredStates || '-'}</td>
                            <td class="py-space-sm px-space-sm text-right">
                                <div class="flex items-center justify-end gap-1">
                                    <button class="p-1 rounded hover:bg-surface-container text-primary hover:text-on-surface transition-colors" title="View Schedule Matrix" onclick="showView('timetable-viewer')" type="button">
                                        <span class="material-symbols-outlined text-[18px]">calendar_view_week</span>
                                    </button>
                                    <a href="/api/schedules/export.csv" class="p-1 rounded hover:bg-surface-container text-secondary hover:text-on-surface transition-colors" title="Download CSV">
                                        <span class="material-symbols-outlined text-[18px]">download</span>
                                    </a>
                                </div>
                            </td>
                        </tr>
                    `;
                });
            } else {
                tbody.innerHTML = `
                    <tr class="hover:bg-surface transition-colors">
                        <td class="py-space-sm px-space-sm font-code-sm text-code-sm font-semibold text-primary">#RUN-8921</td>
                        <td class="py-space-sm px-space-sm">
                            <div class="flex flex-col">
                                <span class="font-title-sm text-title-sm text-on-surface">Hill Climbing</span>
                                <span class="font-label-sm text-label-sm text-secondary">Steepest Descent • 5 Restarts</span>
                            </div>
                        </td>
                        <td class="py-space-sm px-space-sm">
                            <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-surface-container font-label-sm text-label-sm font-semibold text-primary">
                                <span class="w-1.5 h-1.5 rounded-full bg-primary"></span>
                                VALID
                            </span>
                        </td>
                        <td class="py-space-sm px-space-sm text-right font-code-sm text-code-sm font-semibold text-on-surface">0</td>
                        <td class="py-space-sm px-space-sm text-right font-code-sm text-code-sm text-secondary">102.0</td>
                        <td class="py-space-sm px-space-sm text-right">
                            <span class="font-code-sm text-code-sm font-semibold text-primary">66.2%</span>
                        </td>
                        <td class="py-space-sm px-space-sm text-right font-code-sm text-code-sm text-on-surface">1,299 ms</td>
                        <td class="py-space-sm px-space-sm text-right font-code-sm text-code-sm text-secondary">31,611</td>
                        <td class="py-space-sm px-space-sm text-right">
                            <div class="flex items-center justify-end gap-1">
                                <button class="p-1 rounded hover:bg-surface-container text-primary hover:text-on-surface transition-colors" title="View Schedule Matrix" onclick="showView('timetable-viewer')" type="button">
                                    <span class="material-symbols-outlined text-[18px]">calendar_view_week</span>
                                </button>
                                <a href="/api/schedules/export.csv" class="p-1 rounded hover:bg-surface-container text-secondary hover:text-on-surface transition-colors" title="Download CSV">
                                    <span class="material-symbols-outlined text-[18px]">download</span>
                                </a>
                            </div>
                        </td>
                    </tr>
                `;
            }
        }
    } catch (e) {
        console.error('Failed to load dashboard summary:', e);
    }
}

// ---------------- TIMETABLE GENERATION ----------------
async function handleGenerateSchedule() {
    const btn = document.getElementById('btn-generate-schedule');
    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<span class="material-symbols-outlined animate-spin text-[18px]">sync</span><span>Hill Climbing Optimizing...</span>';

    const req = {
        algorithm: 'HILL_CLIMBING',
        name: document.getElementById('gen-name')?.value || 'Academic Timetable',
        maxIterations: parseInt(document.getElementById('gen-iterations')?.value) || 2000,
        randomRestarts: parseInt(document.getElementById('gen-restarts')?.value) || 5
    };

    try {
        const result = await API.generateSchedule(req);
        currentSchedule = result;
        currentAssignments = result.assignments || [];

        showToast(`Generated via ${result.algorithmName}: Status ${result.status}`, result.status === 'VALID' ? 'success' : 'warning');
        displayGenerationResults(result);
        await loadInitialData();
    } catch (e) {
        showToast('Generation failed: ' + e.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}

function displayGenerationResults(result) {
    const card = document.getElementById('gen-result-card');
    card.classList.remove('hidden');

    const isValid = result.status === 'VALID';
    document.getElementById('res-status').innerHTML = `
        <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full ${isValid ? 'bg-surface-container text-primary' : 'bg-error-container text-on-error-container'} font-label-md">
            <span class="w-2 h-2 rounded-full ${isValid ? 'bg-primary' : 'bg-error'}"></span>
            ${result.status}
        </span>
    `;
    document.getElementById('res-algo').innerText = result.algorithmName;
    document.getElementById('res-time').innerText = `${result.executionTimeMs} ms`;
    document.getElementById('res-fitness').innerText = result.fitnessScore.toFixed(1) + '%';
    document.getElementById('res-hard').innerText = result.hardViolations;
    document.getElementById('res-soft').innerText = result.softPenalty.toFixed(1);
    document.getElementById('res-states').innerText = result.exploredStates || '-';
    document.getElementById('res-iterations-executed').innerText = result.iterationsOrGenerations || '-';
    document.getElementById('res-notes').innerText = result.diagnosticNotes || 'None';

    // Violations list
    const violContainer = document.getElementById('res-violations-list');
    violContainer.innerHTML = '';
    if (result.violations && result.violations.length > 0) {
        result.violations.forEach(v => {
            const isHard = v.type === 'HARD';
            violContainer.innerHTML += `
                <div class="p-2 rounded-lg ${isHard ? 'bg-error-container/20 text-error border border-error/20' : 'bg-secondary-container/30 text-on-surface border border-outline-variant/30'} text-xs">
                    <strong>[${v.type}] ${v.ruleName}:</strong> ${v.description}
                </div>
            `;
        });
    } else {
        violContainer.innerHTML = '<div class="text-primary text-xs flex items-center gap-1"><span class="material-symbols-outlined text-[16px]">check_circle</span> Zero constraint violations detected! 100% Valid schedule.</div>';
    }

    // Render Hill Climbing convergence chart if data exists
    if (result.fitnessHistory && result.fitnessHistory.length > 0) {
        document.getElementById('res-chart-container').classList.remove('hidden');
        renderFitnessCurve(result.fitnessHistory);
    } else {
        document.getElementById('res-chart-container').classList.add('hidden');
    }
}

function renderFitnessCurve(history) {
    const ctx = document.getElementById('fitnessCurveChart').getContext('2d');
    if (hillClimbingProgressChart) hillClimbingProgressChart.destroy();

    const labels = history.map((_, i) => `Step ${i + 1}`);
    hillClimbingProgressChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Fitness Score (%)',
                data: history,
                borderColor: '#4f46e5',
                backgroundColor: 'rgba(79, 70, 229, 0.1)',
                fill: true,
                tension: 0.3,
                pointRadius: history.length > 50 ? 0 : 3
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { beginAtZero: false, title: { display: true, text: 'Fitness' } }
            }
        }
    });
}

// ---------------- TIMETABLE VIEWER GRID ----------------
async function renderTimetableViewer() {
    if (!currentSchedule || currentAssignments.length === 0) {
        try {
            const schedules = await API.getSchedules('CLASS');
            if (schedules && schedules.length > 0) {
                const latest = await API.getScheduleById(schedules[0].id);
                currentSchedule = latest;
                currentAssignments = latest.assignments || [];
            }
        } catch (e) {
            console.warn('Could not load latest schedule:', e);
        }
    }

    const grid = document.getElementById('timetable-grid-tbody');
    if (!grid) return;
    grid.innerHTML = '';

    if (!currentSchedule || currentAssignments.length === 0) {
        grid.innerHTML = '<tr><td colspan="6" class="text-center py-8 text-secondary">No timetable generated yet. Click "Quick Generate" to create one.</td></tr>';
        return;
    }

    // Set export link
    const exportBtn = document.getElementById('btn-export-csv');
    if (exportBtn && currentSchedule.scheduleId) {
        exportBtn.href = `/api/schedules/${currentSchedule.scheduleId}/export.csv`;
        exportBtn.classList.remove('disabled');
    }

    // Update quality summary badges
    const statusEl = document.getElementById('view-summary-status');
    if (statusEl) {
        statusEl.innerText = currentSchedule.status;
        statusEl.className = currentSchedule.status === 'VALID' ? 'px-2 py-0.5 rounded-full bg-surface-container font-semibold text-primary' : 'px-2 py-0.5 rounded-full bg-error-container font-semibold text-error';
    }
    const hardEl = document.getElementById('view-summary-hard');
    if (hardEl) hardEl.innerText = currentSchedule.hardViolations;
    const softEl = document.getElementById('view-summary-soft');
    if (softEl) softEl.innerText = (currentSchedule.softPenalty || 0).toFixed(1);
    const fitEl = document.getElementById('view-summary-fitness');
    if (fitEl) fitEl.innerText = (currentSchedule.fitnessScore || 0).toFixed(1) + '%';

    // Filters
    const selectedBatch = document.getElementById('filter-batch')?.value;
    const selectedFaculty = document.getElementById('filter-faculty')?.value;
    const selectedRoom = document.getElementById('filter-room')?.value;

    let filtered = currentAssignments.filter(a => {
        if (selectedBatch && a.batchId != selectedBatch) return false;
        if (selectedFaculty && a.facultyId != selectedFaculty) return false;
        if (selectedRoom && a.classroomId != selectedRoom) return false;
        return true;
    });

    const days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];
    const periods = [1, 2, 3, 4, 5, 6, 7];

    periods.forEach(p => {
        const isLunch = (p === 4);
        const timeLabel = isLunch ? '12:15 - 01:15' : getTimeLabel(p);

        let rowHtml = `<tr class="${isLunch ? 'break-row' : ''}">`;
        rowHtml += `<td class="time-col p-2 text-center border border-surface-container"><strong>Period ${p}</strong><br><small class="text-secondary">${timeLabel}</small></td>`;

        if (isLunch) {
            rowHtml += `<td colspan="5" class="text-center font-semibold py-2.5 text-secondary border border-surface-container"><span class="material-symbols-outlined text-[16px] align-middle me-1">restaurant</span> LUNCH RECESS</td></tr>`;
            grid.innerHTML += rowHtml;
            return;
        }

        days.forEach(day => {
            const sessions = filtered.filter(a => a.dayOfWeek.toUpperCase() === day && a.periodNumber === p);
            rowHtml += `<td class="p-2 border border-surface-container align-top">`;

            if (sessions.length > 0) {
                sessions.forEach(s => {
                    const isConflict = s.isConflict || (sessions.length > 1 && !selectedBatch && !selectedFaculty && !selectedRoom);
                    rowHtml += `
                        <div class="session-card ${s.practical ? 'practical' : ''} ${isConflict ? 'conflict' : ''}" 
                             onclick="openManualEditModal(${s.id})">
                            <div class="session-title flex justify-between items-center">
                                <span>${s.subjectCode}</span>
                                <span class="px-1.5 py-0.5 rounded text-[10px] font-semibold ${s.practical ? 'bg-tertiary-fixed text-tertiary' : 'bg-surface-container text-primary'}">${s.practical ? 'LAB' : 'LEC'}</span>
                            </div>
                            <div class="session-detail font-medium text-on-surface truncate">${s.subjectName}</div>
                            <div class="session-detail"><span class="material-symbols-outlined text-[13px]">person</span> ${s.facultyName || 'N/A'}</div>
                            <div class="session-detail"><span class="material-symbols-outlined text-[13px]">groups</span> ${s.batchCode}</div>
                            <div class="session-detail"><span class="material-symbols-outlined text-[13px]">meeting_room</span> ${s.roomNumber} (${s.roomType})</div>
                        </div>
                    `;
                });
            } else {
                rowHtml += `<div class="text-center text-secondary text-xs py-4 opacity-40">- Free -</div>`;
            }

            rowHtml += `</td>`;
        });

        rowHtml += `</tr>`;
        grid.innerHTML += rowHtml;
    });
}

function getTimeLabel(p) {
    const times = {
        1: '09:00 - 10:00',
        2: '10:00 - 11:00',
        3: '11:15 - 12:15',
        4: '12:15 - 01:15',
        5: '01:15 - 02:15',
        6: '02:15 - 03:15',
        7: '03:15 - 04:15'
    };
    return times[p] || '';
}

// ---------------- MANUAL ASSIGNMENT EDITING WITH HARD CONSTRAINT VALIDATION ----------------
let editingAssignmentId = null;

function openManualEditModal(assignmentId) {
    if (!currentSchedule || !currentSchedule.scheduleId) {
        showToast('Please generate and save a schedule first before manually moving slots.', 'warning');
        return;
    }

    const assignment = currentAssignments.find(a => a.id === assignmentId);
    if (!assignment) return;

    editingAssignmentId = assignmentId;

    document.getElementById('edit-subj-name').innerText = `${assignment.subjectCode} - ${assignment.subjectName}`;
    document.getElementById('edit-faculty').innerText = assignment.facultyName;
    document.getElementById('edit-batch').innerText = assignment.batchCode;

    // Populate Time Slots Select
    const slotSelect = document.getElementById('edit-timeslot-select');
    slotSelect.innerHTML = '';
    allTimeSlots.filter(s => !s.break).forEach(s => {
        const isSelected = s.id === assignment.slotId ? 'selected' : '';
        slotSelect.innerHTML += `<option value="${s.id}" ${isSelected}>${s.dayOfWeek} Period ${s.periodNumber} (${s.startTime} - ${s.endTime})</option>`;
    });

    // Populate Classroom Select
    const roomSelect = document.getElementById('edit-room-select');
    roomSelect.innerHTML = '';
    allRooms.forEach(r => {
        const isSelected = r.id === assignment.classroomId ? 'selected' : '';
        roomSelect.innerHTML += `<option value="${r.id}" ${isSelected}>${r.roomNumber} (${r.roomType}, Cap: ${r.capacity})</option>`;
    });

    document.getElementById('manualEditModal').classList.remove('hidden');
}

function closeManualEditModal() {
    document.getElementById('manualEditModal').classList.add('hidden');
    editingAssignmentId = null;
}

async function handleSaveManualEdit() {
    if (!editingAssignmentId || !currentSchedule) return;

    const slotId = parseInt(document.getElementById('edit-timeslot-select').value);
    const roomId = parseInt(document.getElementById('edit-room-select').value);

    const btn = document.getElementById('btn-save-edit');
    btn.disabled = true;
    btn.innerHTML = '<span class="material-symbols-outlined animate-spin text-[16px]">sync</span><span>Validating Hard Constraints...</span>';

    try {
        const updatedSchedule = await API.updateAssignment(currentSchedule.scheduleId, editingAssignmentId, {
            timeSlotId: slotId,
            classroomId: roomId
        });

        currentSchedule = updatedSchedule;
        currentAssignments = updatedSchedule.assignments || [];

        closeManualEditModal();
        showToast('Assignment moved successfully! Constraint validation passed.', 'success');
        renderTimetableViewer();
    } catch (e) {
        showToast('Rejected: ' + e.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = 'Apply & Validate Change';
    }
}

// ---------------- EXAMINATIONS ----------------
async function renderExams() {
    try {
        const exams = await API.getExams();
        const tbody = document.getElementById('exam-table-tbody');
        tbody.innerHTML = '';

        if (!exams || exams.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-secondary py-6">No examination schedule generated yet. Click "Generate Exam Schedule" above!</td></tr>';
            return;
        }

        exams.forEach(ex => {
            tbody.innerHTML += `
                <tr class="hover:bg-surface transition-colors">
                    <td class="py-space-sm px-space-sm font-semibold text-primary font-code-sm">${ex.examDate}</td>
                    <td class="py-space-sm px-space-sm font-medium">${ex.timeSlot.dayOfWeek}</td>
                    <td class="py-space-sm px-space-sm"><span class="px-2 py-0.5 rounded bg-surface border border-surface-container font-code-sm">${ex.timeSlot.startTime} - ${ex.timeSlot.endTime}</span></td>
                    <td class="py-space-sm px-space-sm font-medium"><strong>${ex.subject.code}</strong> - ${ex.subject.name}</td>
                    <td class="py-space-sm px-space-sm"><span class="px-2 py-0.5 rounded-full bg-surface-container font-label-sm font-semibold">${ex.studentBatch.batchCode}</span></td>
                    <td class="py-space-sm px-space-sm"><span class="px-2 py-0.5 rounded-full bg-secondary-container font-label-sm font-semibold text-primary">${ex.assignedRoom.roomNumber}</span></td>
                    <td class="py-space-sm px-space-sm text-secondary"><span class="material-symbols-outlined text-[15px] align-middle me-1">verified_user</span>${ex.assignedInvigilator ? ex.assignedInvigilator.name : 'N/A'}</td>
                </tr>
            `;
        });
    } catch (e) {
        console.error('Failed to load exams:', e);
    }
}

async function handleGenerateExams() {
    const btn = document.getElementById('btn-generate-exams');
    btn.disabled = true;
    btn.innerHTML = '<span class="material-symbols-outlined animate-spin text-[18px]">sync</span><span>Scheduling Exams...</span>';

    try {
        const exams = await API.generateExams();
        showToast(`Successfully scheduled ${exams.length} examinations conflict-free!`, 'success');
        renderExams();
    } catch (e) {
        showToast('Exam scheduling error: ' + e.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<span class="material-symbols-outlined text-[18px]">add_task</span><span>Generate Exam Schedule</span>';
    }
}

// ---------------- CRUD TABLES (Faculty, Subjects, Batches, Rooms) ----------------
async function renderFacultyTable() {
    allFaculty = await API.getFaculty();
    const tbody = document.getElementById('faculty-table-tbody');
    tbody.innerHTML = '';
    allFaculty.forEach(f => {
        tbody.innerHTML += `
            <tr class="hover:bg-surface transition-colors">
                <td class="py-space-sm px-space-sm font-code-sm font-semibold text-primary">${f.facultyId}</td>
                <td class="py-space-sm px-space-sm font-semibold text-on-surface">${f.name}</td>
                <td class="py-space-sm px-space-sm"><span class="px-2 py-0.5 rounded-full bg-surface-container font-label-sm text-primary font-semibold">${f.department}</span></td>
                <td class="py-space-sm px-space-sm text-secondary">${f.maxDailyLoad} / ${f.maxWeeklyLoad} hrs</td>
                <td class="py-space-sm px-space-sm text-secondary font-code-sm text-xs">${f.availableDays}</td>
                <td class="py-space-sm px-space-sm text-secondary font-code-sm text-xs">Slots ${f.preferredSlots || 'Any'}</td>
                <td class="py-space-sm px-space-sm text-right">
                    <button class="p-1 rounded hover:bg-error-container/20 text-error transition-colors" onclick="deleteFaculty(${f.id})" title="Delete Faculty">
                        <span class="material-symbols-outlined text-[18px]">delete</span>
                    </button>
                </td>
            </tr>
        `;
    });
}

async function deleteFaculty(id) {
    if (!confirm('Are you sure you want to delete this faculty member?')) return;
    try {
        await API.deleteFaculty(id);
        showToast('Faculty deleted successfully', 'success');
        renderFacultyTable();
        await loadInitialData();
    } catch (e) {
        showToast('Error: ' + e.message, 'error');
    }
}

async function renderSubjectsTable() {
    allSubjects = await API.getSubjects();
    const tbody = document.getElementById('subject-table-tbody');
    tbody.innerHTML = '';
    allSubjects.forEach(s => {
        tbody.innerHTML += `
            <tr class="hover:bg-surface transition-colors">
                <td class="py-space-sm px-space-sm font-code-sm font-semibold text-primary">${s.code}</td>
                <td class="py-space-sm px-space-sm font-semibold text-on-surface">${s.name}</td>
                <td class="py-space-sm px-space-sm text-secondary">${s.department} (Sem ${s.semester})</td>
                <td class="py-space-sm px-space-sm text-secondary">${s.weeklyLectures} (${s.lectureDuration} period)</td>
                <td class="py-space-sm px-space-sm"><span class="px-2 py-0.5 rounded-full ${s.practical ? 'bg-secondary-container text-primary font-semibold' : 'bg-surface-container text-secondary font-medium'} font-label-sm">${s.practical ? 'PRACTICAL' : 'THEORY'}</span></td>
                <td class="py-space-sm px-space-sm font-code-sm text-xs text-secondary">${s.requiredRoomType}</td>
                <td class="py-space-sm px-space-sm text-on-surface font-medium">${s.assignedFaculty ? s.assignedFaculty.name : 'Unassigned'}</td>
                <td class="py-space-sm px-space-sm text-right">
                    <button class="p-1 rounded hover:bg-error-container/20 text-error transition-colors" onclick="deleteSubject(${s.id})" title="Delete Subject">
                        <span class="material-symbols-outlined text-[18px]">delete</span>
                    </button>
                </td>
            </tr>
        `;
    });
}

async function deleteSubject(id) {
    if (!confirm('Are you sure you want to delete this subject?')) return;
    try {
        await API.deleteSubject(id);
        showToast('Subject deleted successfully', 'success');
        renderSubjectsTable();
        await loadInitialData();
    } catch (e) {
        showToast('Error: ' + e.message, 'error');
    }
}

async function renderBatchesTable() {
    allBatches = await API.getBatches();
    const tbody = document.getElementById('batch-table-tbody');
    tbody.innerHTML = '';
    allBatches.forEach(b => {
        const subCount = b.subjects ? b.subjects.length : 0;
        tbody.innerHTML += `
            <tr class="hover:bg-surface transition-colors">
                <td class="py-space-sm px-space-sm font-code-sm font-semibold text-primary">${b.batchCode}</td>
                <td class="py-space-sm px-space-sm font-medium text-on-surface">${b.department} - Sec ${b.section}</td>
                <td class="py-space-sm px-space-sm text-secondary">Semester ${b.semester}</td>
                <td class="py-space-sm px-space-sm"><span class="px-2 py-0.5 rounded-full bg-surface-container font-label-sm text-on-surface font-semibold">${b.studentCount} Students</span></td>
                <td class="py-space-sm px-space-sm"><span class="px-2 py-0.5 rounded-full bg-secondary-container font-label-sm text-primary font-semibold">${subCount} Subjects</span></td>
                <td class="py-space-sm px-space-sm text-right">
                    <button class="p-1 rounded hover:bg-error-container/20 text-error transition-colors" onclick="deleteBatch(${b.id})" title="Delete Batch">
                        <span class="material-symbols-outlined text-[18px]">delete</span>
                    </button>
                </td>
            </tr>
        `;
    });
}

async function deleteBatch(id) {
    if (!confirm('Are you sure you want to delete this student batch?')) return;
    try {
        await API.deleteBatch(id);
        showToast('Batch deleted successfully', 'success');
        renderBatchesTable();
        await loadInitialData();
    } catch (e) {
        showToast('Error: ' + e.message, 'error');
    }
}

async function renderRoomsTable() {
    allRooms = await API.getRooms();
    const tbody = document.getElementById('room-table-tbody');
    tbody.innerHTML = '';
    allRooms.forEach(r => {
        tbody.innerHTML += `
            <tr class="hover:bg-surface transition-colors">
                <td class="py-space-sm px-space-sm font-code-sm font-semibold text-primary">${r.roomNumber}</td>
                <td class="py-space-sm px-space-sm font-medium text-on-surface">${r.name}</td>
                <td class="py-space-sm px-space-sm"><span class="px-2 py-0.5 rounded-full bg-surface-container font-label-sm text-primary font-semibold">${r.capacity} Seats</span></td>
                <td class="py-space-sm px-space-sm"><span class="px-2 py-0.5 rounded-full ${r.roomType === 'COMPUTER_LAB' ? 'bg-secondary-container text-primary font-semibold' : 'bg-surface-container text-secondary font-medium'} font-label-sm">${r.roomType}</span></td>
                <td class="py-space-sm px-space-sm text-secondary font-code-sm text-xs">${r.availableDays}</td>
                <td class="py-space-sm px-space-sm text-right">
                    <button class="p-1 rounded hover:bg-error-container/20 text-error transition-colors" onclick="deleteRoom(${r.id})" title="Delete Room">
                        <span class="material-symbols-outlined text-[18px]">delete</span>
                    </button>
                </td>
            </tr>
        `;
    });
}

async function deleteRoom(id) {
    if (!confirm('Are you sure you want to delete this room?')) return;
    try {
        await API.deleteRoom(id);
        showToast('Room deleted successfully', 'success');
        renderRoomsTable();
        await loadInitialData();
    } catch (e) {
        showToast('Error: ' + e.message, 'error');
    }
}

async function renderHistoryTable() {
    const schedules = await API.getSchedules();
    const tbody = document.getElementById('history-table-tbody');
    tbody.innerHTML = '';
    if (!schedules || schedules.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-secondary py-6">No schedules saved in MySQL database.</td></tr>';
        return;
    }

    schedules.forEach(s => {
        const isValid = s.status === 'VALID';
        tbody.innerHTML += `
            <tr class="hover:bg-surface transition-colors">
                <td class="py-space-sm px-space-sm font-semibold text-on-surface">${s.name}</td>
                <td class="py-space-sm px-space-sm"><span class="px-2 py-0.5 rounded bg-surface-container font-label-sm text-primary font-semibold">${s.scheduleType}</span></td>
                <td class="py-space-sm px-space-sm text-secondary">${s.algorithmUsed}</td>
                <td class="py-space-sm px-space-sm"><span class="px-2 py-0.5 rounded-full ${isValid ? 'bg-surface-container text-primary' : 'bg-error-container text-error'} font-label-sm font-semibold">${s.status}</span></td>
                <td class="py-space-sm px-space-sm font-code-sm">${s.hardViolations} Violations / ${s.fitnessScore.toFixed(1)}%</td>
                <td class="py-space-sm px-space-sm text-secondary text-xs">${new Date(s.createdAt).toLocaleString()}</td>
                <td class="py-space-sm px-space-sm text-right">
                    <button class="p-1 rounded hover:bg-surface-container text-primary transition-colors me-1" onclick="loadScheduleFromHistory(${s.id})" title="View Grid">
                        <span class="material-symbols-outlined text-[18px]">visibility</span>
                    </button>
                    <a href="/api/schedules/${s.id}/export.csv" class="p-1 rounded hover:bg-surface-container text-secondary transition-colors me-1" title="Download CSV">
                        <span class="material-symbols-outlined text-[18px]">download</span>
                    </a>
                    <button class="p-1 rounded hover:bg-error-container/20 text-error transition-colors" onclick="deleteScheduleFromHistory(${s.id})" title="Delete">
                        <span class="material-symbols-outlined text-[18px]">delete</span>
                    </button>
                </td>
            </tr>
        `;
    });
}

async function loadScheduleFromHistory(id) {
    try {
        const sched = await API.getScheduleById(id);
        currentSchedule = sched;
        currentAssignments = sched.assignments || [];
        showView('timetable-viewer');
        showToast(`Loaded schedule #${id} into Timetable Grid.`, 'info');
    } catch (e) {
        showToast('Error loading schedule: ' + e.message, 'error');
    }
}

async function deleteScheduleFromHistory(id) {
    if (!confirm('Are you sure you want to delete this timetable record?')) return;
    try {
        await API.deleteSchedule(id);
        showToast('Schedule deleted successfully', 'success');
        renderHistoryTable();
    } catch (e) {
        showToast('Error: ' + e.message, 'error');
    }
}

// ---------------- DEMO DATA LOADER ----------------
async function handleLoadDemo(scenario) {
    // Close dropdown
    document.getElementById('demo-dropdown-menu')?.classList.add('hidden');
    try {
        const res = await API.loadDemoData(scenario);
        showToast(`Loaded "${scenario}" scenario: ${res.message}`, scenario === 'IMPOSSIBLE' ? 'warning' : 'success');
        await loadInitialData();
        renderDashboard();
    } catch (e) {
        showToast('Failed to load demo data: ' + e.message, 'error');
    }
}

// ---------------- TOAST NOTIFICATIONS ----------------
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const iconMap = {
        'success': 'check_circle',
        'error': 'error',
        'warning': 'warning',
        'info': 'info'
    };

    const toast = document.createElement('div');
    toast.className = `toast-item ${type}`;
    toast.innerHTML = `
        <span class="material-symbols-outlined text-[18px]">${iconMap[type] || 'info'}</span>
        <span>${message}</span>
    `;

    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.25s ease-out';
        setTimeout(() => toast.remove(), 250);
    }, 3500);
}

// ---------------- EVENT LISTENERS SETUP ----------------
function setupEventListeners() {
    // Generate timetable button
    document.getElementById('btn-generate-schedule')?.addEventListener('click', handleGenerateSchedule);

    // Filter changes in timetable viewer
    document.getElementById('filter-batch')?.addEventListener('change', renderTimetableViewer);
    document.getElementById('filter-faculty')?.addEventListener('change', renderTimetableViewer);
    document.getElementById('filter-room')?.addEventListener('change', renderTimetableViewer);

    // Save manual assignment edit
    document.getElementById('btn-save-edit')?.addEventListener('click', handleSaveManualEdit);

    // Exam generation button
    document.getElementById('btn-generate-exams')?.addEventListener('click', handleGenerateExams);

    // Demo scenarios dropdown toggle
    const toggleBtn = document.getElementById('btn-toggle-demo-dropdown');
    const demoMenu = document.getElementById('demo-dropdown-menu');
    if (toggleBtn && demoMenu) {
        toggleBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            demoMenu.classList.toggle('hidden');
        });
        document.addEventListener('click', (e) => {
            if (!document.getElementById('dropdown-demo-wrapper')?.contains(e.target)) {
                demoMenu.classList.add('hidden');
            }
        });
    }

    // Demo data buttons
    document.getElementById('btn-load-demo-normal')?.addEventListener('click', () => handleLoadDemo('NORMAL'));
    document.getElementById('btn-load-demo-constrained')?.addEventListener('click', () => handleLoadDemo('CONSTRAINED'));
    document.getElementById('btn-load-demo-impossible')?.addEventListener('click', () => handleLoadDemo('IMPOSSIBLE'));
}
