/**
 * Utility functions for safe data handling and formatting
 */

import { Candidate } from '@/types';

/**
 * Get full name from candidate object with fallback
 */
export function getCandidateFullName(candidate?: Candidate | null): string {
  if (!candidate) return 'Без имени';
  return candidate.fullName || 'Без имени';
}

/**
 * Get candidate email with fallback
 */
export function getCandidateEmail(candidate?: Candidate | null): string {
  if (!candidate) return '—';
  return candidate.email || '—';
}

/**
 * Get candidate phone with fallback
 */
export function getCandidatePhone(candidate?: Candidate | null): string {
  if (!candidate) return '—';
  return candidate.phone || '—';
}

/**
 * Get candidate university with fallback
 */
export function getCandidateUniversity(candidate?: Candidate | null): string {
  if (!candidate) return '—';
  return candidate.university || '—';
}

/**
 * Get candidate course with fallback
 */
export function getCandidateCourse(candidate?: Candidate | null): string {
  if (!candidate) return '—';
  return candidate.course || '—';
}

/**
 * Build full name from first and last name parts (if needed)
 */
export function buildFullName(firstName?: string, lastName?: string): string {
  const first = firstName?.trim() || '';
  const last = lastName?.trim() || '';
  
  if (first && last) return `${first} ${last}`;
  if (first) return first;
  if (last) return last;
  
  return 'Без имени';
}
