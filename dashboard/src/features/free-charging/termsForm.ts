import { z } from "zod";

export const CONDITIONS_MAX = 1000;

export const termsFormSchema = z
  .object({
    customerId: z.string(),
    startsOn: z.string().min(1, "startRequired"),
    endsOn: z.string(),
    conditions: z.string().max(CONDITIONS_MAX, "conditionsTooLong"),
  })
  .refine((form) => form.endsOn === "" || form.endsOn >= form.startsOn, { path: ["endsOn"], message: "endBeforeStart" });

export const createFormSchema = termsFormSchema.refine((form) => form.customerId !== "", { path: ["customerId"], message: "customerRequired" });

export type TermsFormValues = z.input<typeof termsFormSchema>;
export type TermsField = keyof TermsFormValues;
export type TermsErrors = Partial<Record<TermsField, string>>;

export function termsErrorsOf(error: z.ZodError): TermsErrors {
  const errors: TermsErrors = {};
  for (const issue of error.issues) {
    const field = issue.path[0] as TermsField | undefined;
    if (field !== undefined && errors[field] === undefined) {
      errors[field] = issue.message;
    }
  }
  return errors;
}
