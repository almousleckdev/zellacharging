"use client";

import { useEffect } from "react";
import type { RefObject } from "react";

export function useDismiss(open: boolean, root: RefObject<HTMLElement | null>, onDismiss: () => void) {
  useEffect(() => {
    if (!open) {
      return;
    }
    const close = (event: MouseEvent | KeyboardEvent) => {
      if (event instanceof KeyboardEvent ? event.key === "Escape" : !root.current?.contains(event.target as Node)) {
        onDismiss();
      }
    };
    document.addEventListener("mousedown", close);
    document.addEventListener("keydown", close);
    return () => {
      document.removeEventListener("mousedown", close);
      document.removeEventListener("keydown", close);
    };
  }, [open, root, onDismiss]);
}
