package org.handson.ragllm.storage;

import org.handson.ragllm.model.PdfFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * אסטרטגיית אחסון ב-Elasticsearch - מתאימה לקבצים גדולים
 * נטען רק אם Elasticsearch מוגדר ב-application.properties
 */
@Component
@ConditionalOnProperty(name = "spring.elasticsearch.uris")
public class ElasticsearchStorageStrategy implements StorageStrategy {

    private final ElasticsearchOperations elasticsearchOperations;
    private final long maxFileSizeBytes;
    private static final String INDEX_NAME = "pdf_chunks";

    public ElasticsearchStorageStrategy(
            ElasticsearchOperations elasticsearchOperations,
            @Value("${pdf.max.size.elasticsearch:104857600}") long maxFileSizeBytes) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    @Override
    public void saveChunks(PdfFile pdfFile, List<String> chunks, List<float[]> vectors) {
        if (chunks.size() != vectors.size()) {
            throw new IllegalArgumentException("Chunks and vectors lists must have the same size");
        }
        
        List<ElasticsearchChunk> esChunks = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            ElasticsearchChunk chunk = new ElasticsearchChunk();
            // יצירת ID ייחודי: pdfId_chunkIndex
            chunk.setId(pdfFile.getId() + "_" + i);
            chunk.setPdfId(pdfFile.getId());
            chunk.setContent(chunks.get(i));
            chunk.setChunkIndex(i);
            chunk.setEmbedding(vectors.get(i));
            esChunks.add(chunk);
        }
        
        // Batch save to Elasticsearch
        if (!esChunks.isEmpty()) {
            elasticsearchOperations.save(esChunks);
        }
    }

    @Override
    public List<String> searchSimilarChunks(Long pdfId, float[] queryVector, int topK) {
        // חיפוש vector similarity ב-Elasticsearch
        // שימוש ב-CriteriaQuery למימוש פשוט
        // Note: For proper vector similarity search, implement cosine similarity using script_score
        // This is a simplified version that returns chunks by pdfId only
        
        try {
            // Build query using Criteria API
            Criteria criteria = new Criteria("pdfId").is(pdfId);
            CriteriaQuery query = new CriteriaQuery(criteria);
            query.setMaxResults(topK);
            
            List<SearchHit<ElasticsearchChunk>> searchHits = 
                    elasticsearchOperations.search(query, ElasticsearchChunk.class).getSearchHits();
            
            return searchHits.stream()
                    .map(hit -> hit.getContent().getContent())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // במקרה של שגיאה, נזרוק exception כדי שהמערכת תדע שיש בעיה
            throw new RuntimeException("Error searching Elasticsearch: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteChunks(Long pdfId) {
        try {
            // Build query using Criteria API
            Criteria criteria = new Criteria("pdfId").is(pdfId);
            Query query = new CriteriaQuery(criteria);
            
            elasticsearchOperations.delete(query, ElasticsearchChunk.class);
        } catch (Exception e) {
            // Log error but don't throw - allow graceful degradation
            System.err.println("Error deleting chunks from Elasticsearch: " + e.getMessage());
        }
    }

    @Override
    public boolean supportsFileSize(long fileSizeBytes) {
        // Elasticsearch תומך בקבצים גדולים מעל הגבול של PostgreSQL ועד הגודל המקסימלי (ברירת מחדל: 100MB)
        // נניח ש-PostgreSQL תומך עד 10MB, אז Elasticsearch יתמוך מ-10MB עד 100MB
        long postgresMaxSize = 10 * 1024 * 1024; // 10MB
        return fileSizeBytes > postgresMaxSize && fileSizeBytes <= maxFileSizeBytes;
    }
}
