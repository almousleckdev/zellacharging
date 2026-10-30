import type { Locale } from "@/lib/site";
import { en } from "./en";
import { fr } from "./fr";
import type { Copy } from "./types";

const copies: Record<Locale, Copy> = { fr, en };

export const copyFor = (locale: Locale): Copy => copies[locale];
