import { z } from "zod";
import { PASSWORD_RULE } from "@/features/profile/schemas";

export const passwordSetupSchema = z
  .object({
    password: z.string().regex(PASSWORD_RULE, "passwordRules"),
    confirmPassword: z.string(),
  })
  .refine((values) => values.password === values.confirmPassword, {
    path: ["confirmPassword"],
    message: "passwordMismatch",
  });
