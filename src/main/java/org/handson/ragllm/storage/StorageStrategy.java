package org.handson.ragllm.storage;

import org.handson.ragllm.model.PdfFile;

import java.util.List;

/**
 * Interface לאסטרטגיית אחסון - PostgreSQL
 */
public interface StorageStrategy {
    
    /**
     * שמירת chunks של PDF
     * @param pdfFile הקובץ PDF
     * @param chunks רשימת ה-chunks
     * @param vectors ה-embeddings המתאימים לכל chunk
     */
    void saveChunks(PdfFile pdfFile, List<String> chunks, List<float[]> vectors);
    
    /**
     * חיפוש chunks רלוונטיים לפי embedding
     * @param pdfId מזהה הקובץ PDF
     * @param queryVector ה-embedding של השאילתה
     * @param topK מספר התוצאות המבוקש
     * @return רשימת טקסטים של chunks רלוונטיים
     */
    List<String> searchSimilarChunks(Long pdfId, float[] queryVector, int topK);
    
    /**
     * מחיקת כל ה-chunks של קובץ PDF
     * @param pdfId מזהה הקובץ PDF
     */
    void deleteChunks(Long pdfId);
    
    /**
     * בדיקה אם האסטרטגיה תומכת בגודל קובץ מסוים
     * @param fileSizeBytes גודל הקובץ בבתים
     * @return true אם האסטרטגיה תומכת בגודל זה
     */
    boolean supportsFileSize(long fileSizeBytes);
}
