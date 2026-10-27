"use client";

import { useRef } from "react";
import type { ChangeEvent } from "react";
import { initialsOf, ownAvatarUrl } from "@/features/session/avatarUrl";
import type { AdminProfile } from "@/features/session/schemas";
import { ApiError } from "@/shared/api/adminFetch";
import { errorMessage } from "@/shared/i18n/errorMessage";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Avatar } from "@/shared/ui/Avatar";
import { Button } from "@/shared/ui/Button";
import { useToast } from "@/shared/ui/ToastProvider";
import { AVATAR_TYPES, MAX_AVATAR_BYTES, useAvatar } from "../hooks/useAvatar";

export function AvatarUploader({ profile }: { profile: AdminProfile }) {
  const { t } = useI18n();
  const { notify } = useToast();
  const { upload, remove } = useAvatar();
  const input = useRef<HTMLInputElement>(null);
  const labels = t.profile.avatar;
  const busy = upload.isPending || remove.isPending;
  const fail = (error: unknown) => notify("error", errorMessage(t, error instanceof ApiError ? error.code : "UNKNOWN"));

  function handleFile(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) {
      return;
    }
    if (!AVATAR_TYPES.includes(file.type) || file.size > MAX_AVATAR_BYTES) {
      notify("error", errorMessage(t, "INVALID_AVATAR"));
      return;
    }
    upload.mutate(file, { onSuccess: () => notify("success", labels.updated), onError: fail });
  }

  return (
    <div className="flex items-center gap-4">
      <Avatar initials={initialsOf(profile.firstName, profile.lastName, profile.email)} src={ownAvatarUrl(profile.avatarVersion)} size={72} className="text-xl" />
      <div className="flex flex-col gap-2">
        <div className="flex flex-wrap gap-2">
          <Button variant="secondary" onClick={() => input.current?.click()} loading={upload.isPending} disabled={busy}>
            {labels.change}
          </Button>
          {profile.avatarVersion !== null && (
            <Button variant="ghost" onClick={() => remove.mutate(undefined, { onSuccess: () => notify("success", labels.removed), onError: fail })} loading={remove.isPending} disabled={busy}>
              {labels.remove}
            </Button>
          )}
        </div>
        <p className="text-xs text-subtle">{labels.hint}</p>
        <input ref={input} type="file" accept={AVATAR_TYPES.join(",")} onChange={handleFile} className="sr-only" tabIndex={-1} aria-hidden="true" aria-label={labels.change} />
      </div>
    </div>
  );
}
