import type { fr } from "./fr";

export type Locale = "fr" | "en";

type Widen<T> = T extends string
  ? string
  : T extends readonly (infer Item)[]
    ? readonly Widen<Item>[]
    : { -readonly [Key in keyof T]: Widen<T[Key]> };

export type Dictionary = Widen<typeof fr>;

export type ErrorCode = keyof typeof fr.errors;

export const DEFAULT_LOCALE: Locale = "fr";
