-- Atualização para bancos criados antes do atendimento médico e do assistente de IA.
-- Banco novo não precisa disto: 01_cria_tabelas.sql e 02_dados_iniciais.sql já trazem tudo.
-- Esta pasta não é lida pelo container na subida (só os arquivos soltos em db/ são).
-- Aplicar com o banco no ar:
--   docker exec -i kairos-db-1 mysql -ukairos -pkairos kairos < db/atualizacoes/2026-11-15_atendimento_e_ia.sql
SET NAMES utf8mb4;

ALTER TABLE usuario ADD COLUMN fl_ativo BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE permissao (
    cd_usuario BIGINT      NOT NULL,
    nm_modulo  VARCHAR(30) NOT NULL,
    nm_acao    VARCHAR(20) NOT NULL,
    PRIMARY KEY (cd_usuario, nm_modulo, nm_acao),
    CONSTRAINT fk_permissao_usuario FOREIGN KEY (cd_usuario) REFERENCES usuario (cd_usuario)
);

CREATE TABLE resultado (
    cd_resultado   BIGINT       NOT NULL AUTO_INCREMENT,
    cd_compromisso BIGINT       NOT NULL,
    cd_medico      BIGINT       NOT NULL,
    cd_indicador   VARCHAR(20)  NULL,
    ds_achados     TEXT         NULL,
    ds_parecer     TEXT         NULL,
    cd_situacao    VARCHAR(20)  NOT NULL DEFAULT 'RASCUNHO',
    nr_versao      INT          NOT NULL DEFAULT 1,
    ds_assinatura  VARCHAR(300) NULL,
    dt_liberacao   DATETIME     NULL,
    dt_atualizacao TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (cd_resultado),
    CONSTRAINT uk_resultado_compromisso UNIQUE (cd_compromisso),
    CONSTRAINT fk_resultado_compromisso FOREIGN KEY (cd_compromisso) REFERENCES compromisso (cd_compromisso),
    CONSTRAINT fk_resultado_medico      FOREIGN KEY (cd_medico)      REFERENCES usuario (cd_usuario)
);

CREATE TABLE resultado_versao (
    cd_versao         BIGINT       NOT NULL AUTO_INCREMENT,
    cd_resultado      BIGINT       NOT NULL,
    nr_versao         INT          NOT NULL,
    cd_indicador      VARCHAR(20)  NOT NULL,
    ds_achados        TEXT         NULL,
    ds_parecer        TEXT         NOT NULL,
    ds_assinatura     VARCHAR(300) NOT NULL,
    dt_liberacao      DATETIME     NOT NULL,
    ds_motivo         VARCHAR(500) NOT NULL,
    ds_retificado_por VARCHAR(300) NOT NULL,
    dt_retificacao    DATETIME     NOT NULL,
    PRIMARY KEY (cd_versao),
    CONSTRAINT uk_resultado_versao UNIQUE (cd_resultado, nr_versao),
    CONSTRAINT fk_versao_resultado FOREIGN KEY (cd_resultado) REFERENCES resultado (cd_resultado)
);

CREATE TABLE analise_ia (
    cd_analise        BIGINT       NOT NULL AUTO_INCREMENT,
    cd_compromisso    BIGINT       NOT NULL,
    cd_medico         BIGINT       NOT NULL,
    ds_provedor       VARCHAR(150) NOT NULL,
    ds_contexto       MEDIUMTEXT   NOT NULL,
    ds_resposta       MEDIUMTEXT   NULL,
    fl_sucesso        BOOLEAN      NOT NULL,
    ds_erro           VARCHAR(500) NULL,
    nr_tokens_entrada INT          NULL,
    nr_tokens_saida   INT          NULL,
    nr_duracao_ms     INT          NOT NULL,
    dt_criacao        DATETIME     NOT NULL,
    PRIMARY KEY (cd_analise),
    INDEX ix_analise_medico_data (cd_medico, dt_criacao),
    CONSTRAINT fk_analise_compromisso FOREIGN KEY (cd_compromisso) REFERENCES compromisso (cd_compromisso),
    CONSTRAINT fk_analise_medico      FOREIGN KEY (cd_medico)      REFERENCES usuario (cd_usuario)
);

CREATE TABLE medico (
    cd_usuario       BIGINT       NOT NULL,
    nr_crm           VARCHAR(10)  NOT NULL,
    sg_uf_crm        CHAR(2)      NOT NULL,
    ds_especialidade VARCHAR(100) NULL,
    PRIMARY KEY (cd_usuario),
    CONSTRAINT uk_medico_crm UNIQUE (nr_crm, sg_uf_crm),
    CONSTRAINT fk_medico_usuario FOREIGN KEY (cd_usuario) REFERENCES usuario (cd_usuario)
);

-- Quem já existia continua entrando em tudo que não é só de médico (ninguém perde acesso)
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
) p;

-- Médica de desenvolvimento: login "medica", senha "kairos"
INSERT INTO usuario (ds_login, nm_usuario, ds_senha) VALUES
    ('medica', 'Dra. Helena Prado', 'pbkdf2$120000$e54Elobwi0UMSL/ujKMsQQ==$MZ0u+MMu6XvrN3p/uS5FRqomNJO9YNtD+oydXNlgaAs=');

INSERT INTO medico (cd_usuario, nr_crm, sg_uf_crm, ds_especialidade)
SELECT cd_usuario, '123456', 'SP', 'Medicina do Trabalho' FROM usuario WHERE ds_login = 'medica';

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
