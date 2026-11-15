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

-- Usuários de desenvolvimento, os dois com a senha "kairos". Trocar fora do ambiente local.
-- Os textos abaixo foram gerados por br.com.kairos.infra.Senha.gerar("kairos").
--   admin  : administra o sistema (todos os módulos, menos os só de médico)
--   medica : médica do trabalho (atendimento, assistente de IA e leitura de resultados)
INSERT INTO usuario (ds_login, nm_usuario, ds_senha) VALUES
    ('admin',  'Administrador',  'pbkdf2$120000$9Pyo2vJY1bvltu3XhpS4Dg==$lW0EpuN5U6u33o8Nl4BxzTr62xo0YIX3nImHp5NHv5E='),
    ('medica', 'Dra. Helena Prado', 'pbkdf2$120000$e54Elobwi0UMSL/ujKMsQQ==$MZ0u+MMu6XvrN3p/uS5FRqomNJO9YNtD+oydXNlgaAs=');

INSERT INTO medico (cd_usuario, nr_crm, sg_uf_crm, ds_especialidade)
SELECT cd_usuario, '123456', 'SP', 'Medicina do Trabalho' FROM usuario WHERE ds_login = 'medica';

-- RN-PE-01: o administrador recebe todas as ações de todos os módulos que não são só de médico
INSERT INTO permissao (cd_usuario, nm_modulo, nm_acao)
SELECT u.cd_usuario, p.nm_modulo, p.nm_acao
FROM usuario u
JOIN (
    SELECT 'FUNCIONARIOS' AS nm_modulo, 'VER' AS nm_acao
    UNION ALL SELECT 'FUNCIONARIOS', 'INCLUIR'
    UNION ALL SELECT 'FUNCIONARIOS', 'ALTERAR'
    UNION ALL SELECT 'FUNCIONARIOS', 'EXCLUIR'
    UNION ALL SELECT 'AGENDAS', 'VER'
    UNION ALL SELECT 'AGENDAS', 'INCLUIR'
    UNION ALL SELECT 'AGENDAS', 'ALTERAR'
    UNION ALL SELECT 'AGENDAS', 'EXCLUIR'
    UNION ALL SELECT 'COMPROMISSOS', 'VER'
    UNION ALL SELECT 'COMPROMISSOS', 'INCLUIR'
    UNION ALL SELECT 'COMPROMISSOS', 'ALTERAR'
    UNION ALL SELECT 'COMPROMISSOS', 'EXCLUIR'
    UNION ALL SELECT 'RELATORIO', 'VER'
    UNION ALL SELECT 'RESULTADOS', 'VER'
    UNION ALL SELECT 'ACESSOS', 'VER'
    UNION ALL SELECT 'ACESSOS', 'INCLUIR'
    UNION ALL SELECT 'ACESSOS', 'ALTERAR'
) p
WHERE u.ds_login = 'admin';

-- A médica vê os compromissos (fila), atende e usa a IA
INSERT INTO permissao (cd_usuario, nm_modulo, nm_acao)
SELECT u.cd_usuario, p.nm_modulo, p.nm_acao
FROM usuario u
JOIN (
    SELECT 'COMPROMISSOS' AS nm_modulo, 'VER' AS nm_acao
    UNION ALL SELECT 'RESULTADOS', 'VER'
    UNION ALL SELECT 'ATENDIMENTO', 'VER'
    UNION ALL SELECT 'ATENDIMENTO', 'INCLUIR'
    UNION ALL SELECT 'ATENDIMENTO', 'ALTERAR'
    UNION ALL SELECT 'ASSISTENTE_IA', 'VER'
) p
WHERE u.ds_login = 'medica';
