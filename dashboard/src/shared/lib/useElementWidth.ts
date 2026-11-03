"use client";

import { useCallback, useState } from "react";

export function useElementWidth<T extends HTMLElement>(): [(node: T | null) => void, number | null] {
  const [width, setWidth] = useState<number | null>(null);
  const ref = useCallback((node: T | null) => {
    if (!node || typeof ResizeObserver === "undefined") {
      return;
    }
    setWidth(node.getBoundingClientRect().width);
    const observer = new ResizeObserver(([entry]) => setWidth(entry.contentRect.width));
    observer.observe(node);
  }, []);

  return [ref, width];
}
