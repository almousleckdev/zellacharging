export const DIAL_CODES = [
  { country: "GA", dial: "+241", name: "Gabon" },
  { country: "ML", dial: "+223", name: "Mali" },
] as const;

export type SplitPhone = { dial: string; digits: string };

export function splitPhone(phone: string | null): SplitPhone {
  const match = DIAL_CODES.find((entry) => phone?.startsWith(entry.dial));
  if (!phone || !match) {
    return { dial: DIAL_CODES[0].dial, digits: "" };
  }
  return { dial: match.dial, digits: phone.slice(match.dial.length) };
}

export function joinPhone(dial: string, digits: string): string {
  return digits ? `${dial}${digits}` : "";
}
