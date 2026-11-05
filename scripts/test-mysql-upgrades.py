#!/usr/bin/env python3
"""Verify additive upgrades on a disposable MySQL schema inside the dedicated test container."""
from pathlib import Path
import subprocess

container="ck-mtkpm-strategy-mysql"
def sql(text):
    result=subprocess.run(["docker","exec","-i","-e","MYSQL_PWD=ck-test-only",container,
        "mysql","-uroot","--batch","--skip-column-names"],input=text,text=True,capture_output=True,check=True)
    return result.stdout.strip()
schema="ck_mtkpm_upgrade_test"
sql(f"""DROP DATABASE IF EXISTS {schema}; CREATE DATABASE {schema}; USE {schema};
CREATE TABLE users(id BIGINT PRIMARY KEY,email VARCHAR(255));
CREATE TABLE patients(user_id BIGINT PRIMARY KEY);
CREATE TABLE appointments(id BIGINT PRIMARY KEY,price DECIMAL(12,2));
INSERT INTO users VALUES (1,'synthetic-upgrade@test.vn');
INSERT INTO patients VALUES (1);
INSERT INTO appointments VALUES (1,350000.00);
""")
root=Path(__file__).resolve().parents[1]
scripts=sorted((root/"backend/src/main/resources/db/upgrade").glob("*.sql"))
for script in scripts:
    sql("USE "+schema+";\n"+script.read_text())
    if script.name.startswith("001-"):
        sql("USE "+schema+"; INSERT INTO benefit_requests(patient_id,kind,reference_code,requested_from,requested_until,status) VALUES(1,'INSURANCE','OLD-PENDING','2026-01-01','2027-12-31','PENDING');")
old=sql("USE "+schema+"; SELECT CONCAT(price,'|',IFNULL(pricing_key,'NULL'),'|',IFNULL(CAST(base_price AS CHAR),'NULL'),'|',IFNULL(CAST(discount_rate AS CHAR),'NULL')) FROM appointments WHERE id=1;")
assert old=="350000.00|NULL|NULL|NULL",old
assert sql("USE "+schema+"; SELECT status FROM benefit_requests WHERE reference_code='OLD-PENDING';")=="PENDING"
assert sql("USE "+schema+"; SELECT COUNT(*) FROM benefit_decisions;")=="0"
print("PASS:",len(scripts),"MySQL additive upgrade scripts; historical fee and unknown snapshot metadata preserved; old pending request not auto-approved.")
