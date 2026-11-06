import { readdirSync, statSync } from "node:fs";
import { join } from "node:path";
import { describe, expect, it } from "vitest";
import { NAV_GROUPS } from "@/features/shell/navigation";

const APP_DIR = join(process.cwd(), "src", "app");

function pagesUnder(directory: string, prefix = ""): string[] {
  return readdirSync(directory).flatMap((entry) => {
    const path = join(directory, entry);
    if (statSync(path).isDirectory()) {
      const isGroup = entry.startsWith("(") && entry.endsWith(")");
      return pagesUnder(path, isGroup ? prefix : `${prefix}/${entry}`);
    }
    return entry === "page.tsx" ? [prefix === "" ? "/" : prefix] : [];
  });
}

function matches(route: string, href: string): boolean {
  const routeParts = route.split("/");
  const hrefParts = href.split("/");
  return routeParts.length === hrefParts.length && routeParts.every((part, index) => part.startsWith("[") || part === hrefParts[index]);
}

const ROUTES = pagesUnder(APP_DIR);

describe("navigation links", () => {
  it("finds the pages of the console", () => {
    expect(ROUTES).toContain("/");
    expect(ROUTES).toContain("/login");
  });

  it.each(NAV_GROUPS.flatMap((group) => group.items))("the $id entry opens an existing page", (item) => {
    expect(ROUTES.some((route) => matches(route, item.href))).toBe(true);
  });

  it.each(["/customers/GA/1", "/sessions/GA/1", "/withdrawals/GA/1", "/stations/CP-1"])("the detail link %s opens an existing page", (href) => {
    expect(ROUTES.some((route) => matches(route, href))).toBe(true);
  });

  it("does not link to a page that does not exist", () => {
    expect(ROUTES.some((route) => matches(route, "/nowhere"))).toBe(false);
  });
});
