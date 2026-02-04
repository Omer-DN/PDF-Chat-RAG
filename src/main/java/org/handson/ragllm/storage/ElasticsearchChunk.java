package org.handson.ragllm.storage;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Arrays;

/**
 * מודל Elasticsearch ל-chunk של PDF
 */
@Document(indexName = "pdf_chunks")
public class ElasticsearchChunk {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long pdfId;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Integer)
    private Integer chunkIndex;

    // Embedding vector stored as array
    // Note: For proper vector similarity search in production, configure index mapping manually
    // with dense_vector type. Spring Data Elasticsearch doesn't support dense_vector annotation directly.
    @Field(type = FieldType.Object, enabled = false)
    private float[] embedding;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getPdfId() {
        return pdfId;
    }

    public void setPdfId(Long pdfId) {
        this.pdfId = pdfId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    @Override
    public String toString() {
        return "ElasticsearchChunk{" +
                "id='" + id + '\'' +
                ", pdfId=" + pdfId +
                ", chunkIndex=" + chunkIndex +
                ", embedding=" + Arrays.toString(embedding) +
                '}';
    }
}
