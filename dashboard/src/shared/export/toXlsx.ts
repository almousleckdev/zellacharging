import { downloadBlob } from "./download";
import type { ExportSheet } from "./types";

const XLSX_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
const MIN_WIDTH = 10;
const MAX_WIDTH = 42;

export async function exportXlsx<Row>(sheet: ExportSheet<Row>): Promise<void> {
  const { Workbook } = await import("exceljs");
  const workbook = new Workbook();
  const worksheet = workbook.addWorksheet(sheet.title.slice(0, 31));
  worksheet.columns = sheet.columns.map((column) => ({ header: column.header, width: MIN_WIDTH }));
  sheet.rows.forEach((row) => worksheet.addRow(sheet.columns.map((column) => column.value(row))));
  const header = worksheet.getRow(1);
  header.font = { bold: true };
  header.alignment = { vertical: "middle" };
  worksheet.views = [{ state: "frozen", ySplit: 1 }];
  worksheet.autoFilter = { from: { row: 1, column: 1 }, to: { row: 1, column: sheet.columns.length } };
  sheet.columns.forEach((column, index) => {
    const longest = Math.max(column.header.length, ...sheet.rows.map((row) => String(column.value(row)).length));
    const target = worksheet.getColumn(index + 1);
    target.width = Math.min(MAX_WIDTH, Math.max(MIN_WIDTH, longest + 2));
    if (column.numeric) {
      target.alignment = { horizontal: "right" };
    }
  });
  const buffer = await workbook.xlsx.writeBuffer();
  downloadBlob(new Blob([buffer], { type: XLSX_TYPE }), `${sheet.fileName}.xlsx`);
}
