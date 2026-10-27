// SmartSched AI - Centralized API Service

const API = {
    // Dashboard
    getDashboardSummary: () => API.request('/api/dashboard/summary'),

    // Demo Data
    loadDemoData: (scenario = 'NORMAL') => API.request(`/api/demo/load?scenario=${scenario}`, { method: 'POST' }),

    // Faculty
    getFaculty: () => API.request('/api/faculty'),
    createFaculty: (data) => API.request('/api/faculty', { method: 'POST', body: JSON.stringify(data) }),
    updateFaculty: (id, data) => API.request(`/api/faculty/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteFaculty: (id) => API.request(`/api/faculty/${id}`, { method: 'DELETE' }),

    // Subjects
    getSubjects: () => API.request('/api/subjects'),
    createSubject: (data) => API.request('/api/subjects', { method: 'POST', body: JSON.stringify(data) }),
    updateSubject: (id, data) => API.request(`/api/subjects/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteSubject: (id) => API.request(`/api/subjects/${id}`, { method: 'DELETE' }),

    // Batches
    getBatches: () => API.request('/api/batches'),
    createBatch: (data) => API.request('/api/batches', { method: 'POST', body: JSON.stringify(data) }),
    updateBatch: (id, data) => API.request(`/api/batches/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteBatch: (id) => API.request(`/api/batches/${id}`, { method: 'DELETE' }),

    // Rooms
    getRooms: () => API.request('/api/rooms'),
    createRoom: (data) => API.request('/api/rooms', { method: 'POST', body: JSON.stringify(data) }),
    updateRoom: (id, data) => API.request(`/api/rooms/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    deleteRoom: (id) => API.request(`/api/rooms/${id}`, { method: 'DELETE' }),

    // Time Slots
    getTimeSlots: () => API.request('/api/timeslots'),

    // Timetable Generation
    generateSchedule: (req) => API.request('/api/schedules/generate', { method: 'POST', body: JSON.stringify(req) }),
    getSchedules: (type = '') => API.request(`/api/schedules${type ? '?type=' + type : ''}`),
    getScheduleById: (id) => API.request(`/api/schedules/${id}`),
    deleteSchedule: (id) => API.request(`/api/schedules/${id}`, { method: 'DELETE' }),
    updateAssignment: (schedId, assignId, data) => API.request(`/api/schedules/${schedId}/assignments/${assignId}`, {
        method: 'PUT',
        body: JSON.stringify(data)
    }),

    // Exams
    generateExams: () => API.request('/api/exams/generate', { method: 'POST' }),
    getExams: () => API.request('/api/exams/schedules'),

    // Core Fetch Wrapper
    request: async (url, options = {}) => {
        const defaultHeaders = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };

        const config = {
            ...options,
            headers: {
                ...defaultHeaders,
                ...options.headers
            }
        };

        try {
            const response = await fetch(url, config);
            
            // Handle 204 No Content
            if (response.status === 204) {
                return null;
            }

            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const message = (data && data.message) ? data.message : `HTTP error: ${response.status}`;
                throw new Error(message);
            }

            return data;
        } catch (error) {
            console.error(`API Error on [${url}]:`, error);
            throw error;
        }
    }
};

// UI Notification Toasts
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        container.style.zIndex = '9999';
        document.body.appendChild(container);
    }

    const toastEl = document.createElement('div');
    toastEl.className = `toast align-items-center text-white bg-${type === 'error' ? 'danger' : type} border-0 show`;
    toastEl.setAttribute('role', 'alert');
    toastEl.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                <i class="fas ${type === 'error' ? 'fa-exclamation-triangle' : 'fa-check-circle'} me-2"></i>
                ${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;

    container.appendChild(toastEl);
    setTimeout(() => {
        toastEl.remove();
    }, 4500);
}
