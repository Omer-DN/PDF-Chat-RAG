package org.handson.ragllm.storage;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Factory לבחירת אסטרטגיית אחסון מתאימה לפי גודל הקובץ
 */
@Component
public class StorageStrategyFactory {

    private final List<StorageStrategy> strategies;

    public StorageStrategyFactory(List<StorageStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * מחזיר את כל האסטרטגיות הזמינות
     * @return רשימת כל האסטרטגיות
     */
    public List<StorageStrategy> getAllStrategies() {
        return strategies;
    }

    /**
     * בוחר את אסטרטגיית האחסון המתאימה לפי גודל הקובץ
     * עדיפות: PostgreSQL (לקבצים קטנים) ואז Elasticsearch (לקבצים גדולים)
     * @param fileSizeBytes גודל הקובץ בבתים
     * @return אסטרטגיית האחסון המתאימה
     * @throws IllegalArgumentException אם אין אסטרטגיה תומכת בגודל זה
     */
    public StorageStrategy getStrategy(long fileSizeBytes) {
        // נסה למצוא אסטרטגיה תומכת - קודם PostgreSQL, אחר כך Elasticsearch
        StorageStrategy postgresStrategy = strategies.stream()
                .filter(s -> s instanceof PostgreSQLStorageStrategy)
                .filter(s -> s.supportsFileSize(fileSizeBytes))
                .findFirst()
                .orElse(null);
        
        if (postgresStrategy != null) {
            return postgresStrategy;
        }
        
        // אם PostgreSQL לא תומך, נסה Elasticsearch
        return strategies.stream()
                .filter(s -> s instanceof ElasticsearchStorageStrategy)
                .filter(s -> s.supportsFileSize(fileSizeBytes))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No storage strategy supports file size: " + fileSizeBytes + " bytes (" +
                        (fileSizeBytes / 1024 / 1024) + " MB). File too large!"));
    }
}
