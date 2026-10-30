import type { Copy } from "@/content/types";
import { Icon } from "./Icon";

export function PhoneMockup({ copy }: { copy: Copy["phone"] }) {
  return (
    <div
      className="relative mx-auto w-[260px] rounded-[2.6rem] border-[10px] border-ink bg-white shadow-2xl shadow-orange-500/20 sm:w-[290px]"
      style={{ animation: "drift 7s ease-in-out infinite" }}
      role="img"
      aria-label="Zella Charging"
    >
      <div className="absolute left-1/2 top-2 h-5 w-24 -translate-x-1/2 rounded-full bg-ink" />
      <div className="space-y-4 px-4 pb-6 pt-12">
        <div className="rounded-2xl bg-brand p-4 text-white">
          <p className="text-xs opacity-90">{copy.balance}</p>
          <p className="mt-1 text-2xl font-bold">12 500 FCFA</p>
        </div>
        <div className="overflow-hidden rounded-2xl border border-line">
          <div className="flex items-center gap-2 bg-charge/10 px-4 py-3 text-sm font-semibold text-charge">
            <Icon name="bolt" className="h-4 w-4" />
            {copy.charging}
          </div>
          <div className="space-y-3 p-4">
            <div className="flex items-end justify-between">
              <span className="text-3xl font-bold">18,4</span>
              <span className="text-sm text-muted">{copy.kwh}</span>
            </div>
            <div className="h-2 overflow-hidden rounded-full bg-line">
              <div className="h-full rounded-full bg-charge" style={{ animation: "pulse-bar 4s ease-in-out infinite alternate" }} />
            </div>
            <p className="text-xs text-muted">{copy.station}</p>
          </div>
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div className="rounded-xl bg-brand-soft p-3 text-xs font-medium text-brand-deep">Airtel Money</div>
          <div className="rounded-xl bg-surface-muted p-3 text-xs font-medium text-ink">Moov Money</div>
        </div>
      </div>
    </div>
  );
}
