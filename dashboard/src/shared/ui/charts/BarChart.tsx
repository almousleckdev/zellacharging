"use client";

import { useState } from "react";
import { ChartFrame } from "./ChartFrame";
import type { ChartSeries } from "./ChartFrame";
import type { ChartPoint } from "./LineChart";
import { CHART_HEIGHT, PAD, niceMax, tickIndexes, yScale } from "./geometry";
import { useChartWidth } from "./useChartWidth";

type BarChartProps = {
  label: string;
  locale: string;
  series: ChartSeries;
  points: ChartPoint[];
  format: (value: number) => string;
};

export function BarChart({ label, locale, series, points, format }: BarChartProps) {
  const [hover, setHover] = useState<number | null>(null);
  const { ref, width } = useChartWidth();
  const max = niceMax(Math.max(0, ...points.map((point) => point.values[0])));
  const total = points.length;
  const slot = (width - PAD.left - PAD.right) / Math.max(total, 1);
  const barWidth = Math.max(2, Math.min(28, slot * 0.6));
  const baseline = CHART_HEIGHT - PAD.bottom;

  const move = (clientX: number, rect: DOMRect) => {
    const x = ((clientX - rect.left) / rect.width) * width;
    setHover(Math.max(0, Math.min(total - 1, Math.floor((x - PAD.left) / slot))));
  };

  return (
    <ChartFrame
      label={label}
      width={width}
      containerRef={ref}
      max={max}
      locale={locale}
      series={[series]}
      onMove={move}
      onLeave={() => setHover(null)}
      tooltip={
        hover === null
          ? null
          : {
              x: PAD.left + slot * hover + slot / 2,
              title: points[hover].label,
              rows: [{ ...series, value: format(points[hover].values[0]) }],
            }
      }
      xLabels={tickIndexes(total).map((index) => (
        <text key={index} x={PAD.left + slot * index + slot / 2} y={CHART_HEIGHT - 8} textAnchor="middle" fontSize="11" fill="var(--color-subtle)">
          {points[index].label}
        </text>
      ))}
    >
      {points.map((point, index) => {
        const top = yScale(point.values[0], max);
        const x = PAD.left + slot * index + (slot - barWidth) / 2;
        return (
          <rect
            key={point.label + index}
            x={x}
            y={top}
            width={barWidth}
            height={Math.max(0, baseline - top)}
            rx="3"
            fill={series.color}
            opacity={hover === null || hover === index ? 1 : 0.55}
          />
        );
      })}
    </ChartFrame>
  );
}
