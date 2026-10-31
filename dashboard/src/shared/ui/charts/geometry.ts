export const DEFAULT_WIDTH = 720;
export const CHART_HEIGHT = 260;
export const PAD = { top: 12, right: 12, bottom: 28, left: 56 };

export function niceMax(value: number): number {
  if (value <= 0) {
    return 1;
  }
  const exponent = Math.pow(10, Math.floor(Math.log10(value)));
  const fraction = value / exponent;
  const step = fraction <= 1 ? 1 : fraction <= 2 ? 2 : fraction <= 5 ? 5 : 10;
  return step * exponent;
}

export function yTicks(max: number, count = 4): number[] {
  return Array.from({ length: count + 1 }, (_, index) => (max / count) * index);
}

export function xScale(index: number, total: number, width: number): number {
  const inner = width - PAD.left - PAD.right;
  return PAD.left + (total <= 1 ? inner / 2 : (inner * index) / (total - 1));
}

export function yScale(value: number, max: number): number {
  const inner = CHART_HEIGHT - PAD.top - PAD.bottom;
  return PAD.top + inner - (value / max) * inner;
}

export function compact(value: number, locale: string): string {
  return new Intl.NumberFormat(locale, { notation: "compact", maximumFractionDigits: 1 }).format(value);
}

export function tickIndexes(total: number, wanted = 6): number[] {
  if (total <= wanted) {
    return Array.from({ length: total }, (_, index) => index);
  }
  const step = Math.ceil((total - 1) / (wanted - 1));
  const indexes: number[] = [];
  for (let index = 0; index < total; index += step) {
    indexes.push(index);
  }
  return indexes;
}

export function smoothPath(points: { x: number; y: number }[]): string {
  if (points.length === 0) {
    return "";
  }
  return points.reduce((path, point, index) => {
    if (index === 0) {
      return `M${point.x},${point.y}`;
    }
    const previous = points[index - 1];
    const middle = (previous.x + point.x) / 2;
    return `${path} C${middle},${previous.y} ${middle},${point.y} ${point.x},${point.y}`;
  }, "");
}
