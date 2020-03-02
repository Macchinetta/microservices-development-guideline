/* 認証に関するテーブルの作成 */
CREATE TABLE oauth_client_details (client_id VARCHAR(256) PRIMARY KEY,resource_ids VARCHAR(256),client_secret VARCHAR(256),scope VARCHAR(256),authorized_grant_types VARCHAR(256),web_server_redirect_uri VARCHAR(256),authorities VARCHAR(256),access_token_validity INTEGER,refresh_token_validity INTEGER,additional_information VARCHAR(4096),autoapprove VARCHAR(256));
CREATE TABLE oauth_client_token (token_id VARCHAR(256),token bytea,authentication_id VARCHAR(256) PRIMARY KEY,user_name VARCHAR(256),client_id VARCHAR(256));
CREATE TABLE oauth_access_token (token_id VARCHAR(256),token bytea,authentication_id VARCHAR(256) PRIMARY KEY,user_name VARCHAR(256),client_id VARCHAR(256),authentication bytea,refresh_token VARCHAR(256));
CREATE TABLE oauth_refresh_token (token_id VARCHAR(256),token bytea,authentication bytea);
CREATE TABLE oauth_code (code VARCHAR(256), authentication bytea);
CREATE TABLE oauth_approvals (userId VARCHAR(256),clientId VARCHAR(256),scope VARCHAR(256),status VARCHAR(10),expiresAt TIMESTAMP,lastModifiedAt TIMESTAMP);
CREATE TABLE ClientDetails (appId VARCHAR(256) PRIMARY KEY,resourceIds VARCHAR(256),appSecret VARCHAR(256),scope VARCHAR(256),grantTypes VARCHAR(256),redirectUrl VARCHAR(256),authorities VARCHAR(256),access_token_validity INTEGER,refresh_token_validity INTEGER,additionalInformation VARCHAR(4096),autoApproveScopes VARCHAR(256));
CREATE TABLE users (member_id bigint NOT NULL PRIMARY KEY,PASSWORD VARCHAR(256) NOT NULL,ENABLED  BOOLEAN NOT NULL);

/* シーケンス初期値の設定 */
SELECT setval('hibernate_sequence',10);
SELECT nextval('hibernate_sequence');
SELECT setval('account_member_id_seq',1000000000);
SELECT nextval('account_member_id_seq');


/* 認証に関するマスターデータの登録 */
INSERT INTO oauth_client_details(client_id, resource_ids, client_secret, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove) VALUES ('my-client', '', '$2a$10$/FjjySSFYx16.Ml/q/w6.eZ0pPwuJlyvE0IhB1A01gcK84QKPKQmW', 'read,write', 'password,refresh_token', '', 'USER,GUEST,ADMIN', 60, 3600, '{}', '');

/* サンプルのユーザー情報の登録ーー０番ゲストユーザー */
INSERT INTO card (card_no, valid_till_month, valid_till_year, card_company_code, card_company_name) VALUES ('0000000000000000', '09', '23', 'VIS', 'VISA');
INSERT INTO member (member_id, address, birthday, email_id, first_name, first_name_kana, gender, postal_code, surname, surname_kana, telephone_no, card_no, valid_till_month, valid_till_year) VALUES (0, '東京１－２－３', TO_DATE('1979/01/25', 'YYYY/MM/DD'), 'abc@example.com', 'ゲスト', 'ゲスト', 0, '0000000', 'ゲスト', 'ゲスト', '000-0000-0000', '0000000000000000', '09', '23');
INSERT INTO users(member_id, PASSWORD, ENABLED) VALUES (0, '$2a$10$Z563ESirSsw0xZiVa3Pc2uyNpLvK6.NkCQL5Usi6Bg.1FeF6Z8/VS', true);
INSERT INTO authorities(member_id, AUTHORITY) VALUES (0, 'GUEST');
INSERT INTO password (password_id, created_at, password) VALUES (0, localtimestamp, '$2a$10$Z563ESirSsw0xZiVa3Pc2uyNpLvK6.NkCQL5Usi6Bg.1FeF6Z8/VS') ON CONFLICT ON CONSTRAINT password_pkey DO NOTHING;
INSERT INTO account (member_id) VALUES (0);
INSERT INTO account_authorities(account_member_id, authorities_member_id) VALUES (0, 0);
INSERT INTO account_passwords(account_member_id, passwords_password_id) VALUES (0, 0);

/* サンプルのユーザー情報の登録ーー1000000001番会員ユーザー */
INSERT INTO card (card_no, valid_till_month, valid_till_year, card_company_code, card_company_name) VALUES ('1234567890123457', '09', '23', 'VIS', 'VISA');
INSERT INTO member (member_id, address, birthday, email_id, first_name, first_name_kana, gender, postal_code, surname, surname_kana, telephone_no, card_no, valid_till_month, valid_till_year) VALUES (1000000001, '東京１－２－３', TO_DATE('1979/01/25', 'YYYY/MM/DD'), 'abc@example.com', '太郎', 'タロウ', 0, '2721234', '渡辺', 'ワタナベ', '080-1234-5678', '1234567890123457', '09', '23');
INSERT INTO users(member_id, PASSWORD, ENABLED) VALUES (1000000001, '$2a$10$Z563ESirSsw0xZiVa3Pc2uyNpLvK6.NkCQL5Usi6Bg.1FeF6Z8/VS', true);
INSERT INTO authorities(member_id, AUTHORITY) VALUES (1000000001, 'USER');
INSERT INTO password (password_id, created_at, password) VALUES (1, localtimestamp, '$2a$10$Z563ESirSsw0xZiVa3Pc2uyNpLvK6.NkCQL5Usi6Bg.1FeF6Z8/VS');
INSERT INTO account (member_id) VALUES (1000000001);
INSERT INTO account_authorities(account_member_id, authorities_member_id) VALUES (1000000001, 1000000001);
INSERT INTO account_passwords(account_member_id, passwords_password_id) VALUES (1000000001, 1);

