-- Dados de exemplo para a aplicação não abrir vazia.
SET NAMES utf8mb4;

INSERT INTO funcionario (nm_funcionario) VALUES
    ('João'), ('Maria'), ('José'), ('Joana');

INSERT INTO agenda (nm_agenda, cd_periodo) VALUES
    ('Exames clínicos', 1),
    ('Treinamentos',    2),
    ('Atendimento RH',  3);

INSERT INTO compromisso (cd_funcionario, cd_agenda, dt_compromisso, hr_compromisso) VALUES
    (1, 1, '2026-11-16', '08:30:00'),
    (2, 2, '2026-11-16', '14:00:00'),
    (3, 3, '2026-11-17', '10:00:00'),
    (4, 3, '2026-11-18', '15:30:00');
