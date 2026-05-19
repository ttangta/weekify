-- user_credentials 테이블에는 사용자의 원문 비밀번호가 아닌 BCrypt 해시값이 저장하므로, 컬럼명을 password -> password_hash로 변경--
-- 이를 통해 DB 스키마만 확인하더라도 해당 값이 원문 비밀번호가 아니라 해싱된 인증 정보임을 명확히 표현하고, 비밀번호 원문 저장을 방지하는 보안 의도를 드러낼 수 있다.--
ALTER TABLE user_credentials
CHANGE COLUMN password password_hash VARCHAR(255) NOT NULL;