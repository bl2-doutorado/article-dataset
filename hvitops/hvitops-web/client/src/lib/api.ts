import axios, { AxiosInstance } from 'axios';

const API_BASE_URL = 'http://localhost:8080';

interface AuthResponse {
  token: string;
  user: {
    id: number;
    email: string;
    role: string;
  };
}

interface Appointment {
  id: number;
  patientId: number;
  doctorId: number;
  scheduledAt: string;
  status: 'SCHEDULED' | 'COMPLETED' | 'CANCELLED';
  notes?: string;
  medicalRecordId?: string;
  createdAt: string;
  updatedAt: string;
}

interface Doctor {
  id: number;
  name: string;
  specialty: string;
}

interface AvailableSlot {
  startTime: string;
  endTime: string;
  available: boolean;
}

interface LabTest {
  id: string;
  patientId: number;
  date: string;
  performedAt?: string;
  status: 'scheduled' | 'pending_results' | 'completed';
  items: TestItem[];
}

interface TestItem {
  testType: string;
  result?: string;
  unit?: string;
  referenceRange?: string;
}

interface LabTestType {
  id: string;
  name: string;
  description: string;
}

interface MedicalRecord {
  id: string;
  patientId: number;
  physicianId: number;
  appointmentId?: number;
  date: string;
  diagnosis: string;
  prescriptions: string[];
  clinicalNotes: string;
  createdAt: string;
  updatedAt: string;
}

interface Notification {
  id: string;
  userId: number;
  type: string;
  message: string;
  read: boolean;
  createdAt: string;
}

let apiClient: AxiosInstance;

export function initializeApi(token?: string) {
  apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (token) {
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }

  apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        localStorage.removeItem('token');
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
  );

  return apiClient;
}

export function getApiClient() {
  if (!apiClient) {
    initializeApi(localStorage.getItem('token') || undefined);
  }
  return apiClient;
}

export const appointmentApi = {
  create: (appointment: Partial<Appointment>) =>
    getApiClient().post<Appointment>('/appointments', appointment),
  
  getById: (id: number) =>
    getApiClient().get<Appointment>(`/appointments/${id}`),
  
  getAll: () =>
    getApiClient().get<Appointment[]>('/appointments'),
  
  getByPatient: (patientId: number) =>
    getApiClient().get<Appointment[]>(`/appointments/patient/${patientId}`),
  
  getByDoctor: (doctorId: number) =>
    getApiClient().get<Appointment[]>(`/appointments/doctor/${doctorId}`),
  
  update: (id: number, appointment: Partial<Appointment>) =>
    getApiClient().put<Appointment>(`/appointments/${id}`, appointment),
  
  cancel: (id: number) =>
    getApiClient().delete(`/appointments/${id}`),
  
  getSpecialties: () =>
    getApiClient().get<string[]>('/appointments/specialties/list'),
  
  getDoctorsBySpecialty: (specialty: string) =>
    getApiClient().get<Doctor[]>(`/appointments/doctors/specialty/${specialty}`),
  
  getAvailableSlots: (doctorId: number, startDate: string, endDate: string) =>
    getApiClient().get<AvailableSlot[]>(`/appointments/slots/${doctorId}`, {
      params: { startDate, endDate },
    }),
};

export const labTestApi = {
  create: (test: Partial<LabTest>) =>
    getApiClient().post<LabTest>('/laboratory-tests', test),
  
  getById: (id: string) =>
    getApiClient().get<LabTest>(`/laboratory-tests/${id}`),
  
  getAll: () =>
    getApiClient().get<LabTest[]>('/laboratory-tests'),
  
  getByPatient: (patientId: number) =>
    getApiClient().get<LabTest[]>(`/laboratory-tests/patient/${patientId}`),
  
  update: (id: string, test: Partial<LabTest>) =>
    getApiClient().put<LabTest>(`/laboratory-tests/${id}`, test),
  
  delete: (id: string) =>
    getApiClient().delete(`/laboratory-tests/${id}`),
  
  getTestTypes: () =>
    getApiClient().get<LabTestType[]>('/laboratory-tests/types/list'),
};

export const medicalRecordApi = {
  create: (record: Partial<MedicalRecord>) =>
    getApiClient().post<MedicalRecord>('/records', record),
  
  getById: (id: string) =>
    getApiClient().get<MedicalRecord>(`/records/${id}`),
  
  getAll: () =>
    getApiClient().get<MedicalRecord[]>('/records'),
  
  getByPatient: (patientId: number) =>
    getApiClient().get<MedicalRecord[]>(`/records/patient/${patientId}`),
  
  getByAppointment: (appointmentId: number) =>
    getApiClient().get<MedicalRecord[]>(`/records/appointment/${appointmentId}`),
  
  update: (id: string, record: Partial<MedicalRecord>) =>
    getApiClient().put<MedicalRecord>(`/records/${id}`, record),
  
  delete: (id: string) =>
    getApiClient().delete(`/records/${id}`),
};

export const notificationApi = {
  getByUser: (userId: number) =>
    getApiClient().get<Notification[]>(`/notifications/user/${userId}`),
  
  markAsRead: (id: string) =>
    getApiClient().put(`/notifications/${id}/read`),
};

export type { Appointment, Doctor, AvailableSlot, LabTest, LabTestType, MedicalRecord, Notification };
