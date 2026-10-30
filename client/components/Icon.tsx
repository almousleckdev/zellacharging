const paths = {
  map: "M12 21s-7-5.5-7-11a7 7 0 1 1 14 0c0 5.5-7 11-7 11Zm0-8.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z",
  scan: "M4 8V6a2 2 0 0 1 2-2h2M16 4h2a2 2 0 0 1 2 2v2M20 16v2a2 2 0 0 1-2 2h-2M8 20H6a2 2 0 0 1-2-2v-2M7 12h10",
  bolt: "M13 2 4 14h7l-1 8 9-12h-7l1-8Z",
  wallet: "M3 7a2 2 0 0 1 2-2h13v3M3 7v10a2 2 0 0 0 2 2h14a1 1 0 0 0 1-1V9a1 1 0 0 0-1-1H5a2 2 0 0 1-2-1Zm13 6h2",
  bell: "M6 9a6 6 0 1 1 12 0c0 5 2 6 2 6H4s2-1 2-6Zm4 9a2 2 0 0 0 4 0",
  shield: "M12 3 5 6v5c0 4.5 3 8.2 7 10 4-1.8 7-5.5 7-10V6l-7-3Zm-3 9 2 2 4-4",
  check: "m5 12 5 5 9-10",
  apple: "M16.4 12.6c0-2.2 1.8-3.2 1.9-3.3-1-1.5-2.6-1.7-3.2-1.7-1.4-.1-2.6.8-3.3.8s-1.7-.8-2.9-.8c-1.5 0-2.9.9-3.7 2.2-1.6 2.7-.4 6.800 1.100 9 .8 1.100 1.600 2.300 2.800 2.300 1.100 0 1.500-.7 2.900-.7s1.700.7 2.900.7 2-1.100 2.700-2.200c.9-1.300 1.200-2.500 1.200-2.600-.1 0-2.400-.9-2.400-3.700ZM14.200 6c.6-.7 1-1.700.9-2.700-.9 0-2 .6-2.600 1.300-.6.600-1.100 1.600-.9 2.600 1 .1 2-.5 2.600-1.200Z",
  play: "M5 3.500v17a1 1 0 0 0 1.500.9l14-8.500a1 1 0 0 0 0-1.800l-14-8.500A1 1 0 0 0 5 3.500Z",
  chevron: "m6 9 6 6 6-6",
} as const;

export type IconName = keyof typeof paths;

export function Icon({ name, className = "h-6 w-6" }: { name: IconName; className?: string }) {
  const filled = name === "apple" || name === "play" || name === "bolt";
  return (
    <svg
      viewBox="0 0 24 24"
      aria-hidden="true"
      className={className}
      fill={filled ? "currentColor" : "none"}
      stroke={filled ? "none" : "currentColor"}
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d={paths[name]} />
    </svg>
  );
}
