# E2E Checklist and Findings

Use this table to record the status of every critical scenario. Update the **Result** column with `PASS`/`FAIL` (or `BLOCKED`) and add short notes with evidence.

| Step | Scenario | Result | Notes |
| --- | --- | --- | --- |
| 1 | Stack boot (Docker) + `/actuator/health` + frontend assets | BLOCKED | Not executed in this run; follow runbook health commands. |
| 2 | Auth flow per role (admin/recruiter/stager) | BLOCKED | Pending manual login; ensure seed creds from runbook. |
| 3 | Excel import statistics & live UI refresh | BLOCKED | Pending; verify `/api/import-export/import` response counters and frontend list refresh. |
| 4 | Recruiter + stager cabinet navigation | BLOCKED | Pending; watch for null-safe rendering of candidate fields. |
| 5 | Application status change persists and re-renders | BLOCKED | Pending; confirm correct endpoint/role. |
| 6 | Hiring manager functionality removed | BLOCKED | Pending sweep of UI and API calls for HM actions. |
| 7 | Vacancy questions + video answers + transcript | BLOCKED | Pending end-to-end trial if in scope. |
| 8 | Frontend endpoint parity (fetch/axios map vs backend) | BLOCKED | Pending inventory of calls; ensure Next build stays green. |
| 9 | Critical UI/UX flows stable (loading/empty states) | BLOCKED | Pending exploratory testing. |

## Bugs / Risks Observed
- Maven tests could not resolve Spring Boot parent (`403` from Maven Central) in this environment. Run with access to `https://repo.maven.apache.org/maven2` or use a configured mirror before CI.