/* サンプルのユーザー情報の登録ーー1000000002番会員ユーザー */
INSERT INTO card (card_no, valid_till_month, valid_till_year, card_company_code, card_company_name) VALUES ('1234567890123458', '09', '23', 'VIS', 'VISA');
INSERT INTO member (member_id, address, birthday, email_id, first_name, first_name_kana, gender, postal_code, surname, surname_kana, telephone_no, card_no, valid_till_month, valid_till_year) VALUES (1000000002, '東京１－２－３', TO_DATE('1979/01/25', 'YYYY/MM/DD'), 'abc@example.com', '次郎', 'ジロウ', 0, '2721234', '渡辺', 'ワタナベ', '080-1234-5678', '1234567890123458', '09', '23');
INSERT INTO users(member_id, PASSWORD, ENABLED) VALUES (1000000002, '$2a$10$Z563ESirSsw0xZiVa3Pc2uyNpLvK6.NkCQL5Usi6Bg.1FeF6Z8/VS', true);
INSERT INTO authorities(member_id, AUTHORITY) VALUES (1000000002, 'USER');
INSERT INTO password (password_id, created_at, password) VALUES (2, localtimestamp, '$2a$10$Z563ESirSsw0xZiVa3Pc2uyNpLvK6.NkCQL5Usi6Bg.1FeF6Z8/VS');
INSERT INTO account (member_id) VALUES (1000000002);
INSERT INTO account_authorities(account_member_id, authorities_member_id) VALUES (1000000002, 1000000002);
INSERT INTO account_passwords(account_member_id, passwords_password_id) VALUES (1000000002, 2);

/* サンプルのユーザー情報の登録ーー1000000003番会員ユーザー */
INSERT INTO card (card_no, valid_till_month, valid_till_year, card_company_code, card_company_name) VALUES ('1234567890123459', '09', '23', 'VIS', 'VISA');
INSERT INTO member (member_id, address, birthday, email_id, first_name, first_name_kana, gender, postal_code, surname, surname_kana, telephone_no, card_no, valid_till_month, valid_till_year) VALUES (1000000003, '東京１－２－３', TO_DATE('1979/01/25', 'YYYY/MM/DD'), 'abc@example.com', '三郎', 'サンロウ', 0, '2721234', '渡辺', 'ワタナベ', '080-1234-5678', '1234567890123459', '09', '23');
INSERT INTO users(member_id, PASSWORD, ENABLED) VALUES (1000000003, '$2a$10$Z563ESirSsw0xZiVa3Pc2uyNpLvK6.NkCQL5Usi6Bg.1FeF6Z8/VS', true);
INSERT INTO authorities(member_id, AUTHORITY) VALUES (1000000003, 'USER');
INSERT INTO password (password_id, created_at, password) VALUES (3, localtimestamp, '$2a$10$Z563ESirSsw0xZiVa3Pc2uyNpLvK6.NkCQL5Usi6Bg.1FeF6Z8/VS');
INSERT INTO account (member_id) VALUES (1000000003);
INSERT INTO account_authorities(account_member_id, authorities_member_id) VALUES (1000000003, 1000000003);
INSERT INTO account_passwords(account_member_id, passwords_password_id) VALUES (1000000003, 3);

/* サンプルのユーザー情報の登録ーー100000000４番会員ユーザー */
INSERT INTO card (card_no, valid_till_month, valid_till_year, card_company_code, card_company_name) VALUES ('1234567890123410', '09', '23', 'VIS', 'VISA');
INSERT INTO member (member_id, address, birthday, email_id, first_name, first_name_kana, gender, postal_code, surname, surname_kana, telephone_no, card_no, valid_till_month, valid_till_year) VALUES (1000000004, '東京1-2-3', TO_DATE('1979/01/25', 'YYYY/MM/DD'), 'abc@example.com', '花子', 'ハナコ', 1, '272-1234', '渡辺', 'ワタナベ', '080-1234-5678', '1234567890123410', '09', '23');
INSERT INTO users(member_id, PASSWORD, ENABLED) VALUES (1000000004, '$2a$10$Z563ESirSsw0xZiVa3Pc2uyNpLvK6.NkCQL5Usi6Bg.1FeF6Z8/VS', true);
INSERT INTO authorities(member_id, AUTHORITY) VALUES (1000000004, 'USER');
INSERT INTO password (password_id, created_at, password) VALUES (4, localtimestamp, '$2a$10$Z563ESirSsw0xZiVa3Pc2uyNpLvK6.NkCQL5Usi6Bg.1FeF6Z8/VS');
INSERT INTO account (member_id) VALUES (1000000004);
INSERT INTO account_authorities(account_member_id, authorities_member_id) VALUES (1000000004, 1000000004);
INSERT INTO account_passwords(account_member_id, passwords_password_id) VALUES (1000000004, 4);
