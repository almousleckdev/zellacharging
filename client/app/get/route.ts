import { NextResponse, type NextRequest } from "next/server";
import { storeFor } from "@/lib/store";

export function GET(request: NextRequest) {
  const store = storeFor(request.headers.get("user-agent") ?? "");
  return NextResponse.redirect(store ?? new URL("/", request.url), 307);
}
