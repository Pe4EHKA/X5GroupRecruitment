// User roles from backend
export enum UserRole {
  ADMIN = 'ADMIN',
  RECRUITER = 'RECRUITER',
  HM = 'HM',
  CANDIDATE = 'CANDIDATE',
  STAGER = 'STAGER',
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
  INTERVIEW_SCHEDULED = 'INTERVIEW_SCHEDULED',
  INTERVIEW_COMPLETED = 'INTERVIEW_COMPLETED',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  OFFER_SENT = 'OFFER_SENT',
  OFFER_ACCEPTED = 'OFFER_ACCEPTED',
  OFFER_DECLINED = 'OFFER_DECLINED',
  WITHDRAWN = 'WITHDRAWN',
  ON_HOLD = 'ON_HOLD',
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

export interface PasswordResetResponse {
  traineeId: number;
  temporaryPassword: string;
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

// CandidateDto from backend API
export interface CandidateDto {
  id: number;
  fullName: string;
  email: string;
  phone?: string;
  university?: string;
  course?: string;
  statusToken?: string;
}

// ApplicationDto from backend API (list view)
// Use this interface when consuming data from backend API endpoints
export interface ApplicationDto {
  id: number;
  candidate?: CandidateDto;
  // @deprecated Use candidate.id instead. Backend maintains for compatibility.
  candidateId?: number;
  // @deprecated Use candidate.fullName instead. Backend maintains for compatibility.
  candidateName?: string;
  // @deprecated Use candidate.email instead. Backend maintains for compatibility.
  candidateEmail?: string;
  vacancyId: number;
  vacancyTitle: string;
  status: ApplicationStatus;
  coverLetter?: string;
  notes?: string;
  recruiterId?: number;
  recruiterName?: string;
  hmId?: number;
  hmName?: string;
  // @deprecated Use recruiterId/recruiterName instead. Backend maintains for compatibility.
  assignedRecruiterId?: number;
  // @deprecated Use recruiterId/recruiterName instead. Backend maintains for compatibility.
  assignedRecruiterName?: string;
  screeningScore?: number;
  createdAt: string;
  updatedAt: string;
  statusChangedAt: string;
  currentComment?: string;
}

// Application type (legacy interface)
// @deprecated Use ApplicationDto instead for new code. This interface exists for backward compatibility
// with existing frontend code that expects non-optional candidate field.
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

// ApplicationDetailDto from backend API (detail view)
export interface ApplicationDetailDto extends ApplicationDto {
  preferences?: ApplicationPreference[];
  statusHistory?: StatusHistory[];
  interviews?: Interview[];
  feedbacks?: Feedback[];
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
  uploadedById?: number;
  uploadedByName?: string;
  uploadedAt: string;
  totalRows: number;
  successRows: number;
  failedRows: number;
  usersCreated: number;
  usersLinked: number;
  completed: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ImportRowError {
  id: number;
  batchId: number;
  rowNumber: number;
  errorCode: string;
  errorMessage: string;
  rawSnapshot?: Record<string, unknown>;
  createdAt?: string;
}

export interface ImportResult {
  batch: ImportBatch;
  errors: ImportRowError[];
  totalErrors: number;
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
  newStatus: ApplicationStatus;
  comment?: string;
}

// Dashboard metrics
export interface DashboardMetrics {
  newCount: number;
  screeningCount: number;
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

// Stager profile type
export interface StagerProfileDto {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  city?: string;
  university?: string;
  course?: string;
  telegram?: string;
  birthYear?: number;
}
