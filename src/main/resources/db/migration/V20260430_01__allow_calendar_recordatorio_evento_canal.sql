ALTER TABLE recordatorio_evento
DROP CONSTRAINT IF EXISTS recordatorio_evento_canal_check;

ALTER TABLE recordatorio_evento
ADD CONSTRAINT recordatorio_evento_canal_check
CHECK (canal IN ('app', 'email', 'sms', 'calendar'));