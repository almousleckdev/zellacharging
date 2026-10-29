import { downloadBlob } from "./download";
import type { ExportSheet } from "./types";

const BRAND: [number, number, number] = [249, 115, 22];
const INK: [number, number, number] = [23, 23, 23];
const MUTED: [number, number, number] = [115, 115, 115];
const MARGIN = 36;

export async function exportPdf<Row>(sheet: ExportSheet<Row>): Promise<void> {
  const [{ jsPDF }, { default: autoTable }] = await Promise.all([import("jspdf"), import("jspdf-autotable")]);
  const document = new jsPDF({ orientation: "landscape", unit: "pt", format: "a4" });
  document.setFont("helvetica", "bold");
  document.setFontSize(16);
  document.setTextColor(...INK);
  document.text(sheet.title, MARGIN, MARGIN + 6);
  document.setFont("helvetica", "normal");
  document.setFontSize(9);
  document.setTextColor(...MUTED);
  document.text(sheet.subtitle, MARGIN, MARGIN + 22);

  autoTable(document, {
    startY: MARGIN + 36,
    margin: { left: MARGIN, right: MARGIN, bottom: MARGIN },
    head: [sheet.columns.map((column) => column.header)],
    body: sheet.rows.map((row) => sheet.columns.map((column) => String(column.value(row)))),
    styles: { fontSize: 8, cellPadding: 4, textColor: INK },
    headStyles: { fillColor: BRAND, textColor: INK, fontStyle: "bold" },
    alternateRowStyles: { fillColor: [250, 250, 250] },
    columnStyles: Object.fromEntries(
      sheet.columns.flatMap((column, index) => (column.numeric ? [[index, { halign: "right" as const }]] : [])),
    ),
  });
  const pages = document.getNumberOfPages();
  const { width, height } = document.internal.pageSize;
  document.setFontSize(8);
  document.setTextColor(...MUTED);
  for (let page = 1; page <= pages; page += 1) {
    document.setPage(page);
    document.text(`${page} / ${pages}`, width - MARGIN, height - 16, { align: "right" });
  }
  downloadBlob(document.output("blob"), `${sheet.fileName}.pdf`);
}
