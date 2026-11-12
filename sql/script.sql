-- =========================================================
-- Script de criacao do banco de dados
-- Sistema de Gestao de Pedidos para Lanchonete Delivery
-- =========================================================

CREATE DATABASE IF NOT EXISTS lanchonete_delivery
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE lanchonete_delivery;

-- ---------------------------------------------------------
-- Tabela: cliente (Participante)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS cliente (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    nome      VARCHAR(100) NOT NULL,
    telefone  VARCHAR(20),
    endereco  VARCHAR(200),
    email     VARCHAR(100)
);

-- ---------------------------------------------------------
-- Tabela: produto (Produto / recurso vendavel)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS produto (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    nome             VARCHAR(100) NOT NULL,
    descricao        VARCHAR(255),
    preco            DECIMAL(10,2) NOT NULL,
    categoria        VARCHAR(50),
    imagem_caminho   VARCHAR(500)
);

-- ---------------------------------------------------------
-- Tabela: pedido (Operacao principal)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS pedido (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id       INT NOT NULL,
    data_hora        DATETIME NOT NULL,
    forma_pagamento  VARCHAR(30),
    status           VARCHAR(30) NOT NULL DEFAULT 'Em preparo',
    valor_total      DECIMAL(10,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente(id)
        ON DELETE RESTRICT
);

-- ---------------------------------------------------------
-- Tabela: item_pedido (Item da operacao)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS item_pedido (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id       INT NOT NULL,
    produto_id      INT NOT NULL,
    quantidade      INT NOT NULL,
    valor_unitario  DECIMAL(10,2) NOT NULL,
    subtotal        DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_item_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedido(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_item_produto FOREIGN KEY (produto_id)
        REFERENCES produto(id)
        ON DELETE RESTRICT
);

-- ---------------------------------------------------------
-- Dados de exemplo (opcional, apenas para testes)
-- ---------------------------------------------------------
INSERT INTO cliente (nome, telefone, endereco, email) VALUES
('Maria Silva', '(11) 98888-1111', 'Rua das Flores, 123', 'maria@email.com'),
('Joao Souza', '(11) 97777-2222', 'Av. Brasil, 456', 'joao@email.com');

INSERT INTO produto (nome, descricao, preco, categoria, imagem_caminho) VALUES
('X-Burguer', 'Pao, hamburguer, queijo e salada', 18.90, 'Lanches', NULL),
('X-Salada', 'Pao, hamburguer, queijo, alface e tomate', 19.90, 'Lanches', NULL),
('Batata Frita (P)', 'Porcao pequena de batata frita', 9.90, 'Acompanhamentos', NULL),
('Refrigerante Lata', 'Lata 350ml', 6.00, 'Bebidas', NULL);
