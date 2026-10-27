import { z } from "zod";

export const accessFormSchema = z.object({
  email: z.string().trim().min(1, "emailRequired").pipe(z.email("emailInvalid")),
  countries: z.array(z.string()).min(1, "countriesRequired"),
});
