package br.com.teofilob.pendingnotification.application.ports.out;

import java.time.Duration;

/**
 * Output Port para gerenciar cache de pendências no Redis.
 * Responsável pela idempotência e detecção de duplicidade.
 */
public interface PendencyCachePort {
    
    /**
     * Verifica se uma chave de pendência já existe no cache.
     * Formato da chave: pendency:{email}:{title}
     * 
     * @param key chave única da pendência (email + title)
     * @return true se a chave existe no cache, false caso contrário
     */
    boolean exists(String key);

    /**
     * Salva uma pendência no cache com status PENDING e TTL configurado.
     * 
     * @param key chave única da pendência (email + title)
     * @param value dados da pendência (JSON serializado ou objeto)
     * @param ttl tempo de vida
     */
    void save(String key, String value, Duration ttl);

    /**
     * Atualiza o status de uma pendência no cache para PROCESSED.
     * Usado após consumo da fila.
     * 
     * @param key chave única da pendência
     */
    void markProcessed(String key);

    /**
     * Remove uma pendência do cache.
     * 
     * @param key chave única da pendência
     */
    void remove(String key);

    /**
     * Recupera o valor (dados) de uma pendência do cache.
     * 
     * @param key chave única da pendência
     * @return valor armazenado no cache ou null se não existir
     */
    String get(String key);
}
