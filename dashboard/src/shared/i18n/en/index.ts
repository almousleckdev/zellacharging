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
};
