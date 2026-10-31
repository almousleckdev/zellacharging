"use client";

import { useActionState } from "react";
import { sendContact, type ContactState } from "@/app/[lang]/contact/actions";
import type { ContactCopy } from "@/content/contact";

const field =
  "mt-2 block w-full rounded-xl border border-line bg-white px-4 py-3 text-base outline-none transition-colors focus:border-brand";

export function ContactForm({ lang, copy }: { lang: string; copy: ContactCopy["form"] }) {
  const [state, action, pending] = useActionState<ContactState, FormData>(sendContact, { status: "idle" });
  if (state.status === "sent") {
    return (
      <p role="status" className="rounded-2xl border border-line p-6 text-lg leading-relaxed">
        {copy.sent}
      </p>
    );
  }
  return (
    <form action={action} className="space-y-5 rounded-2xl border border-line p-6 sm:p-8">
      <h2 className="text-xl font-bold">{copy.title}</h2>
      <input type="hidden" name="lang" value={lang} />
      <input type="text" name="website" tabIndex={-1} autoComplete="off" aria-hidden="true" className="hidden" />
      <label className="block text-sm font-medium">
        {copy.name}
        <input name="name" required maxLength={120} autoComplete="name" className={field} />
      </label>
      <div className="grid gap-5 sm:grid-cols-2">
        <label className="block text-sm font-medium">
          {copy.email}
          <input name="email" type="email" required maxLength={200} autoComplete="email" className={field} />
        </label>
        <label className="block text-sm font-medium">
          {copy.phone}
          <input name="phone" type="tel" maxLength={40} autoComplete="tel" className={field} />
        </label>
      </div>
      <label className="block text-sm font-medium">
        {copy.topic}
        <select name="topic" required className={field} defaultValue={copy.topics[0]}>
          {copy.topics.map((topic) => (
            <option key={topic}>{topic}</option>
          ))}
        </select>
      </label>
      <label className="block text-sm font-medium">
        {copy.message}
        <textarea name="message" required minLength={10} maxLength={4000} rows={6} className={field} />
      </label>
      {(state.status === "invalid" || state.status === "failed") && (
        <p role="alert" className="text-sm font-medium text-red-600">
          {state.status === "invalid" ? copy.invalid : copy.failed}
        </p>
      )}
      <button
        type="submit"
        disabled={pending}
        className="inline-flex h-12 items-center justify-center rounded-xl bg-brand px-6 font-semibold text-white transition-colors hover:bg-brand-deep disabled:opacity-60"
      >
        {pending ? copy.sending : copy.send}
      </button>
    </form>
  );
}
