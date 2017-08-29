CREATE TABLE payment_provider (
    id TEXT PRIMARY KEY,
    name TEXT,
    logo_url TEXT,
    version INTEGER DEFAULT 0,
    description TEXT,
    oauth_authorize_url TEXT,
    black_white_logo TEXT,
    background_color TEXT,
    text_color TEXT,
    button_color TEXT,
    currencies TEXT,
    asset_types TEXT
);

CREATE INDEX ix_payment_provider_name ON payment_provider ("name");


CREATE TABLE payment_asset (
    provider_id TEXT,
    id TEXT,
    type TEXT,
    name TEXT,
    currency TEXT,
    available_balance TEXT,
    total_balance TEXT,
    verified INTEGER DEFAULT 0,
    enabled INTEGER DEFAULT 0,
    has_balance INTEGER DEFAULT 0,
    has_transactions INTEGER DEFAULT 0,
    required_action TEXT,
    PRIMARY KEY (provider_id, id)
 );

 CREATE INDEX ix_payment_asset_name ON payment_asset ("name");


 CREATE TABLE security_key (
    type TEXT,
    algorithm TEXT,
    name TEXT,
    indexx TEXT,
    data TEXT,
    PRIMARY KEY (type, algorithm, name, indexx)
 );

  CREATE TABLE embedded_app_translations (
     id TEXT PRIMARY KEY,
     content TEXT
  );