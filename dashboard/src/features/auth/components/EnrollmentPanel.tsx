"use client";

import { QRCodeSVG } from "qrcode.react";
import { useState } from "react";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Button } from "@/shared/ui/Button";

type Props = {
  secret: string;
  provisioningUri: string;
};

const COPIED_MS = 2000;

const GROUPS_PER_LINE = 4;

function keyLines(secret: string): string[] {
  const groups = secret.match(/.{1,4}/g) ?? [secret];
  const lines: string[] = [];
  for (let start = 0; start < groups.length; start += GROUPS_PER_LINE) {
    lines.push(groups.slice(start, start + GROUPS_PER_LINE).join(" "));
  }
  return lines;
}

export function EnrollmentPanel({ secret, provisioningUri }: Props) {
  const { t } = useI18n();
  const [copied, setCopied] = useState(false);

  async function copySecret() {
    try {
      await navigator.clipboard.writeText(secret);
      setCopied(true);
      window.setTimeout(() => setCopied(false), COPIED_MS);
    } catch {
      setCopied(false);
    }
  }

  return (
    <div className="flex flex-col gap-4 rounded-card border border-border bg-surface p-4">
      <ol className="flex flex-col gap-1 text-sm text-muted">
        <li>
          <span className="font-semibold text-foreground">1.</span> {t.totp.stepScan}
        </li>
        <li>
          <span className="font-semibold text-foreground">2.</span> {t.totp.stepEnter}
        </li>
      </ol>
      <div className="flex justify-center">
        <div className="rounded-control border border-border bg-background p-3">
          <QRCodeSVG value={provisioningUri} size={164} level="M" role="img" aria-label={t.totp.qrLabel} />
        </div>
      </div>
      <div className="flex flex-col gap-1.5">
        <p className="text-xs text-subtle">{t.totp.manualKey}</p>
        <code className="rounded-control border border-border bg-background px-3 py-2 text-center font-mono text-sm leading-6 tracking-wider text-foreground">
          {keyLines(secret).map((line) => (
            <span key={line} className="block">
              {line}
            </span>
          ))}
        </code>
        <Button variant="secondary" onClick={copySecret} aria-live="polite" className="h-9">
          {copied ? t.totp.copied : t.totp.copy}
        </Button>
      </div>
    </div>
  );
}
