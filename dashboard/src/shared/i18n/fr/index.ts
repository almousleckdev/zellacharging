import { frCommon } from "./common";
import { frAuth } from "./auth";
import { frShell } from "./shell";
import { frOverview } from "./overview";
import { frProfile } from "./profile";
import { frCustomers } from "./customers";
import { frUsers } from "./users";
import { frOnboarding } from "./onboarding";
import { frPayments } from "./payments";
import { frSessions } from "./sessions";
import { frCommissions } from "./commissions";
import { frIncidents } from "./incidents";
import { frAudit } from "./audit";
import { frPlatform } from "./platform";
import { frStations } from "./stations";
import { frWithdrawals } from "./withdrawals";
import { frStaff } from "./staff";

export const fr = {
  ...frCommon,
  ...frAuth,
  ...frShell,
  ...frOverview,
  ...frProfile,
  ...frCustomers,
  ...frUsers,
  ...frOnboarding,
  ...frPayments,
  ...frSessions,
  ...frCommissions,
  ...frIncidents,
  ...frAudit,
  ...frPlatform,
  ...frStations,
  ...frWithdrawals,
  ...frStaff,
} as const;
