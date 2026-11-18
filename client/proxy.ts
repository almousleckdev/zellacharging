import { NextResponse, type NextRequest } from "next/server";
import { isLocale, locales } from "@/lib/site";

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const first = pathname.split("/")[1];
  if (isLocale(first)) return;
  const wantsEnglish = (request.headers.get("accept-language") ?? "").toLowerCase().startsWith("en");
  const locale = wantsEnglish ? "en" : locales[0];
  request.nextUrl.pathname = `/${locale}${pathname === "/" ? "" : pathname}`;
  return NextResponse.redirect(request.nextUrl);
}

export const config = {
  matcher: ["/((?!_next|api|get|.*\\..*).*)"],
};
