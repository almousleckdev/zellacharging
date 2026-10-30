import type { Copy } from "@/content/types";
import { site } from "@/lib/site";
import { Icon } from "./Icon";

type Props = { copy: Copy["stores"]; tone?: "dark" | "light" };

function StoreButton({
  href,
  icon,
  lead,
  name,
  soon,
  tone,
}: {
  href: string;
  icon: "apple" | "play";
  lead: string;
  name: string;
  soon: string;
  tone: "dark" | "light";
}) {
  const palette =
    tone === "dark" ? "bg-ink text-white hover:bg-slate-800" : "bg-white text-ink hover:bg-brand-soft";
  const body = (
    <>
      <Icon name={icon} className="h-7 w-7 shrink-0" />
      <span className="flex flex-col text-left leading-tight">
        <span className="text-[11px] opacity-80">{href ? lead : soon}</span>
        <span className="text-base font-semibold">{name}</span>
      </span>
    </>
  );
  const shared = `inline-flex h-12 min-w-44 items-center justify-center gap-3 rounded-xl px-5 transition-colors ${palette}`;
  return href ? (
    <a href={href} className={shared} rel="noopener">
      {body}
    </a>
  ) : (
    <span className={`${shared} cursor-default opacity-90`} aria-disabled="true">
      {body}
    </span>
  );
}

export function StoreButtons({ copy, tone = "dark" }: Props) {
  return (
    <div className="flex flex-wrap items-center gap-3">
      <StoreButton href={site.appStoreUrl} icon="apple" lead={copy.appStoreLead} name={copy.appStore} soon={copy.soon} tone={tone} />
      <StoreButton href={site.playStoreUrl} icon="play" lead={copy.playStoreLead} name={copy.playStore} soon={copy.soon} tone={tone} />
    </div>
  );
}
