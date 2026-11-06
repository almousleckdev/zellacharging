import {
  ArrowDown,
  ArrowLeft,
  ArrowUp,
  BatteryCharging,
  Bell,
  ChartColumn,
  Check,
  ChevronDown,
  ChevronRight,
  CircleAlert,
  ClipboardList,
  Ellipsis,
  Globe,
  LayoutDashboard,
  LogOut,
  Menu,
  Percent,
  Search,
  ShieldCheck,
  SlidersHorizontal,
  Tag,
  User,
  Users,
  Wallet,
  X,
  Zap,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { cn } from "@/shared/lib/cn";

const ICONS = {
  overview: LayoutDashboard,
  globe: Globe,
  station: BatteryCharging,
  bolt: Zap,
  users: Users,
  wallet: Wallet,
  tag: Tag,
  percent: Percent,
  shield: ShieldCheck,
  chart: ChartColumn,
  bell: Bell,
  clipboard: ClipboardList,
  sliders: SlidersHorizontal,
  logout: LogOut,
  menu: Menu,
  close: X,
  chevronDown: ChevronDown,
  chevronRight: ChevronRight,
  check: Check,
  arrowDown: ArrowDown,
  arrowUp: ArrowUp,
  user: User,
  search: Search,
  arrowLeft: ArrowLeft,
  more: Ellipsis,
  alert: CircleAlert,
} satisfies Record<string, LucideIcon>;

export type IconName = keyof typeof ICONS;

export function Icon({ name, className }: { name: IconName; className?: string }) {
  const Glyph = ICONS[name];
  return <Glyph aria-hidden="true" className={cn("size-5 shrink-0", className)} />;
}
