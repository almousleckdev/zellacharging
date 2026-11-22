import { z } from "zod";
import { WEEKDAYS } from "./schemas";

export const windowFormSchema = z
  .object({
    day: z.enum(WEEKDAYS),
    from: z.string().min(1, "fromRequired"),
    until: z.string(),
    endOfDay: z.boolean(),
  })
  .superRefine((values, context) => {
    if (values.endOfDay) return;
    if (!values.until) context.addIssue({ code: "custom", path: ["until"], message: "untilRequired" });
    else if (values.from && values.until <= values.from) context.addIssue({ code: "custom", path: ["until"], message: "untilBeforeFrom" });
  });

export type WindowFormValues = z.infer<typeof windowFormSchema>;
export type WindowErrors = Partial<Record<keyof WindowFormValues, string>>;

export function windowErrorsOf(error: z.ZodError): WindowErrors {
  const errors: WindowErrors = {};
  for (const issue of error.issues) {
    const field = issue.path[0] as keyof WindowFormValues;
    errors[field] ??= issue.message;
  }
  return errors;
}
