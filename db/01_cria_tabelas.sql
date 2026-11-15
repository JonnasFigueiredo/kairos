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
-- RN-PE-06: conta não se exclui, só se desativa (fl_ativo).
CREATE TABLE usuario (
    cd_usuario BIGINT       NOT NULL AUTO_INCREMENT,
    ds_login   VARCHAR(60)  NOT NULL,
    nm_usuario VARCHAR(255) NOT NULL,
    ds_senha   VARCHAR(255) NOT NULL,
    fl_ativo   BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (cd_usuario),
    CONSTRAINT uk_usuario_login UNIQUE (ds_login)
);

-- RN-PE-01: cada linha libera uma ação de um módulo para um usuário.
-- Os módulos e as ações que cada um aceita estão no enum Modulo (o código é a fonte da verdade).
CREATE TABLE permissao (
    cd_usuario BIGINT      NOT NULL,
    nm_modulo  VARCHAR(30) NOT NULL,
    nm_acao    VARCHAR(20) NOT NULL,
    PRIMARY KEY (cd_usuario, nm_modulo, nm_acao),
    CONSTRAINT fk_permissao_usuario FOREIGN KEY (cd_usuario) REFERENCES usuario (cd_usuario)
);

-- RN-AT-01: resultado do atendimento, um por compromisso, lançado pelo médico.
-- * cd_indicador com os valores do eSocial S-2220: NORMAL, ALTERADO, ESTAVEL, AGRAVAMENTO
-- * RASCUNHO pode ficar incompleto; LIBERADO é assinado e não se edita, só se retifica (RN-AT-03)
-- * ds_assinatura guarda nome e CRM do médico no momento da liberação (RN-ME-05)
-- * Sem ON DELETE CASCADE: excluir o compromisso ou o funcionário apaga o resultado na Business (RN-AT-05)
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

-- RN-AT-03: cada retificação guarda aqui a versão substituída, com o motivo e quem retificou.
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

-- RN-IA-05: auditoria do assistente de IA. Cada análise pedida fica registrada: quem pediu, para qual
-- compromisso, em qual provedor, o contexto que saiu do sistema (já pseudonimizado) e a resposta.
-- Serve para prestar contas (LGPD) e para o limite diário por médico (RN-IA-04).
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

-- RN-ME-01: dados profissionais de um usuário. Ter esta linha é o que faz da conta um médico.
-- RN-ME-02: um CRM por UF.
CREATE TABLE medico (
    cd_usuario       BIGINT       NOT NULL,
    nr_crm           VARCHAR(10)  NOT NULL,
    sg_uf_crm        CHAR(2)      NOT NULL,
    ds_especialidade VARCHAR(100) NULL,
    PRIMARY KEY (cd_usuario),
    CONSTRAINT uk_medico_crm UNIQUE (nr_crm, sg_uf_crm),
    CONSTRAINT fk_medico_usuario FOREIGN KEY (cd_usuario) REFERENCES usuario (cd_usuario)
);
