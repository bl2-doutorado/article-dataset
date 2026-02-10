-- Criar banco de dados para auth se não existir
CREATE DATABASE hvitops_auth;

-- Conectar ao banco de auth
\c hvitops_auth;

-- Criar tabela de usuários
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Criar índice em email
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Inserir usuários de teste com senhas hash (BCrypt)
-- Senha: "demo" com BCrypt
-- Hash gerado com: $2a$10$dXJ3SW6G7P50eS3BQybS2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW

INSERT INTO users (email, password_hash, name, role) VALUES
    ('patient@example.com', '$2a$10$dXJ3SW6G7P50eS3BQybS2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', 'João Silva', 'PATIENT'),
    ('doctor@example.com', '$2a$10$dXJ3SW6G7P50eS3BQybS2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', 'Dr. Carlos Santos', 'PHYSICIAN'),
    ('lab@example.com', '$2a$10$dXJ3SW6G7P50eS3BQybS2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', 'Maria Técnica', 'LAB_TECHNICIAN'),
    ('admin@example.com', '$2a$10$dXJ3SW6G7P50eS3BQybS2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', 'Admin User', 'ADMIN')
ON CONFLICT (email) DO NOTHING;
