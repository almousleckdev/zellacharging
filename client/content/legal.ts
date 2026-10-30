import type { Locale } from "@/lib/site";
import type { LegalDocument } from "./legal-types";
import { deleteAccountEn } from "./account-en";
import { deleteAccountFr } from "./account-fr";
import { privacyEn } from "./privacy-en";
import { privacyFr } from "./privacy-fr";
import { termsEn } from "./terms-en";
import { termsFr } from "./terms-fr";

export const privacyFor = (locale: Locale): LegalDocument => (locale === "fr" ? privacyFr : privacyEn);
export const termsFor = (locale: Locale): LegalDocument => (locale === "fr" ? termsFr : termsEn);
export const deleteAccountFor = (locale: Locale): LegalDocument =>
  locale === "fr" ? deleteAccountFr : deleteAccountEn;
