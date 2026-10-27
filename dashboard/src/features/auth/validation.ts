import type { z } from "zod";
import type { ValidationKey } from "./schemas";

export type FieldErrors<Field extends string> = Partial<Record<Field, ValidationKey>>;

export function fieldErrorsOf<Field extends string>(error: z.ZodError): FieldErrors<Field> {
  const errors: FieldErrors<Field> = {};
  for (const issue of error.issues) {
    const field = issue.path[0] as Field | undefined;
    if (field !== undefined && errors[field] === undefined) {
      errors[field] = issue.message as ValidationKey;
    }
  }
  return errors;
}
