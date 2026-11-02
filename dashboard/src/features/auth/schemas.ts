import { z } from "zod";
import type { fr } from "@/shared/i18n/fr";

export type ValidationKey = keyof typeof fr.validation;

export const credentialsSchema = z.object({
  email: z.string().trim().min(1, "emailRequired").pipe(z.email("emailInvalid")),
  password: z.string().min(1, "passwordRequired"),
});

export const forgotPasswordSchema = z.object({
  email: credentialsSchema.shape.email,
});

export const totpCodeSchema = z.string().regex(/^\d{6}$/, "codeInvalid");

export const totpSubmissionSchema = z.object({
  challengeToken: z.string().min(1),
  code: totpCodeSchema,
});

export const loginStepSchema = z
  .object({
    stage: z.enum(["TOTP_REQUIRED", "TOTP_ENROLLMENT"]),
    challengeToken: z.string().min(1),
    secret: z.string().nullish(),
    provisioningUri: z.string().nullish(),
  })
  .transform((value) =>
    value.stage === "TOTP_ENROLLMENT" && value.secret && value.provisioningUri
      ? {
          stage: "TOTP_ENROLLMENT" as const,
          challengeToken: value.challengeToken,
          secret: value.secret,
          provisioningUri: value.provisioningUri,
        }
      : { stage: "TOTP_REQUIRED" as const, challengeToken: value.challengeToken },
  );

export type Credentials = z.infer<typeof credentialsSchema>;
export type LoginStep = z.output<typeof loginStepSchema>;
