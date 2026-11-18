import { site } from "@/lib/site";

const IOS = /iPhone|iPad|iPod/i;
const ANDROID = /Android/i;

export function storeFor(userAgent: string): string | null {
  if (IOS.test(userAgent)) return site.appStoreUrl || null;
  if (ANDROID.test(userAgent)) return site.playStoreUrl || null;
  return null;
}
