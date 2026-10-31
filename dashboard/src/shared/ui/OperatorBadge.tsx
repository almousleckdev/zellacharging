import Image from "next/image";

const OPERATORS: Record<string, { name: string; logo: string }> = {
  AIRTEL: { name: "Airtel Money", logo: "/operators/airtel.png" },
  MOOV: { name: "Moov Money", logo: "/operators/moov.png" },
};

type OperatorBadgeProps = { operator: string; showName?: boolean };

export function OperatorBadge({ operator, showName = false }: OperatorBadgeProps) {
  const known = OPERATORS[operator];
  if (!known) {
    return <span>{operator}</span>;
  }

  return (
    <span className="inline-flex items-center gap-1.5">
      <Image src={known.logo} alt={showName ? "" : known.name} title={known.name} width={20} height={20} className="size-5 rounded-md" />
      {showName && <span>{known.name}</span>}
    </span>
  );
}
