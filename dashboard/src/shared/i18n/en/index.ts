import type { Dictionary } from "../types";
import { enCommon } from "./common";
import { enAuth } from "./auth";
import { enShell } from "./shell";
import { enOverview } from "./overview";
import { enProfile } from "./profile";
import { enCustomers } from "./customers";
import { enUsers } from "./users";
import { enOnboarding } from "./onboarding";
import { enPayments } from "./payments";
import { enSessions } from "./sessions";
import { enCommissions } from "./commissions";
import { enIncidents } from "./incidents";
import { enAudit } from "./audit";
import { enPlatform } from "./platform";
import { enStations } from "./stations";

export const en: Dictionary = {
  ...enCommon,
  ...enAuth,
  ...enShell,
  ...enOverview,
  ...enProfile,
  ...enCustomers,
  ...enUsers,
  ...enOnboarding,
  ...enPayments,
  ...enSessions,
  ...enCommissions,
  ...enIncidents,
  ...enAudit,
  ...enPlatform,
  ...enStations,
};
