"use server";

import { isLocale } from "@/lib/site";

export type ContactState = { status: "idle" | "sent" | "invalid" | "failed" };

const apiBase = process.env.API_BASE_URL ?? "https://apicharging.up.railway.app";
const text = (data: FormData, key: string) => String(data.get(key) ?? "").trim();

export async function sendContact(_: ContactState, data: FormData): Promise<ContactState> {
  if (text(data, "website") !== "") return { status: "sent" };
  const lang = text(data, "lang");
  const body = {
    name: text(data, "name"),
    email: text(data, "email"),
    phone: text(data, "phone"),
    topic: text(data, "topic"),
    message: text(data, "message"),
  };
  const valid =
    isLocale(lang) &&
    body.name.length > 0 &&
    /^\S+@\S+\.\S+$/.test(body.email) &&
    body.topic.length > 0 &&
    body.message.length >= 10;
  if (!valid) return { status: "invalid" };
  try {
    const response = await fetch(`${apiBase}/v1/contact`, {
      method: "POST",
      headers: { "Content-Type": "application/json", "Accept-Language": lang },
      body: JSON.stringify(body),
    });
    return { status: response.ok ? "sent" : "failed" };
  } catch {
    return { status: "failed" };
  }
}
