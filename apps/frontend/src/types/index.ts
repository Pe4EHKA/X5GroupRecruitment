// User roles from backend
export enum UserRole {
  ADMIN = 'ADMIN',
  RECRUITER = 'RECRUITER',
  HM = 'HM',
  CANDIDATE = 'CANDIDATE',
}

// User status enum
export enum UserStatus {
  ACTIVE = 'ACTIVE',
  DISABLED = 'DISABLED',
  INVITED = 'INVITED',
}

// Application statuses from backend
export enum ApplicationStatus {
  NEW = 'NEW',
  SCREENING = 'SCREENING',
  PENDING_HM_REVIEW = 'PENDING_HM_REVIEW',
  HM_REVIEW = 'HM_REVIEW',
  INTERVIEW_SCHEDULED = 'INTERVIEW_SCHEDULED',
  INTERVIEW_COMPLETED = 'INTERVIEW_COMPLETED',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  OFFER_SENT = 'OFFER_SENT',
  OFFER_ACCEPTED = 'OFFER_ACCEPTED',
  OFFER_DECLINED = 'OFFER_DECLINED',
}

// User type
export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  roles: UserRole[];
}

// Extended user type for admin management
export interface AdminUser {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  phone?: string;
  department?: string;
  comment?: string;
  roles: UserRole[];
  status: UserStatus;
  lastLoginAt?: string;
  createdBy?: number;
  updatedBy?: number;
  createdAt: string;
  updatedAt: string;
}

// Create user request
export interface CreateUserRequest {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  department?: string;
  comment?: string;
  roles: UserRole[];
  status?: UserStatus;
  password?: string;
}

// Update user request
export interface UpdateUserRequest {
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  department?: string;
  comment?: string;
}

// Update roles request
export interface UpdateRolesRequest {
  roles: UserRole[];
}

// Update status request
export interface UpdateStatusRequest {
  status: UserStatus;
}

// Candidate type
export interface Candidate {
  id: number;
  email: string;
  fullName: string;
  phone: string;
  university?: string;
  course?: string;  // Changed from number to string to match backend
  statusToken: string;
  createdAt?: string;  // Made optional as it's not in CandidateDto
}

// Application type
export interface Application {
  id: number;
  candidate: Candidate;
  vacancyId: number;
  vacancyTitle: string;
  status: ApplicationStatus;
  recruiterId?: number;
  recruiterName?: string;
  hmId?: number;
  hmName?: string;
  createdAt: string;
  updatedAt: string;
  statusChangedAt: string;
  currentComment?: string;
}

// Application details with full info
export interface ApplicationDetail extends Application {
  preferences?: ApplicationPreference[];
  statusHistory?: StatusHistory[];
  interviews?: Interview[];
  feedbacks?: Feedback[];
}

export interface ApplicationPreference {
  preferenceOrder: number;
  preferredPosition: string;
  preferredLocation: string;
}

export interface StatusHistory {
  id: number;
  status: ApplicationStatus;
  comment?: string;
  changedBy: string;
  changedAt: string;
}

export interface Interview {
  id: number;
  interviewType: string;
  scheduledAt?: string;
  completedAt?: string;
  interviewerName?: string;
  notes?: string;
}

export interface Feedback {
  id: number;
  hmName: string;
  decision: 'APPROVE' | 'REJECT' | 'NEEDS_INFO';
  overallAssessment?: string;
  strengths?: string;
  areasForGrowth?: string;
  recommendations?: string;
  talentPool?: boolean;
  createdAt: string;
}

// Import batch
export interface ImportBatch {
  id: number;
  fileName: string;
  uploadedBy: string;
  uploadedAt: string;
  totalRows: number;
  successCount: number;
  errorCount: number;
  status: 'PROCESSING' | 'COMPLETED' | 'FAILED';
}

export interface ImportRowError {
  id: number;
  batchId: number;
  rowNumber: number;
  errorCode: string;
  errorMessage: string;
  rowData?: string;
}

export interface ImportResult {
  batch: ImportBatch;
  errors: ImportRowError[];
}

// Candidate status DTO
export interface CandidateStatus {
  applications: Array<{
    id: number;
    vacancyTitle: string;
    status: ApplicationStatus;
    statusChangedAt: string;
    currentComment?: string;
    nextStep?: string;
  }>;
}

// Request DTOs
export interface ChangeStatusRequest {
  status: ApplicationStatus;
  comment?: string;
}

export interface SendToHmRequest {
  hmId: number;
  comment?: string;
}

export interface HmDecisionRequest {
  decision: 'APPROVE' | 'REJECT' | 'NEEDS_INFO';
  overallAssessment?: string;
  strengths?: string;
  areasForGrowth?: string;
  recommendations?: string;
  talentPool?: boolean;
}

// Dashboard metrics
export interface DashboardMetrics {
  newCount: number;
  screeningCount: number;
  hmReviewCount: number;
  interviewCount: number;
  approvedCount: number;
  rejectedCount: number;
  slaBreachCount: number;
  totalCount: number;
}

// Filters
export interface ApplicationFilters {
  programId?: number;
  status?: ApplicationStatus;
  recruiterId?: number;
  hmId?: number;
  dateFrom?: string;
  dateTo?: string;
  slaBreached?: boolean;
  page?: number;
  size?: number;
  sort?: string;
}

// Paginated response
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
