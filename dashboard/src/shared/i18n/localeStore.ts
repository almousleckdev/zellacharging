import { DEFAULT_LOCALE } from "./types";
import type { Locale } from "./types";

const STORAGE_KEY = "zella.locale";

const listeners = new Set<() => void>();
let remembered: Locale | null = null;

function isLocale(value: unknown): value is Locale {
  return value === "fr" || value === "en";
}

function readStored(): Locale | null {
  try {
    const stored = window.localStorage.getItem(STORAGE_KEY);
    return isLocale(stored) ? stored : null;
  } catch {
    return null;
  }
}

function writeStored(locale: Locale) {
  try {
    window.localStorage.setItem(STORAGE_KEY, locale);
  } catch {
    return;
  }
}

export function subscribeToLocale(listener: () => void): () => void {
  listeners.add(listener);
  window.addEventListener("storage", listener);
  return () => {
    listeners.delete(listener);
    window.removeEventListener("storage", listener);
  };
}

export function readLocale(): Locale {
  return remembered ?? readStored() ?? DEFAULT_LOCALE;
}

export function readServerLocale(): Locale {
  return DEFAULT_LOCALE;
}

export function saveLocale(locale: Locale) {
  remembered = locale;
  writeStored(locale);
  listeners.forEach((listener) => listener());
}
