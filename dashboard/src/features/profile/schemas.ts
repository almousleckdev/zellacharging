import { z } from "zod";

export const PASSWORD_RULE = /^(?=.*\p{L})(?=.*\d).{10,128}$/u;

export const passwordChangeSchema = z
  .object({
    currentPassword: z.string().min(1, "currentPasswordRequired"),
    newPassword: z.string().regex(PASSWORD_RULE, "passwordRules"),
    confirmPassword: z.string(),
  })
  .refine((values) => values.newPassword === values.confirmPassword, {
    path: ["confirmPassword"],
    message: "passwordMismatch",
  });

export type PasswordChange = z.infer<typeof passwordChangeSchema>;

export const profileDetailsSchema = z.object({
  firstName: z.string().trim().min(1, "firstNameRequired").max(60, "firstNameRequired"),
  lastName: z.string().trim().min(1, "lastNameRequired").max(60, "lastNameRequired"),
  phoneDigits: z.string().regex(/^(\d{6,12})?$/, "phoneInvalid"),
});
