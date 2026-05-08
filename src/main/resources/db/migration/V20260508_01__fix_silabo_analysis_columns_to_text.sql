alter table silabo_analysis_snapshot
  alter column temas_json type text using temas_json::text,
  alter column recursos_json type text using recursos_json::text;
