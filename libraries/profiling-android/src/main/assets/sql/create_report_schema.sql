PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS Report(
report_id TEXT NOT NULL,
reporting_type TEXT NOT NULL,
clazz TEXT NOT NULL,
json TEXT NOT NULL,
created_at INT,
PRIMARY KEY (report_id)
);

PRAGMA foreign_keys = OFF;
