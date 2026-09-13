--liquibase formatted sql

--changeset fawry:001-create-users
CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    email       VARCHAR(180) NOT NULL UNIQUE,
    password    VARCHAR(120) NOT NULL,
    full_name   VARCHAR(150) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'USER'))
);
--rollback DROP TABLE users;

--changeset fawry:002-create-gateways
CREATE TABLE gateways (
    id                      SERIAL         PRIMARY KEY,
    name                    VARCHAR(120)  NOT NULL UNIQUE,
    fixed_commission        NUMERIC(19, 2) NOT NULL,
    percentage_commission   NUMERIC(5, 2)  NOT NULL,
    min_transaction_amount  NUMERIC(19, 2) NOT NULL,
    max_transaction_amount  NUMERIC(19, 2),
    daily_limit_per_biller  NUMERIC(19, 2) NOT NULL,
    available_from          TIME           NOT NULL,
    available_to            TIME           NOT NULL,
    available_day_from      VARCHAR(10)    NOT NULL,
    available_day_to        VARCHAR(10)    NOT NULL,
    processing_time_hours   INTEGER        NOT NULL,
    active                  BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_gateways_fixed      CHECK (fixed_commission >= 0),
    CONSTRAINT chk_gateways_percentage CHECK (percentage_commission >= 0),
    CONSTRAINT chk_gateways_min        CHECK (min_transaction_amount > 0),
    CONSTRAINT chk_gateways_bounds     CHECK (max_transaction_amount >= min_transaction_amount),
    CONSTRAINT chk_gateways_limit      CHECK (daily_limit_per_biller > 0),
    CONSTRAINT chk_gateways_day_from   CHECK (available_day_from IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    CONSTRAINT chk_gateways_day_to     CHECK (available_day_to   IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    CONSTRAINT chk_gateways_speed      CHECK (processing_time_hours >= 0)
);
CREATE INDEX idx_gateways_active ON gateways (active);
--rollback DROP TABLE gateways;

--changeset fawry:003-create-transactions
CREATE TABLE transactions (
    id            SERIAL         PRIMARY KEY,
    biller_id     INTEGER        NOT NULL REFERENCES users (id),
    gateway_id    INTEGER        NOT NULL REFERENCES gateways (id),
    amount        NUMERIC(19, 2) NOT NULL,
    commission    NUMERIC(19, 2) NOT NULL,
    urgency       VARCHAR(20)    NOT NULL,
    business_date DATE           NOT NULL,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_transactions_urgency    CHECK (urgency IN ('INSTANT', 'CAN_WAIT')),
    CONSTRAINT chk_transactions_amount     CHECK (amount > 0),
    CONSTRAINT chk_transactions_commission CHECK (commission >= 0)
);
CREATE INDEX idx_transactions_biller_date ON transactions (biller_id, business_date);
CREATE INDEX idx_transactions_gateway     ON transactions (gateway_id);
--rollback DROP TABLE transactions;

--changeset fawry:005-create-biller-quota-usage
CREATE TABLE biller_quota_usage (
    id          SERIAL         PRIMARY KEY,
    biller_id   INTEGER        NOT NULL REFERENCES users (id),
    gateway_id  INTEGER        NOT NULL REFERENCES gateways (id),
    usage_date  DATE           NOT NULL,
    used_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    CONSTRAINT uq_biller_quota UNIQUE (biller_id, gateway_id, usage_date),
    CONSTRAINT chk_quota_used CHECK (used_amount >= 0)
);
--rollback DROP TABLE biller_quota_usage;

--changeset fawry:010-updated-at-function splitStatements:false
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
--rollback DROP FUNCTION IF EXISTS set_updated_at();

--changeset fawry:011-updated-at-trigger
CREATE TRIGGER trg_gateways_updated_at
    BEFORE UPDATE ON gateways
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
--rollback DROP TRIGGER IF EXISTS trg_gateways_updated_at ON gateways;
