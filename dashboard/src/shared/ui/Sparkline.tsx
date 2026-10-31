type SparklineProps = { values: number[]; color: string; className?: string };

const WIDTH = 96;
const HEIGHT = 36;

export function Sparkline({ values, color, className }: SparklineProps) {
  const max = Math.max(...values, 1);
  const step = values.length > 1 ? WIDTH / (values.length - 1) : WIDTH;
  const points = values.map((value, index) => `${index * step},${HEIGHT - 3 - (value / max) * (HEIGHT - 6)}`).join(" ");

  return (
    <svg aria-hidden="true" viewBox={`0 0 ${WIDTH} ${HEIGHT}`} className={className}>
      <polyline fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" points={points} />
    </svg>
  );
}
