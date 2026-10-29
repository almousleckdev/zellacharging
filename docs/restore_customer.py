import re
import subprocess
import sys

TABLES = ["topup", "funding_hold", "charging_session", "ledger_entry", "ledger_hidden"]


def copy_blocks(restore, dump, table):
    command = restore.split() + ["-a", "-t", table, "-f", "-", dump]
    output = subprocess.run(command, capture_output=True, text=True, check=True).stdout.splitlines()
    blocks = []
    header = None
    rows = []
    for line in output:
        if line.startswith("COPY "):
            header, rows = line, []
        elif header and line == "\\.":
            blocks.append((header, rows))
            header = None
        elif header:
            rows.append(line)
    return blocks


def main():
    restore, dump, customer = sys.argv[1], sys.argv[2], sys.argv[3]
    if not re.fullmatch(r"[0-9a-f-]{36}", customer):
        raise SystemExit("customer must be a uuid")
    print("begin;")
    wallet = copy_blocks(restore, dump, "wallet")
    for header, rows in wallet:
        columns = [name.strip() for name in re.search(r"\((.*)\)", header).group(1).split(",")]
        for row in rows:
            cells = row.split("\t")
            if cells[columns.index("customer_id")] == customer:
                print("update wallet set balance_minor = %s, reserved_minor = %s where customer_id = '%s';"
                      % (cells[columns.index("balance_minor")], cells[columns.index("reserved_minor")], customer))
    for table in TABLES:
        for header, rows in copy_blocks(restore, dump, table):
            mine = [row for row in rows if customer in row]
            if mine:
                print(header)
                print("\n".join(mine))
                print("\\.")
    print("select count(*) as ledger_rows from ledger_entry where customer_id = '%s';" % customer)
    print("select balance_minor, reserved_minor from wallet where customer_id = '%s';" % customer)
    print("rollback;")


main()
