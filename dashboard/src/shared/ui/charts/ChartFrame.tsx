"use client";

import type { ReactNode } from "react";
import type { RefObject } from "react";
import { CHART_HEIGHT, PAD, compact, yScale, yTicks } from "./geometry";

export type ChartSeries = { name: string; color: string };

export type TooltipRow = { name: string; color: string; value: string };

type ChartFrameProps = {
  label: string;
  width: number;
  containerRef: RefObject<HTMLDivElement | null>;
  max: number;
  locale: string;
  series: ChartSeries[];
  tooltip: { x: number; title: string; rows: TooltipRow[] } | null;
  onMove: (clientX: number, rect: DOMRect) => void;
  onLeave: () => void;
  children: ReactNode;
  xLabels: ReactNode;
};

export function ChartFrame({ label, width, containerRef, max, locale, series, tooltip, onMove, onLeave, children, xLabels }: ChartFrameProps) {
  return (
    <figure className="flex flex-col gap-3">
      {series.length > 1 && (
        <figcaption className="flex flex-wrap gap-x-5 gap-y-1 text-xs text-muted">
          {series.map((item) => (
            <span key={item.name} className="flex items-center gap-1.5">
              <span aria-hidden="true" className="size-2.5 rounded-full" style={{ background: item.color }} />
              {item.name}
            </span>
          ))}
        </figcaption>
      )}
      <div ref={containerRef} className="relative">
        <svg
          role="img"
          aria-label={label}
          viewBox={`0 0 ${width} ${CHART_HEIGHT}`}
          className="h-auto w-full"
          onMouseMove={(event) => onMove(event.clientX, event.currentTarget.getBoundingClientRect())}
          onMouseLeave={onLeave}
        >
          {yTicks(max).map((tick) => (
            <g key={tick}>
              <line x1={PAD.left} x2={width - PAD.right} y1={yScale(tick, max)} y2={yScale(tick, max)} stroke="var(--color-grid)" />
              <text x={PAD.left - 8} y={yScale(tick, max) + 4} textAnchor="end" fontSize="11" fill="var(--color-subtle)">
                {compact(tick, locale)}
              </text>
            </g>
          ))}
          {children}
          {xLabels}
        </svg>
        {tooltip && (
          <div
            role="tooltip"
            className="pointer-events-none absolute top-2 z-10 min-w-40 -translate-x-1/2 rounded-control border border-border bg-background px-3 py-2 text-xs shadow-lg"
            style={{ left: `${Math.min(88, Math.max(12, (tooltip.x / width) * 100))}%` }}
          >
            <p className="mb-1 font-semibold text-foreground">{tooltip.title}</p>
            {tooltip.rows.map((row) => (
              <p key={row.name} className="flex items-center justify-between gap-4 text-muted">
                <span className="flex items-center gap-1.5">
                  <span aria-hidden="true" className="size-2 rounded-full" style={{ background: row.color }} />
                  {row.name}
                </span>
                <span className="figure font-medium text-foreground">{row.value}</span>
              </p>
            ))}
          </div>
        )}
      </div>
    </figure>
  );
}
