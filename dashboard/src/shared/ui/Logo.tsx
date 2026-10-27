import Image from "next/image";

export function Logo({ height = 28 }: { height?: number }) {
  const width = Math.round((height * 2362) / 546);
  return <Image src="/logo/logo.png" alt="Zella" width={width} height={height} priority />;
}
