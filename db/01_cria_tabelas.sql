-- Estrutura do banco. Executado pelo MySQL na primeira subida do container.
SET NAMES utf8mb4;

-- RN-FU-02: código gerado pelo banco, nome obrigatório
CREATE TABLE funcionario (
    cd_funcionario BIGINT       NOT NULL AUTO_INCREMENT,
    nm_funcionario VARCHAR(255) NOT NULL,
    PRIMARY KEY (cd_funcionario)
);

-- RN-AG-02 e RN-AG-03: período obrigatório. 1 = Manhã, 2 = Tarde, 3 = Ambos
CREATE TABLE agenda (
    cd_agenda  BIGINT       NOT NULL AUTO_INCREMENT,
    nm_agenda  VARCHAR(255) NOT NULL,
    cd_periodo TINYINT      NOT NULL,
    PRIMARY KEY (cd_agenda),
    CONSTRAINT ck_agenda_periodo CHECK (cd_periodo IN (1, 2, 3))
);

-- RN-CO-02 e RN-CO-03: os quatro campos são obrigatórios e os vínculos precisam existir.
-- Sem ON DELETE CASCADE: a exclusão em cadeia (RN-FU-03) e o bloqueio (RN-AG-04)
-- são regras de negócio e ficam na camada Business. As chaves estrangeiras são a
-- última barreira: o banco recusa se o código deixar passar.
CREATE TABLE compromisso (
    cd_compromisso BIGINT NOT NULL AUTO_INCREMENT,
    cd_funcionario BIGINT NOT NULL,
    cd_agenda      BIGINT NOT NULL,
    dt_compromisso DATE   NOT NULL,
    hr_compromisso TIME   NOT NULL,
    PRIMARY KEY (cd_compromisso),
    CONSTRAINT fk_compromisso_funcionario FOREIGN KEY (cd_funcionario) REFERENCES funcionario (cd_funcionario),
    CONSTRAINT fk_compromisso_agenda      FOREIGN KEY (cd_agenda)      REFERENCES agenda (cd_agenda),
    -- Acelera o relatório por período (RN-RE-01)
    INDEX ix_compromisso_data (dt_compromisso)
);

-- RN-AC-03: ds_senha guarda o resultado do PBKDF2 com sal, nunca a senha.
-- O login é único; a comparação não diferencia maiúsculas de minúsculas (collation do banco).
CREATE TABLE usuario (
    cd_usuario BIGINT       NOT NULL AUTO_INCREMENT,
    ds_login   VARCHAR(60)  NOT NULL,
    nm_usuario VARCHAR(255) NOT NULL,
    ds_senha   VARCHAR(255) NOT NULL,
    PRIMARY KEY (cd_usuario),
    CONSTRAINT uk_usuario_login UNIQUE (ds_login)
);
