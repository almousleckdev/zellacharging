import { frCommon } from "./common";
import { frAuth } from "./auth";
import { frShell } from "./shell";
import { frOverview } from "./overview";
import { frProfile } from "./profile";
import { frCustomers } from "./customers";
import { frUsers } from "./users";
import { frOnboarding } from "./onboarding";

export const fr = {
  ...frCommon,
  ...frAuth,
  ...frShell,
  ...frOverview,
  ...frProfile,
  ...frCustomers,
  ...frUsers,
  ...frOnboarding,
} as const;
