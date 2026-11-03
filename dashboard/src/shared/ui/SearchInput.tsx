"use client";

import { useEffect, useId, useState } from "react";
import { Icon } from "./Icon";

type SearchInputProps = {
  label: string;
  placeholder: string;
  value: string;
  onSearch: (value: string) => void;
  delayMs?: number;
};

export function SearchInput({ label, placeholder, value, onSearch, delayMs = 350 }: SearchInputProps) {
  const id = useId();
  const [text, setText] = useState(value);

  useEffect(() => {
    if (text === value) {
      return;
    }
    const timer = window.setTimeout(() => onSearch(text.trim()), delayMs);
    return () => window.clearTimeout(timer);
  }, [text, value, onSearch, delayMs]);

  return (
    <div className="relative w-full sm:w-80">
      <label htmlFor={id} className="sr-only">
        {label}
      </label>
      <Icon name="search" className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-subtle" />
      <input
        id={id}
        type="search"
        value={text}
        placeholder={placeholder}
        onChange={(event) => setText(event.target.value)}
        className="h-10 w-full rounded-control border border-transparent bg-surface-strong pl-9 pr-3 text-sm text-foreground placeholder:text-subtle focus:bg-background"
      />
    </div>
  );
}
