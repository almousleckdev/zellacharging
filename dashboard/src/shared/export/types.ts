export type ExportColumn<Row> = {
  header: string;
  value: (row: Row) => string | number;
  numeric?: boolean;
};

export type ExportSheet<Row> = {
  title: string;
  subtitle: string;
  fileName: string;
  columns: ExportColumn<Row>[];
  rows: Row[];
};
