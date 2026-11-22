export function formatDay(day: string, locale: "fr" | "en"): string {
  const [year, month, date] = day.split("-");
  return locale === "fr" ? `${date}/${month}/${year}` : `${year}-${month}-${date}`;
}
