"use client";

import { useState } from "react";
import { ChartFrame } from "./ChartFrame";
import type { ChartSeries } from "./ChartFrame";
import { CHART_HEIGHT, PAD, niceMax, tickIndexes, xScale, yScale } from "./geometry";
import { useChartWidth } from "./useChartWidth";

export type ChartPoint = { label: string; values: number[] };

type LineChartProps = {
  label: string;
  locale: string;
  series: ChartSeries[];
  points: ChartPoint[];
  format: (value: number) => string;
};

export function LineChart({ label, locale, series, points, format }: LineChartProps) {
  const [hover, setHover] = useState<number | null>(null);
  const { ref, width } = useChartWidth();
  const max = niceMax(Math.max(0, ...points.flatMap((point) => point.values)));
  const total = points.length;

  const move = (clientX: number, rect: DOMRect) => {
    const x = ((clientX - rect.left) / rect.width) * width;
    const step = total <= 1 ? 1 : (width - PAD.left - PAD.right) / (total - 1);
    setHover(Math.max(0, Math.min(total - 1, Math.round((x - PAD.left) / step))));
  };

  const active = hover === null ? null : points[hover];

  return (
    <ChartFrame
      label={label}
      width={width}
      containerRef={ref}
      max={max}
      locale={locale}
      series={series}
      onMove={move}
      onLeave={() => setHover(null)}
      tooltip={
        active && hover !== null
          ? {
              x: xScale(hover, total, width),
              title: active.label,
              rows: series.map((item, index) => ({ ...item, value: format(active.values[index]) })),
            }
          : null
      }
      xLabels={tickIndexes(total).map((index) => (
        <text key={index} x={xScale(index, total, width)} y={CHART_HEIGHT - 8} textAnchor="middle" fontSize="11" fill="var(--color-subtle)">
          {points[index].label}
        </text>
      ))}
    >
      {series.map((item, seriesIndex) => (
        <polyline
          key={item.name}
          fill="none"
          stroke={item.color}
          strokeWidth="2"
          strokeLinejoin="round"
          strokeLinecap="round"
          points={points.map((point, index) => `${xScale(index, total, width)},${yScale(point.values[seriesIndex], max)}`).join(" ")}
        />
      ))}
      {hover !== null && active && (
        <g>
          <line x1={xScale(hover, total, width)} x2={xScale(hover, total, width)} y1={PAD.top} y2={CHART_HEIGHT - PAD.bottom} stroke="var(--color-border-strong)" />
          {series.map((item, seriesIndex) => (
            <circle key={item.name} cx={xScale(hover, total, width)} cy={yScale(active.values[seriesIndex], max)} r="4" fill={item.color} stroke="var(--color-background)" strokeWidth="2" />
          ))}
        </g>
      )}
    </ChartFrame>
  );
}
